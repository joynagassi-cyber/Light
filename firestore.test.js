const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const pastDate = () => new Date(Date.now() - 2000);

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write profiles, courses, tasks, or focusSessions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("courses").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("tasks").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("focusSessions").get());
});

test("Owner can create and update their UserProfile with server timestamps", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(
    profileRef.set({
      userId: ALICE_UID,
      displayName: "Camille",
      email: "camille@ecole.fr",
      major: "Génie Logiciel · 3ᵉ Année",
      selectedGradientId: "aurore_lavande",
      dailyGoalTasks: 5,
      streakDays: 3,
      lastStudyDate: "2026-11-16",
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );

  await assertSucceeds(
    profileRef.update({
      selectedGradientId: "lagon_boreal",
      gender: "feminine",
      birthYear: 2005,
      age: 21,
      profession: "entrepreneur",
      customProfession: "",
      notificationsEnabled: true,
      notificationFrequency: "daily",
      onboardingCompleted: true,
      updatedAt: pastDate(),
    })
  );

  await assertFails(
    profileRef.update({
      profession: "invalid_job",
      updatedAt: pastDate(),
    })
  );

  await assertFails(
    profileRef.update({
      age: 3,
      updatedAt: pastDate(),
    })
  );
});

test("Shadow update test: UserProfile rejects ghost fields and userId spoofing", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Camille",
      major: "Génie Logiciel · 3ᵉ Année",
      selectedGradientId: "aurore_lavande",
      dailyGoalTasks: 5,
      streakDays: 0,
      isAdmin: true,
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Spoof",
      major: "Génie Logiciel",
      selectedGradientId: "aurore_lavande",
      dailyGoalTasks: 5,
      streakDays: 0,
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );
});

test("Cross-user isolation: Bob cannot read or write Alice's courses, tasks, or focusSessions", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("courses").doc("gl301").set({
      userId: ALICE_UID,
      code: "GL-301",
      title: "Architecture Logicielle",
      professor: "Prof. Laurent",
      iconKey: "code",
      colorIndex: 0,
      targetMinutes: 1200,
      completedMinutes: 300,
      grade: 16.5,
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("courses").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("courses").doc("gl301").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("courses").doc("gl301").update({
      title: "Hacked",
      updatedAt: pastDate(),
    })
  );
});

test("Owner can list, create, update, and delete tasks; focusSessions are append-only", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const tasksCol = aliceDb.collection("users").doc(ALICE_UID).collection("tasks");

  await assertSucceeds(tasksCol.get());

  const taskRef = tasksCol.doc("task_1");
  await assertSucceeds(
    taskRef.set({
      userId: ALICE_UID,
      courseId: "gl301",
      courseTitle: "Architecture Logicielle",
      title: "Réviser les Design Patterns GoF",
      notes: "Singleton, Observer, Strategy",
      dueDateIso: "2026-11-16",
      startDateIso: "2026-11-12",
      scheduledTime: "10:00",
      priority: "high",
      category: "exam",
      estimatedMinutes: 50,
      completed: false,
      aiGenerated: false,
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );

  await assertSucceeds(
    taskRef.update({
      completed: true,
      category: "milestone",
      updatedAt: pastDate(),
    })
  );

  await assertFails(
    taskRef.update({
      priority: "urgent",
      updatedAt: pastDate(),
    })
  );

  await assertFails(
    taskRef.update({
      category: "invalid_category",
      updatedAt: pastDate(),
    })
  );

  const sessionRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("focusSessions")
    .doc("sess_1");

  await assertSucceeds(
    sessionRef.set({
      userId: ALICE_UID,
      courseId: "gl301",
      courseTitle: "Architecture Logicielle",
      taskTitle: "Réviser les Design Patterns GoF",
      durationMinutes: 25,
      mode: "pomodoro",
      dateIso: "2026-11-16",
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("focusSessions")
      .doc("sess_stopwatch")
      .set({
        userId: ALICE_UID,
        courseId: "gl301",
        courseTitle: "Architecture Logicielle",
        taskTitle: "Chronomètre libre",
        durationMinutes: 35,
        mode: "stopwatch",
        dateIso: "2026-11-16",
        createdAt: pastDate(),
        updatedAt: pastDate(),
      })
  );

  await assertFails(
    sessionRef.update({
      durationMinutes: 120,
      updatedAt: pastDate(),
    })
  );

  // Task quadrant 1..4 allowed, 5 rejected
  await assertSucceeds(
    taskRef.update({
      quadrant: 2,
      updatedAt: pastDate(),
    })
  );
  await assertFails(
    taskRef.update({
      quadrant: 5,
      updatedAt: pastDate(),
    })
  );
});

test("Owner can manage habits and countdowns; cross-user and unauth rejected", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("habits").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("countdowns").get());

  const habitRef = aliceDb.collection("users").doc(ALICE_UID).collection("habits").doc("hab_1");
  await assertSucceeds(
    habitRef.set({
      userId: ALICE_UID,
      title: "Levé tôt",
      subtitle: "Levez-vous et soyez incroyable",
      category: "health",
      iconKey: "sun",
      colorIndex: 3,
      streakDays: 2,
      completedDates: ["2026-11-15", "2026-11-16"],
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );

  await assertSucceeds(
    habitRef.update({
      streakDays: 3,
      completedDates: ["2026-11-15", "2026-11-16", "2026-11-17"],
      updatedAt: pastDate(),
    })
  );

  await assertFails(
    habitRef.update({
      category: "unknown_cat",
      updatedAt: pastDate(),
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("habits").get());

  const countdownRef = aliceDb.collection("users").doc(ALICE_UID).collection("countdowns").doc("cd_1");
  await assertSucceeds(
    countdownRef.set({
      userId: ALICE_UID,
      title: "Partiel Systèmes Distribués",
      notes: "Amphi Turing · 09:00",
      targetDateIso: "2026-12-10",
      category: "exam",
      colorIndex: 4,
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );

  await assertFails(
    countdownRef.set({
      userId: ALICE_UID,
      title: "Invalid",
      targetDateIso: "2026-12-10",
      category: "invalid_type",
      colorIndex: 0,
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("countdowns").doc("cd_1").get());
});


test("Owners can manage projects and only link tasks to existing projects", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const userRef = aliceDb.collection("users").doc(ALICE_UID);
  const projectRef = userRef.collection("projects").doc("proj_capstone");

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("projects").get());
  await assertSucceeds(projectRef.set({
    userId: ALICE_UID,
    title: "Application de suivi",
    description: "Livrer une première version fonctionnelle.",
    startDateIso: "2026-11-16",
    dueDateIso: "2026-12-20",
    status: "active",
    createdAt: pastDate(),
    updatedAt: pastDate(),
  }));

  const linkedTask = userRef.collection("tasks").doc("task_in_project");
  await assertSucceeds(linkedTask.set({
    userId: ALICE_UID,
    courseId: "gl301",
    courseTitle: "Architecture Logicielle",
    projectId: "proj_capstone",
    title: "Écrire les tests d'intégration",
    notes: "",
    dueDateIso: "2026-12-02",
    startDateIso: "2026-11-20",
    scheduledTime: "10:00",
    priority: "medium",
    category: "project",
    estimatedMinutes: 60,
    completed: false,
    aiGenerated: false,
    createdAt: pastDate(),
    updatedAt: pastDate(),
  }));

  await assertFails(userRef.collection("tasks").doc("orphan_task").set({
    userId: ALICE_UID,
    title: "Tâche orpheline",
    dueDateIso: "2026-12-02",
    startDateIso: "2026-11-20",
    projectId: "missing_project",
    priority: "medium",
    estimatedMinutes: 30,
    completed: false,
    aiGenerated: false,
    createdAt: pastDate(),
    updatedAt: pastDate(),
  }));

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("projects").get());
  await assertSucceeds(projectRef.update({ status: "completed", updatedAt: pastDate() }));
  await assertFails(projectRef.update({ status: "unknown", updatedAt: pastDate() }));
  await assertFails(projectRef.update({ dueDateIso: "2026-10-01", updatedAt: pastDate() }));
  await assertFails(projectRef.update({ startDateIso: "2026-13-01", updatedAt: pastDate() }));
});
