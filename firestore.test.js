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

function awaitTaskOnClient(db, id, title, timeoutMs = 8000) {
  const ref = db.collection("users").doc(ALICE_UID).collection("tasks").doc(id);
  return new Promise((resolve, reject) => {
    const stop = () => {
      clearTimeout(timeout);
      unsubscribe();
    };
    const timeout = setTimeout(() => { stop(); reject(new Error(`Task not observed: ${id}`)); }, timeoutMs);
    const unsubscribe = ref.onSnapshot({ includeMetadataChanges: true }, (snapshot) => {
      if (!snapshot.metadata.fromCache && snapshot.data()?.title === title) { stop(); resolve(); }
    }, (error) => { stop(); reject(error); });
  });
}

test("Same-account devices observe committed task creation in both directions; other account is denied", async () => {
  const clients = [testEnv.authenticatedContext(ALICE_UID).firestore(), testEnv.authenticatedContext(ALICE_UID).firestore()];
  const otherUser = testEnv.authenticatedContext(BOB_UID).firestore();
  const taskRef = (db, id) => db.collection("users").doc(ALICE_UID).collection("tasks").doc(id);
  for (const [writer, reader, id] of [[clients[0], clients[1], "sync-a"], [clients[1], clients[0], "sync-b"]]) {
    const title = `Created on ${id}`;
    await Promise.all([
      awaitTaskOnClient(reader, id, title),
      assertSucceeds(taskRef(writer, id).set({
        userId: ALICE_UID, title, dueDateIso: "2026-11-19", priority: "medium",
        estimatedMinutes: 25, completed: false, aiGenerated: false, createdAt: pastDate(), updatedAt: pastDate(),
      })),
    ]);
    await assertFails(taskRef(otherUser, id).get());
  }
});

test("Task subtasks preserve order and reject invalid items; moving both dates is atomic", async () => {
  const db = testEnv.authenticatedContext(ALICE_UID).firestore();
  const task = db.collection("users").doc(ALICE_UID).collection("tasks").doc("draft");
  await assertSucceeds(task.set({
    userId: ALICE_UID, title: "Réviser", startDateIso: "2026-11-18", dueDateIso: "2026-11-19",
    subtasks: ["Lire", "Résumer"], priority: "medium", estimatedMinutes: 30,
    completed: false, aiGenerated: false, createdAt: pastDate(), updatedAt: pastDate(),
  }));
  require("node:assert/strict").deepEqual((await task.get()).data().subtasks, ["Lire", "Résumer"]);
  for (const subtasks of [Array(9).fill("test"), ["Lire", 2], [""], ["x".repeat(121)]]) {
    await assertFails(task.update({ subtasks, updatedAt: pastDate() }));
  }
  await assertFails(task.update({ startDateIso: "2026-11-21", updatedAt: pastDate() }));
  await assertSucceeds(task.update({ startDateIso: "2026-11-21", dueDateIso: "2026-11-22", updatedAt: pastDate() }));
});

test("Gantt dependencies accept existing owner tasks and reject invalid references", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const tasks = alice.collection("users").doc(ALICE_UID).collection("tasks");
  const payload = userId => ({ userId, title: "Étape", dueDateIso: "2026-11-19", priority: "medium",
    estimatedMinutes: 30, completed: false, aiGenerated: false, createdAt: pastDate(), updatedAt: pastDate() });
  const parent = tasks.doc("parent");
  const child = tasks.doc("child");
  await assertSucceeds(parent.set(payload(ALICE_UID)));
  await assertSucceeds(child.set(payload(ALICE_UID)));
  await assertSucceeds(bob.collection("users").doc(BOB_UID).collection("tasks").doc("bobs-task").set(payload(BOB_UID)));
  for (const predecessorIds of [["child"], ["bobs-task"], ["parent", "parent"], [42], ["../parent"], Array(5).fill("parent")]) {
    await assertFails(child.update({ predecessorIds, updatedAt: pastDate() }));
  }
  await assertSucceeds(child.update({ predecessorIds: ["parent"], updatedAt: pastDate() }));
  await assertFails(bob.collection("users").doc(ALICE_UID).collection("tasks").doc("child").update({ predecessorIds: [] }));
  await assertSucceeds(parent.delete());
  await assertSucceeds(child.update({ completed: true, updatedAt: pastDate() }));
  await assertSucceeds(child.update({ predecessorIds: [], updatedAt: pastDate() }));
});

test("Navigation syncs between devices, including an offline edit, and rejects settings and credentials", async () => {
  const assert = require("node:assert/strict");
  const firebase = require("firebase/compat/app");
  require("firebase/compat/firestore");
  const a = testEnv.authenticatedContext(ALICE_UID).firestore();
  const b = testEnv.authenticatedContext(ALICE_UID).firestore();
  const other = testEnv.authenticatedContext(BOB_UID).firestore();
  const nav = (db) => db.collection("users").doc(ALICE_UID).collection("preferences").doc("navigation");
  const updatedAt = () => firebase.firestore.FieldValue.serverTimestamp();
  const layout = { order: ["planner", "home", "focus", "matrix"], hidden: ["focus"], icons: { planner: "star" } };
  await assertSucceeds(nav(a).set({ ...layout, updatedAt: updatedAt() }));
  assert.deepEqual((await nav(b).get({ source: "server" })).data().order, layout.order);
  await b.disableNetwork();
  const pendingWrite = nav(b).set({ ...layout, hidden: ["focus", "matrix"], updatedAt: updatedAt() });
  const cached = await nav(b).get({ source: "cache" });
  assert.equal(cached.metadata.hasPendingWrites, true);
  assert.deepEqual(cached.data().hidden, ["focus", "matrix"]);
  await b.enableNetwork();
  await assertSucceeds(pendingWrite);
  assert.deepEqual((await nav(a).get({ source: "server" })).data().hidden, ["focus", "matrix"]);
  await assertFails(nav(other).get());
  await assertFails(nav(other).set({ ...layout, updatedAt: updatedAt() }));
  for (const bad of [
    { order: ["home", "planner", "focus", "settings"] },
    { order: ["home", "home", "focus", "matrix"] },
    { hidden: ["home"] }, { hidden: ["focus", "matrix", "planner"] },
    { icons: { home: "unsupported" } }, { apiKey: "fixture" },
  ]) await assertFails(nav(a).set({ ...layout, ...bad, updatedAt: updatedAt() }));
});
