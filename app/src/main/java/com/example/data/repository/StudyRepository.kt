package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.AdaptiveHabitTemplate
import com.example.data.model.CountdownCategory
import com.example.data.model.Course
import com.example.data.model.FocusProfile
import com.example.data.model.FocusSession
import com.example.data.model.HabitCategory
import com.example.data.model.NotificationFrequency
import com.example.data.model.StudyCountdown
import com.example.data.model.StudyHabit
import com.example.data.model.StudyProject
import com.example.data.model.StudyTask
import com.example.data.model.TaskCategory
import com.example.data.model.UserGender
import com.example.data.model.UserProfession
import com.example.data.model.UserProfile
import com.example.data.model.adaptiveStarterModulesFor
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate

class StudyRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : StudyWorkspaceRepository {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = FirebaseAuth.getInstance()
    )

    private fun userDoc(userId: String) = db.collection("users").document(userId)
    private fun coursesCol(userId: String) = userDoc(userId).collection("courses")
    private fun tasksCol(userId: String) = userDoc(userId).collection("tasks")
    private fun projectsCol(userId: String) = userDoc(userId).collection("projects")
    private fun sessionsCol(userId: String) = userDoc(userId).collection("focusSessions")
    private fun focusProfilesCol(userId: String) = userDoc(userId).collection("focusProfiles")
    private fun habitsCol(userId: String) = userDoc(userId).collection("habits")
    private fun countdownsCol(userId: String) = userDoc(userId).collection("countdowns")

    private fun requireSignedInUid(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("L'utilisateur doit être connecté avec Google.")
    }

    override fun observeUserProfile(userId: String): Flow<UserProfile?> =
        userDoc(userId).snapshots()
            .map { snap ->
                if (snap.exists()) {
                    snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.GET, "users/$userId")
                }
                throw error
            }

    override fun observeCourses(userId: String): Flow<List<Course>> =
        coursesCol(userId).snapshots()
            .map { snapshot ->
                snapshot.toObjects(Course::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.code }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "users/$userId/courses")
                }
                throw error
            }

    override fun observeTasks(userId: String): Flow<List<StudyTask>> =
        tasksCol(userId).snapshots()
            .map { snapshot ->
                snapshot.toObjects(StudyTask::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedWith(
                        compareBy<StudyTask> { it.completed }
                            .thenBy { it.dueDateIso }
                            .thenBy { priorityRank(it.priority) }
                            .thenBy { it.scheduledTime }
                    )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "users/$userId/tasks")
                }
                throw error
            }

    override fun observeProjects(userId: String): Flow<List<StudyProject>> =
        projectsCol(userId).snapshots()
            .map { snapshot ->
                snapshot.toObjects(StudyProject::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.dueDateIso }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "users/$userId/projects")
                }
                throw error
            }

    override fun observeFocusSessions(userId: String): Flow<List<FocusSession>> =
        sessionsCol(userId).snapshots()
            .map { snapshot ->
                snapshot.toObjects(FocusSession::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "users/$userId/focusSessions")
                }
                throw error
            }

    override fun observeFocusProfiles(userId: String): Flow<List<FocusProfile>> =
        focusProfilesCol(userId).snapshots()
            .map { snapshot ->
                snapshot.toObjects(FocusProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "users/$userId/focusProfiles")
                }
                throw error
            }

    override fun observeHabits(userId: String): Flow<List<StudyHabit>> =
        habitsCol(userId).snapshots()
            .map { snapshot ->
                snapshot.toObjects(StudyHabit::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.streakDays }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "users/$userId/habits")
                }
                throw error
            }

    override fun observeCountdowns(userId: String): Flow<List<StudyCountdown>> =
        countdownsCol(userId).snapshots()
            .map { snapshot ->
                snapshot.toObjects(StudyCountdown::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.targetDateIso }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "users/$userId/countdowns")
                }
                throw error
            }

    override fun observeHasPendingWrites(userId: String): Flow<Boolean> =
        combine(
            userDoc(userId).snapshots(MetadataChanges.INCLUDE).map { it.metadata.hasPendingWrites() }.catch { emit(false) },
            tasksCol(userId).snapshots(MetadataChanges.INCLUDE).map { it.metadata.hasPendingWrites() }.catch { emit(false) },
            projectsCol(userId).snapshots(MetadataChanges.INCLUDE).map { it.metadata.hasPendingWrites() }.catch { emit(false) },
            habitsCol(userId).snapshots(MetadataChanges.INCLUDE).map { it.metadata.hasPendingWrites() }.catch { emit(false) },
            focusProfilesCol(userId).snapshots(MetadataChanges.INCLUDE).map { it.metadata.hasPendingWrites() }.catch { emit(false) }
        ) { profilePending, tasksPending, projectsPending, habitsPending, focusProfilesPending ->
            profilePending || tasksPending || projectsPending || habitsPending || focusProfilesPending
        }

    private suspend fun awaitOptimisticWrite(
        task: Task<*>,
        operationType: OperationType,
        path: String
    ) {
        val outcome = withTimeoutOrNull(OPTIMISTIC_WRITE_ACK_TIMEOUT_MS) {
            try {
                task.await()
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure<Unit>(e)
            }
        }
        if (outcome != null) {
            outcome.getOrThrow()
            return
        }
        // Offline or slow network: Firestore has durably queued the write in persistent SQLite cache.
        requireSignedInUid()
        Log.i(TAG, "Write queued in offline persistent cache ($operationType at $path)")
        task.addOnFailureListener { e ->
            handleFirestoreError(e, operationType, path)
        }
    }

    private suspend fun getDocumentServerOrCache(docRef: DocumentReference): DocumentSnapshot? {
        val serverResult = withTimeoutOrNull(SERVER_OR_CACHE_TIMEOUT_MS) {
            try {
                Result.success(docRef.get().await())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure<DocumentSnapshot>(e)
            }
        }
        if (serverResult != null) {
            val err = serverResult.exceptionOrNull()
            if (err == null) return serverResult.getOrNull()
            val fsCode = (err as? FirebaseFirestoreException)?.code
            if (fsCode == FirebaseFirestoreException.Code.PERMISSION_DENIED ||
                fsCode == FirebaseFirestoreException.Code.UNAUTHENTICATED
            ) {
                throw err
            }
        }
        return try {
            docRef.get(Source.CACHE).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val fsCode = (e as? FirebaseFirestoreException)?.code
            if (fsCode == FirebaseFirestoreException.Code.PERMISSION_DENIED ||
                fsCode == FirebaseFirestoreException.Code.UNAUTHENTICATED
            ) {
                throw e
            }
            null
        }
    }

    private suspend fun getQueryServerOrCache(query: Query): QuerySnapshot? {
        val serverResult = withTimeoutOrNull(SERVER_OR_CACHE_TIMEOUT_MS) {
            try {
                Result.success(query.get().await())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure<QuerySnapshot>(e)
            }
        }
        if (serverResult != null) {
            val err = serverResult.exceptionOrNull()
            if (err == null) return serverResult.getOrNull()
            val fsCode = (err as? FirebaseFirestoreException)?.code
            if (fsCode == FirebaseFirestoreException.Code.PERMISSION_DENIED ||
                fsCode == FirebaseFirestoreException.Code.UNAUTHENTICATED
            ) {
                throw err
            }
        }
        return try {
            query.get(Source.CACHE).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val fsCode = (e as? FirebaseFirestoreException)?.code
            if (fsCode == FirebaseFirestoreException.Code.PERMISSION_DENIED ||
                fsCode == FirebaseFirestoreException.Code.UNAUTHENTICATED
            ) {
                throw e
            }
            null
        }
    }

    override suspend fun ensureUserProfileAndSeedIfNew(
        userId: String,
        displayName: String,
        email: String
    ) {
        val docRef = userDoc(userId)
        try {
            val snap = getDocumentServerOrCache(docRef)
            if (snap == null || !snap.exists()) {
                val safeName = displayName.trim().ifEmpty { "Étudiante" }.take(80)
                val today = LocalDate.now().toString()
                val profilePayload = mapOf(
                    "userId" to userId,
                    "displayName" to safeName,
                    "email" to email.take(120),
                    "major" to "Génie Logiciel · 3ᵉ Année",
                    "selectedGradientId" to "aurore_lavande",
                    "dailyGoalTasks" to 5L,
                    "streakDays" to 1L,
                    "lastStudyDate" to today,
                    "gender" to UserGender.FEMININE.key,
                    "birthYear" to 2005L,
                    "age" to 21L,
                    "profession" to UserProfession.STUDENT.key,
                    "customProfession" to "",
                    "notificationsEnabled" to true,
                    "notificationFrequency" to NotificationFrequency.DAILY.key,
                    "onboardingCompleted" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                awaitOptimisticWrite(docRef.set(profilePayload), OperationType.CREATE, docRef.path)
                seedSoftwareEngineeringStarterData(userId)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun seedSoftwareEngineeringStarterData(userId: String) {
        val uid = requireSignedInUid()
        if (uid != userId) return
        val existingCourses = getQueryServerOrCache(coursesCol(userId).limit(1))
        if (existingCourses != null && !existingCourses.isEmpty) return

        val today = LocalDate.now()
        val starterCourses = listOf(
            CourseSeed("gl301", "GL-301", "Architecture Logicielle & Design Patterns", "Prof. Laurent", 0L, 1440L, 620L, 16.5, "code"),
            CourseSeed("gl302", "GL-302", "Systèmes Distribués & Cloud", "Prof. Moreau", 1L, 1200L, 480L, 15.2, "network"),
            CourseSeed("gl303", "GL-303", "Bases de Données Avancées & NoSQL", "Prof. Benali", 2L, 1080L, 540L, 17.0, "database"),
            CourseSeed("gl304", "GL-304", "Algorithmique & Graphes", "Prof. Chen", 3L, 1320L, 710L, 16.0, "math-symbols"),
            CourseSeed("gl305", "GL-305", "Génie des Exigences & DevOps CI/CD", "Prof. Dupuis", 4L, 960L, 350L, 15.8, "code"),
            CourseSeed("gl306", "GL-306", "Systèmes d'Exploitation & Concurrence", "Prof. Girard", 5L, 1200L, 420L, 14.8, "device-laptop")
        )

        val batch = db.batch()
        starterCourses.forEach { c ->
            val ref = coursesCol(userId).document(c.id)
            batch.set(
                ref,
                mapOf(
                    "userId" to userId,
                    "code" to c.code,
                    "title" to c.title,
                    "professor" to c.professor,
                    "iconKey" to c.iconKey,
                    "colorIndex" to c.colorIndex,
                    "targetMinutes" to c.targetMinutes,
                    "completedMinutes" to c.completedMinutes,
                    "grade" to c.grade,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        val starterProfiles = listOf(
            FocusProfileSeed("fp1", "Deep Code & Architecture", "Maîtriser Kotlin, Clean Architecture & Graphes", "code", 0L, 45L, 50L),
            FocusProfileSeed("fp2", "Apprentissage Libre", "Explorer un nouveau sujet à mon rythme", "brain", 3L, 25L, 0L)
        )
        starterProfiles.forEach { fp ->
            val ref = focusProfilesCol(userId).document(fp.id)
            batch.set(
                ref,
                mapOf(
                    "userId" to userId,
                    "title" to fp.title,
                    "learningTopic" to fp.learningTopic,
                    "iconKey" to fp.iconKey,
                    "colorIndex" to fp.colorIndex,
                    "defaultMinutes" to fp.defaultMinutes,
                    "totalFocusMinutes" to fp.totalFocusMinutes,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        val starterTasks = listOf(
            TaskSeed("gl301", "Architecture Logicielle & Design Patterns", "Implémenter Observer & Strategy en Kotlin", "TP Clean Architecture + tests unitaires", today.toString(), "09:00", "high", TaskCategory.PROJECT.key, 50L, false, "fp1", "Deep Code & Architecture"),
            TaskSeed("gl302", "Systèmes Distribués & Cloud", "Revoir Consensus Raft & horloges de Lamport", "Fiches de synthèse chapitre 4", today.toString(), "11:15", "high", TaskCategory.EXAM.key, 45L, false),
            TaskSeed("gl304", "Algorithmique & Graphes", "Exercices Dijkstra & Bellman-Ford", "Série TD n°5 — complexité O(E log V)", today.toString(), "14:30", "medium", TaskCategory.ASSIGNMENT.key, 40L, true, "fp1", "Deep Code & Architecture"),
            TaskSeed("gl303", "Bases de Données Avancées & NoSQL", "Modélisation agrégats Firestore & indexation", "Schéma multi-tenant + règles sécurité", today.plusDays(1).toString(), "10:00", "medium", TaskCategory.PROJECT.key, 45L, false),
            TaskSeed("gl305", "Génie des Exigences & DevOps CI/CD", "Configurer pipeline GitHub Actions & lint", "Projet intégrateur S5", today.plusDays(2).toString(), "16:00", "low", TaskCategory.ASSIGNMENT.key, 30L, false)
        )

        starterTasks.forEach { t ->
            val ref = tasksCol(userId).document()
            batch.set(
                ref,
                mapOf(
                    "userId" to userId,
                    "courseId" to t.courseId,
                    "courseTitle" to t.courseTitle,
                    "focusProfileId" to t.focusProfileId,
                    "focusProfileTitle" to t.focusProfileTitle,
                    "title" to t.title,
                    "notes" to t.notes,
                    "dueDateIso" to t.dueDateIso,
                    "scheduledTime" to t.scheduledTime,
                    "priority" to t.priority,
                    "category" to t.category,
                    "estimatedMinutes" to t.estimatedMinutes,
                    "completed" to t.completed,
                    "aiGenerated" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        val starterSessions = listOf(
            SessionSeed("gl304", "Algorithmique & Graphes", "Exercices Dijkstra & Bellman-Ford", 50L, "deep_code", today.minusDays(2).toString(), "fp1", "Deep Code & Architecture"),
            SessionSeed("gl301", "Architecture Logicielle & Design Patterns", "Diagrammes UML & SOLID", 25L, "pomodoro", today.minusDays(1).toString()),
            SessionSeed("gl303", "Bases de Données Avancées & NoSQL", "Requêtes d'agrégation & transactions", 50L, "deep_code", today.toString())
        )
        starterSessions.forEach { s ->
            val ref = sessionsCol(userId).document()
            batch.set(
                ref,
                mapOf(
                    "userId" to userId,
                    "courseId" to s.courseId,
                    "courseTitle" to s.courseTitle,
                    "taskTitle" to s.taskTitle,
                    "focusProfileId" to s.focusProfileId,
                    "focusProfileTitle" to s.focusProfileTitle,
                    "durationMinutes" to s.durationMinutes,
                    "mode" to s.mode,
                    "dateIso" to s.dateIso,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        val starterHabits = listOf(
            HabitSeed(
                title = "Levé tôt",
                subtitle = "Levez-vous et soyez incroyable",
                category = HabitCategory.HEALTH.key,
                iconKey = "sun",
                colorIndex = 2L,
                streakDays = 2L,
                completedDates = listOf(today.minusDays(2).toString(), today.minusDays(1).toString())
            ),
            HabitSeed(
                title = "Prendre soin de ses yeux",
                subtitle = "Pause 20-20-20 loin des écrans de code",
                category = HabitCategory.HEALTH.key,
                iconKey = "eye",
                colorIndex = 3L,
                streakDays = 1L,
                completedDates = listOf(today.minusDays(1).toString())
            ),
            HabitSeed(
                title = "Apprendre de nouvelles choses",
                subtitle = "Petit nombre, grand résultat · Veille L3",
                category = HabitCategory.LIFE.key,
                iconKey = "book",
                colorIndex = 1L,
                streakDays = 3L,
                completedDates = listOf(
                    today.minusDays(3).toString(),
                    today.minusDays(2).toString(),
                    today.minusDays(1).toString()
                )
            )
        )
        starterHabits.forEach { h ->
            val ref = habitsCol(userId).document()
            batch.set(
                ref,
                mapOf(
                    "userId" to userId,
                    "title" to h.title,
                    "subtitle" to h.subtitle,
                    "category" to h.category,
                    "iconKey" to h.iconKey,
                    "colorIndex" to h.colorIndex,
                    "streakDays" to h.streakDays,
                    "completedDates" to h.completedDates,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        val starterCountdowns = listOf(
            CountdownSeed(
                title = "Partiel Systèmes Distribués",
                notes = "Consensus Raft, horloges logiques & gRPC",
                targetDateIso = today.plusDays(14).toString(),
                category = CountdownCategory.EXAM.key,
                colorIndex = 4L
            ),
            CountdownSeed(
                title = "Soutenance Projet Génie Logiciel",
                notes = "Démo Clean Architecture & couverture CI/CD",
                targetDateIso = today.plusDays(28).toString(),
                category = CountdownCategory.PROJECT.key,
                colorIndex = 0L
            )
        )
        starterCountdowns.forEach { c ->
            val ref = countdownsCol(userId).document()
            batch.set(
                ref,
                mapOf(
                    "userId" to userId,
                    "title" to c.title,
                    "notes" to c.notes,
                    "targetDateIso" to c.targetDateIso,
                    "category" to c.category,
                    "colorIndex" to c.colorIndex,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        awaitOptimisticWrite(batch.commit(), OperationType.WRITE, "users/$userId")
    }

    override suspend fun updateSelectedGradient(userId: String, gradientId: String) {
        val docRef = userDoc(userId)
        try {
            awaitOptimisticWrite(
                docRef.update(
                    mapOf(
                        "selectedGradientId" to gradientId,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ),
                OperationType.UPDATE,
                docRef.path
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    override suspend fun updateProfile(
        userId: String,
        displayName: String,
        major: String,
        dailyGoalTasks: Long,
        gender: String?,
        birthYear: Long?,
        age: Long?,
        profession: String?,
        customProfession: String?
    ) {
        val docRef = userDoc(userId)
        val payload = buildMap<String, Any> {
            put("displayName", displayName.trim().ifBlank { "Utilisateur" }.take(80))
            put("major", major.trim().ifBlank { "Organisation" }.take(80))
            put("dailyGoalTasks", dailyGoalTasks.coerceIn(1L, 50L))
            if (gender != null) put("gender", UserGender.fromKey(gender).key)
            if (birthYear != null) put("birthYear", birthYear.coerceIn(1930L, 2020L))
            if (age != null) put("age", age.coerceIn(6L, 100L))
            if (profession != null) put("profession", UserProfession.fromKey(profession).key)
            if (customProfession != null) put("customProfession", customProfession.trim().take(80))
            put("updatedAt", FieldValue.serverTimestamp())
        }
        try {
            awaitOptimisticWrite(docRef.update(payload), OperationType.UPDATE, docRef.path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    override suspend fun saveOnboardingConfiguration(
        userId: String,
        displayName: String,
        gender: UserGender,
        birthYear: Long,
        age: Long,
        profession: UserProfession,
        customProfession: String,
        major: String,
        selectedGradientId: String,
        dailyGoalTasks: Long,
        notificationsEnabled: Boolean,
        notificationFrequency: NotificationFrequency,
        selectedHabits: List<AdaptiveHabitTemplate>,
        adaptModulesToProfession: Boolean
    ) {
        requireSignedInUid()
        val docRef = userDoc(userId)
        val safeMajor = major.trim().ifBlank { profession.defaultFocusDomain() }.take(80)
        val safeCustom = if (profession == UserProfession.OTHER) customProfession.trim().take(80) else ""
        val safeName = displayName.trim().ifBlank { profession.labelFor(gender, safeCustom) }.take(80)
        val safeBirthYear = birthYear.coerceIn(1930L, 2020L)
        val safeAge = age.coerceIn(6L, 100L)

        try {
            awaitOptimisticWrite(
                docRef.update(
                    mapOf(
                        "displayName" to safeName,
                        "gender" to gender.key,
                        "birthYear" to safeBirthYear,
                        "age" to safeAge,
                        "profession" to profession.key,
                        "customProfession" to safeCustom,
                        "major" to safeMajor,
                        "selectedGradientId" to selectedGradientId.trim().ifBlank { "aurore_lavande" }.take(40),
                        "dailyGoalTasks" to dailyGoalTasks.coerceIn(1L, 50L),
                        "notificationsEnabled" to notificationsEnabled,
                        "notificationFrequency" to notificationFrequency.key,
                        "onboardingCompleted" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ),
                OperationType.UPDATE,
                docRef.path
            )

            // Create selected habits that don't already exist
            if (selectedHabits.isNotEmpty()) {
                val existingTitles = getQueryServerOrCache(habitsCol(userId))?.documents.orEmpty()
                    .mapNotNull { it.getString("title")?.trim()?.lowercase() }
                    .toHashSet()
                val batch = db.batch()
                var writes = 0
                selectedHabits.forEach { habit ->
                    if (!existingTitles.contains(habit.title.trim().lowercase())) {
                        val ref = habitsCol(userId).document()
                        batch.set(
                            ref,
                            mapOf(
                                "userId" to userId,
                                "title" to habit.title.trim().take(80),
                                "subtitle" to habit.subtitle.trim().take(140),
                                "category" to habit.category.key,
                                "iconKey" to habit.iconKey.take(30),
                                "colorIndex" to habit.colorIndex.coerceIn(0L, 15L),
                                "streakDays" to 0L,
                                "completedDates" to emptyList<String>(),
                                "createdAt" to FieldValue.serverTimestamp(),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                        writes++
                    }
                }
                if (writes > 0) awaitOptimisticWrite(batch.commit(), OperationType.WRITE, "users/$userId/habits")
            }

            if (adaptModulesToProfession) {
                val starterIds = setOf(
                    "gl301", "gl302", "gl303", "gl304", "gl305", "gl306",
                    "ent01", "ent02", "ent03", "ent04", "ent05",
                    "auto01", "auto02", "auto03", "auto04",
                    "per01", "per02", "per03", "per04",
                    "oth01", "oth02", "oth03", "oth04"
                )
                val existingDocs = getQueryServerOrCache(coursesCol(userId))?.documents.orEmpty()
                val onlyStarterOrEmpty = existingDocs.all { it.id in starterIds }
                if (onlyStarterOrEmpty) {
                    val batch = db.batch()
                    existingDocs.forEach { batch.delete(it.reference) }
                    adaptiveStarterModulesFor(profession, safeCustom).forEach { mod ->
                        batch.set(
                            coursesCol(userId).document(mod.id),
                            mapOf(
                                "userId" to userId,
                                "code" to mod.code,
                                "title" to mod.title,
                                "professor" to mod.lead,
                                "iconKey" to mod.iconKey,
                                "colorIndex" to mod.colorIndex,
                                "targetMinutes" to mod.targetMinutes,
                                "completedMinutes" to 0L,
                                "grade" to 15.0,
                                "createdAt" to FieldValue.serverTimestamp(),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                    awaitOptimisticWrite(batch.commit(), OperationType.WRITE, "users/$userId/courses")
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    override suspend fun updateNotificationSettings(
        userId: String,
        enabled: Boolean,
        frequency: NotificationFrequency
    ) {
        val docRef = userDoc(userId)
        try {
            awaitOptimisticWrite(
                docRef.update(
                    mapOf(
                        "notificationsEnabled" to enabled,
                        "notificationFrequency" to frequency.key,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ),
                OperationType.UPDATE,
                docRef.path
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    override suspend fun createCourse(
        userId: String,
        code: String,
        title: String,
        professor: String,
        colorIndex: Long,
        targetMinutes: Long,
        grade: Double,
        iconKey: String
    ): String {
        requireSignedInUid()
        val docRef = coursesCol(userId).document()
        val payload = mapOf(
            "userId" to userId,
            "code" to code.trim().take(20),
            "title" to title.trim().take(100),
            "professor" to professor.trim().take(80),
            "iconKey" to iconKey.trim().ifEmpty { "book" }.take(40),
            "colorIndex" to colorIndex.coerceIn(0L, 15L),
            "targetMinutes" to targetMinutes.coerceIn(30L, 60000L),
            "completedMinutes" to 0L,
            "grade" to grade.coerceIn(0.0, 20.0),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            awaitOptimisticWrite(docRef.set(payload), OperationType.CREATE, docRef.path)
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    override suspend fun updateCourseGrade(userId: String, courseId: String, newGrade: Double) {
        val docRef = coursesCol(userId).document(courseId)
        try {
            awaitOptimisticWrite(
                docRef.update(
                    mapOf(
                        "grade" to newGrade.coerceIn(0.0, 20.0),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ),
                OperationType.UPDATE,
                docRef.path
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    override suspend fun deleteCourse(userId: String, courseId: String) {
        val docRef = coursesCol(userId).document(courseId)
        try {
            awaitOptimisticWrite(docRef.delete(), OperationType.DELETE, docRef.path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    override suspend fun createProject(
        userId: String,
        title: String,
        description: String,
        startDateIso: String,
        dueDateIso: String
    ): String {
        require(requireSignedInUid() == userId) { "Utilisateur non autorisé." }
        val safeTitle = title.trim().take(120)
        require(safeTitle.isNotBlank()) { "Le nom du projet est obligatoire." }
        val start = LocalDate.parse(startDateIso)
        val due = LocalDate.parse(dueDateIso)
        require(!due.isBefore(start)) { "L'échéance doit être postérieure au début." }
        val docRef = projectsCol(userId).document()
        try {
            awaitOptimisticWrite(
                docRef.set(
                    mapOf(
                        "userId" to userId,
                        "title" to safeTitle,
                        "description" to description.trim().take(500),
                        "startDateIso" to start.toString(),
                        "dueDateIso" to due.toString(),
                        "status" to "active",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ),
                OperationType.CREATE,
                docRef.path
            )
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    override suspend fun updateProject(project: StudyProject) {
        require(requireSignedInUid() == project.userId) { "Utilisateur non autorisé." }
        val start = LocalDate.parse(project.startDateIso)
        val due = LocalDate.parse(project.dueDateIso)
        require(!due.isBefore(start)) { "L'échéance doit être postérieure au début." }
        require(project.status in setOf("active", "on_hold", "completed"))
        val docRef = projectsCol(project.userId).document(project.id)
        try {
            awaitOptimisticWrite(
                docRef.update(
                    mapOf(
                        "title" to project.title.trim().take(120),
                        "description" to project.description.trim().take(500),
                        "startDateIso" to start.toString(),
                        "dueDateIso" to due.toString(),
                        "status" to project.status,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ),
                OperationType.UPDATE,
                docRef.path
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    override suspend fun deleteProject(userId: String, projectId: String) {
        val docRef = projectsCol(userId).document(projectId)
        try {
            require(requireSignedInUid() == userId) { "Utilisateur non autorisé." }
            val linkedTasks = getQueryServerOrCache(
                tasksCol(userId).whereEqualTo("projectId", projectId)
            )?.documents.orEmpty()
            linkedTasks.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { task ->
                    batch.update(
                        task.reference,
                        mapOf(
                            "projectId" to "",
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                awaitOptimisticWrite(batch.commit(), OperationType.WRITE, "users/$userId/tasks")
            }
            awaitOptimisticWrite(docRef.delete(), OperationType.DELETE, docRef.path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    override suspend fun createTask(
        userId: String,
        courseId: String,
        courseTitle: String,
        title: String,
        notes: String,
        dueDateIso: String,
        scheduledTime: String,
        priority: String,
        category: String,
        estimatedMinutes: Long,
        aiGenerated: Boolean,
        quadrant: Long?,
        startDateIso: String,
        projectId: String,
        focusProfileId: String,
        focusProfileTitle: String
    ): String {
        require(requireSignedInUid() == userId) { "Utilisateur non autorisé." }
        val safeProjectId = projectId.trim().take(128)
        if (safeProjectId.isNotBlank()) {
            val project = getDocumentServerOrCache(projectsCol(userId).document(safeProjectId))
            require(project?.exists() == true) { "Le projet sélectionné n'existe plus." }
        }
        val docRef = tasksCol(userId).document()
        val safePriority = if (priority in listOf("high", "medium", "low")) priority else "medium"
        val safeCategory = TaskCategory.sanitizeKey(category)
        val payload = buildMap<String, Any> {
            put("userId", userId)
            put("courseId", courseId.take(128))
            put("courseTitle", courseTitle.take(100))
            put("title", title.trim().take(140))
            put("notes", notes.trim().take(500))
            put("dueDateIso", dueDateIso.trim().take(20))
            put("startDateIso", startDateIso.trim().take(20).ifBlank { dueDateIso.trim().take(20) })
            put("projectId", safeProjectId)
            put("focusProfileId", focusProfileId.trim().take(128))
            put("focusProfileTitle", focusProfileTitle.trim().take(100))
            put("scheduledTime", scheduledTime.trim().take(20))
            put("priority", safePriority)
            put("category", safeCategory)
            put("estimatedMinutes", estimatedMinutes.coerceIn(5L, 600L))
            if (quadrant != null && quadrant in 1L..4L) {
                put("quadrant", quadrant)
            }
            put("completed", false)
            put("aiGenerated", aiGenerated)
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
        }
        try {
            awaitOptimisticWrite(docRef.set(payload), OperationType.CREATE, docRef.path)
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    override suspend fun createTasksBatch(userId: String, tasks: List<StudyTask>) {
        requireSignedInUid()
        if (tasks.isEmpty()) return
        val batch = db.batch()
        tasks.forEach { task ->
            val docRef = tasksCol(userId).document()
            val safePriority = if (task.priority in listOf("high", "medium", "low")) task.priority else "medium"
            val safeCategory = TaskCategory.sanitizeKey(task.category)
            batch.set(
                docRef,
                mapOf(
                    "userId" to userId,
                    "courseId" to task.courseId.take(128),
                    "courseTitle" to task.courseTitle.take(100),
                    "title" to task.title.trim().take(140),
                    "notes" to task.notes.trim().take(500),
                    "dueDateIso" to task.dueDateIso.trim().take(20),
                    "startDateIso" to task.startDateIso.trim().take(20).ifBlank { task.dueDateIso.trim().take(20) },
                    "projectId" to task.projectId.trim().take(128),
                    "scheduledTime" to task.scheduledTime.trim().take(20),
                    "priority" to safePriority,
                    "category" to safeCategory,
                    "estimatedMinutes" to task.estimatedMinutes.coerceIn(5L, 600L),
                    "completed" to false,
                    "aiGenerated" to task.aiGenerated,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }
        try {
            awaitOptimisticWrite(batch.commit(), OperationType.WRITE, "users/$userId/tasks")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$userId/tasks")
            throw e
        }
    }

    override suspend fun toggleTaskCompleted(userId: String, task: StudyTask) {
        val docRef = tasksCol(userId).document(task.id)
        try {
            awaitOptimisticWrite(
                docRef.update(
                    mapOf(
                        "completed" to !task.completed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ),
                OperationType.UPDATE,
                docRef.path
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    override suspend fun updateTaskQuadrant(userId: String, taskId: String, quadrant: Long) {
        requireSignedInUid()
        val safeQuadrant = quadrant.coerceIn(1L, 4L)
        val docRef = tasksCol(userId).document(taskId)
        try {
            awaitOptimisticWrite(
                docRef.update(
                    mapOf(
                        "quadrant" to safeQuadrant,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ),
                OperationType.UPDATE,
                docRef.path
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    override suspend fun assignTaskToFocusProfile(
        userId: String,
        taskId: String,
        focusProfileId: String,
        focusProfileTitle: String
    ) {
        requireSignedInUid()
        val safeProfileId = focusProfileId.trim().take(128)
        val safeProfileTitle = if (safeProfileId.isBlank()) "" else focusProfileTitle.trim().take(100)
        val docRef = tasksCol(userId).document(taskId)
        try {
            awaitOptimisticWrite(
                docRef.update(
                    mapOf(
                        "focusProfileId" to safeProfileId,
                        "focusProfileTitle" to safeProfileTitle,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ),
                OperationType.UPDATE,
                docRef.path
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    override suspend fun deleteTask(userId: String, taskId: String) {
        val docRef = tasksCol(userId).document(taskId)
        try {
            awaitOptimisticWrite(docRef.delete(), OperationType.DELETE, docRef.path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    override suspend fun createFocusProfile(
        userId: String,
        title: String,
        learningTopic: String,
        iconKey: String,
        colorIndex: Long,
        defaultMinutes: Long
    ): String {
        require(requireSignedInUid() == userId) { "Utilisateur non autorisé." }
        val safeTitle = title.trim().take(100)
        require(safeTitle.isNotBlank()) { "Le nom du profil de focus est obligatoire." }
        val docRef = focusProfilesCol(userId).document()
        val payload = mapOf(
            "userId" to userId,
            "title" to safeTitle,
            "learningTopic" to learningTopic.trim().take(220),
            "iconKey" to iconKey.trim().ifEmpty { "brain" }.take(40),
            "colorIndex" to colorIndex.coerceIn(0L, 15L),
            "defaultMinutes" to defaultMinutes.coerceIn(5L, 180L),
            "totalFocusMinutes" to 0L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            awaitOptimisticWrite(docRef.set(payload), OperationType.CREATE, docRef.path)
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    override suspend fun deleteFocusProfile(userId: String, profileId: String) {
        val docRef = focusProfilesCol(userId).document(profileId)
        try {
            require(requireSignedInUid() == userId) { "Utilisateur non autorisé." }
            val linkedTasks = getQueryServerOrCache(
                tasksCol(userId).whereEqualTo("focusProfileId", profileId)
            )?.documents.orEmpty()
            linkedTasks.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { task ->
                    batch.update(
                        task.reference,
                        mapOf(
                            "focusProfileId" to "",
                            "focusProfileTitle" to "",
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                awaitOptimisticWrite(batch.commit(), OperationType.WRITE, "users/$userId/tasks")
            }
            awaitOptimisticWrite(docRef.delete(), OperationType.DELETE, docRef.path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    override suspend fun createHabit(
        userId: String,
        title: String,
        subtitle: String,
        category: String,
        iconKey: String,
        colorIndex: Long
    ): String {
        requireSignedInUid()
        val docRef = habitsCol(userId).document()
        val payload = mapOf(
            "userId" to userId,
            "title" to title.trim().take(80),
            "subtitle" to subtitle.trim().take(140),
            "category" to HabitCategory.sanitizeKey(category),
            "iconKey" to iconKey.trim().ifEmpty { "sparkles" }.take(30),
            "colorIndex" to colorIndex.coerceIn(0L, 15L),
            "streakDays" to 0L,
            "completedDates" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            awaitOptimisticWrite(docRef.set(payload), OperationType.CREATE, docRef.path)
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    override suspend fun toggleHabitForDate(userId: String, habit: StudyHabit, dateIso: String) {
        requireSignedInUid()
        val docRef = habitsCol(userId).document(habit.id)
        val updatedDates = if (habit.completedDates.contains(dateIso)) {
            habit.completedDates.filterNot { it == dateIso }
        } else {
            (habit.completedDates + dateIso).distinct().sorted().takeLast(365)
        }
        val streak = computeHabitStreak(updatedDates, LocalDate.now())
        try {
            awaitOptimisticWrite(
                docRef.update(
                    mapOf(
                        "completedDates" to updatedDates,
                        "streakDays" to streak,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ),
                OperationType.UPDATE,
                docRef.path
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    override suspend fun deleteHabit(userId: String, habitId: String) {
        val docRef = habitsCol(userId).document(habitId)
        try {
            awaitOptimisticWrite(docRef.delete(), OperationType.DELETE, docRef.path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    override suspend fun createCountdown(
        userId: String,
        title: String,
        notes: String,
        targetDateIso: String,
        category: String,
        colorIndex: Long
    ): String {
        requireSignedInUid()
        val docRef = countdownsCol(userId).document()
        val safeDate = targetDateIso.trim().take(20).ifBlank { LocalDate.now().plusDays(7).toString() }
        val payload = mapOf(
            "userId" to userId,
            "title" to title.trim().take(100),
            "notes" to notes.trim().take(300),
            "targetDateIso" to safeDate,
            "category" to CountdownCategory.sanitizeKey(category),
            "colorIndex" to colorIndex.coerceIn(0L, 15L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            awaitOptimisticWrite(docRef.set(payload), OperationType.CREATE, docRef.path)
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    override suspend fun deleteCountdown(userId: String, countdownId: String) {
        val docRef = countdownsCol(userId).document(countdownId)
        try {
            awaitOptimisticWrite(docRef.delete(), OperationType.DELETE, docRef.path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    private fun computeHabitStreak(dates: List<String>, today: LocalDate): Long {
        if (dates.isEmpty()) return 0L
        val set = dates.toHashSet()
        var cursor = if (set.contains(today.toString())) today else today.minusDays(1)
        var count = 0L
        while (set.contains(cursor.toString()) && count < 3650L) {
            count++
            cursor = cursor.minusDays(1)
        }
        return count
    }

    override suspend fun logFocusSession(
        userId: String,
        courseId: String,
        courseTitle: String,
        taskTitle: String,
        durationMinutes: Long,
        mode: String,
        dateIso: String,
        focusProfileId: String,
        focusProfileTitle: String,
        taskId: String
    ): String {
        requireSignedInUid()
        val sessionRef = sessionsCol(userId).document()
        val safeMode = if (mode in listOf("pomodoro", "deep_code", "short_break", "stopwatch")) mode else "pomodoro"
        val safeDuration = durationMinutes.coerceIn(1L, 240L)
        val safeProfileId = focusProfileId.trim().take(128)
        val safeProfileTitle = focusProfileTitle.trim().take(100)

        val payload = mapOf(
            "userId" to userId,
            "courseId" to courseId.take(128),
            "courseTitle" to courseTitle.take(100),
            "taskTitle" to taskTitle.trim().take(140),
            "focusProfileId" to safeProfileId,
            "focusProfileTitle" to safeProfileTitle,
            "taskId" to taskId.trim().take(128),
            "durationMinutes" to safeDuration,
            "mode" to safeMode,
            "dateIso" to dateIso.take(20),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            awaitOptimisticWrite(sessionRef.set(payload), OperationType.CREATE, sessionRef.path)
            if (courseId.isNotBlank() && safeMode != "short_break") {
                val courseRef = coursesCol(userId).document(courseId)
                awaitOptimisticWrite(
                    courseRef.update(
                        mapOf(
                            "completedMinutes" to FieldValue.increment(safeDuration),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ),
                    OperationType.UPDATE,
                    courseRef.path
                )
            }
            if (safeProfileId.isNotBlank() && safeMode != "short_break") {
                val profileRef = focusProfilesCol(userId).document(safeProfileId)
                val fpSnap = getDocumentServerOrCache(profileRef)
                if (fpSnap != null && fpSnap.exists()) {
                    awaitOptimisticWrite(
                        profileRef.update(
                            mapOf(
                                "totalFocusMinutes" to FieldValue.increment(safeDuration),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ),
                        OperationType.UPDATE,
                        profileRef.path
                    )
                }
            }
            if (safeMode != "short_break") {
                val profileSnap = getDocumentServerOrCache(userDoc(userId))
                if (profileSnap != null && profileSnap.exists()) {
                    val lastDate = profileSnap.getString("lastStudyDate") ?: ""
                    val currentStreak = profileSnap.getLong("streakDays") ?: 0L
                    val today = LocalDate.now()
                    val yesterdayStr = today.minusDays(1).toString()
                    val todayStr = today.toString()
                    val nextStreak = when (lastDate) {
                        todayStr -> maxOf(1L, currentStreak)
                        yesterdayStr -> currentStreak + 1L
                        else -> 1L
                    }
                    awaitOptimisticWrite(
                        userDoc(userId).update(
                            mapOf(
                                "streakDays" to nextStreak,
                                "lastStudyDate" to todayStr,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ),
                        OperationType.UPDATE,
                        userDoc(userId).path
                    )
                }
            }
            return sessionRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, sessionRef.path)
            throw e
        }
    }

    private companion object {
        const val TAG = "StudyRepository"
        const val OPTIMISTIC_WRITE_ACK_TIMEOUT_MS = 1500L
        const val SERVER_OR_CACHE_TIMEOUT_MS = 1500L
    }

    private fun priorityRank(priority: String): Int = when (priority) {
        "high" -> 0
        "medium" -> 1
        else -> 2
    }

    private data class CourseSeed(
        val id: String,
        val code: String,
        val title: String,
        val professor: String,
        val colorIndex: Long,
        val targetMinutes: Long,
        val completedMinutes: Long,
        val grade: Double,
        val iconKey: String = "book"
    )

    private data class FocusProfileSeed(
        val id: String,
        val title: String,
        val learningTopic: String,
        val iconKey: String,
        val colorIndex: Long,
        val defaultMinutes: Long,
        val totalFocusMinutes: Long
    )

    private data class TaskSeed(
        val courseId: String,
        val courseTitle: String,
        val title: String,
        val notes: String,
        val dueDateIso: String,
        val scheduledTime: String,
        val priority: String,
        val category: String,
        val estimatedMinutes: Long,
        val completed: Boolean,
        val focusProfileId: String = "",
        val focusProfileTitle: String = ""
    )

    private data class SessionSeed(
        val courseId: String,
        val courseTitle: String,
        val taskTitle: String,
        val durationMinutes: Long,
        val mode: String,
        val dateIso: String,
        val focusProfileId: String = "",
        val focusProfileTitle: String = ""
    )

    private data class HabitSeed(
        val title: String,
        val subtitle: String,
        val category: String,
        val iconKey: String,
        val colorIndex: Long,
        val streakDays: Long,
        val completedDates: List<String>
    )

    private data class CountdownSeed(
        val title: String,
        val notes: String,
        val targetDateIso: String,
        val category: String,
        val colorIndex: Long
    )
}
