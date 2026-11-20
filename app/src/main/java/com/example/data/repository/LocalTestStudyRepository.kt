package com.example.data.repository

import android.content.Context
import android.util.Log
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
import com.example.data.model.normalizeSubtasks
import com.example.data.model.TaskCategory
import com.example.data.model.UserGender
import com.example.data.model.UserProfession
import com.example.data.model.UserProfile
import com.example.data.model.adaptiveStarterModulesFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.io.File
import java.time.LocalDate
import java.util.UUID

/**
 * Local workspace repository used when Google Sign-In is paused for local testing / offline use.
 * Supports atomic on-device disk persistence across restarts and idempotent migration to Cloud Firestore
 * when the user later connects a Google account.
 */
class LocalTestStudyRepository(
    private val storageFile: File? = null
) : StudyWorkspaceRepository {

    constructor(context: Context) : this(
        storageFile = File(context.applicationContext.filesDir, "light_local_workspace.json")
    )

    private val profileFlow = MutableStateFlow<UserProfile?>(null)
    private val coursesFlow = MutableStateFlow<List<Course>>(emptyList())
    private val tasksFlow = MutableStateFlow<List<StudyTask>>(emptyList())
    private val projectsFlow = MutableStateFlow<List<StudyProject>>(emptyList())
    private val sessionsFlow = MutableStateFlow<List<FocusSession>>(emptyList())
    private val focusProfilesFlow = MutableStateFlow<List<FocusProfile>>(emptyList())
    private val habitsFlow = MutableStateFlow<List<StudyHabit>>(emptyList())
    private val countdownsFlow = MutableStateFlow<List<StudyCountdown>>(emptyList())

    private val persistLock = Any()
    private val cloudSyncMutex = Mutex()
    private var hasUserModifications = false
    private val syncedCloudUserIds = mutableSetOf<String>()

    init {
        loadFromDisk()
    }

    override fun observeUserProfile(userId: String): Flow<UserProfile?> = profileFlow.asStateFlow()

    override fun observeCourses(userId: String): Flow<List<Course>> =
        coursesFlow.map { list -> list.sortedBy { it.code } }

    override fun observeTasks(userId: String): Flow<List<StudyTask>> =
        tasksFlow.map { list ->
            list.sortedWith(
                compareBy<StudyTask> { it.completed }
                    .thenBy { it.dueDateIso }
                    .thenBy { priorityRank(it.priority) }
                    .thenBy { it.scheduledTime }
            )
        }

    override fun observeProjects(userId: String): Flow<List<StudyProject>> =
        projectsFlow.map { list -> list.sortedBy { project -> project.dueDateIso } }

    override fun observeFocusSessions(userId: String): Flow<List<FocusSession>> =
        sessionsFlow.map { list -> list.reversed() }

    override fun observeFocusProfiles(userId: String): Flow<List<FocusProfile>> =
        focusProfilesFlow.map { list -> list.reversed() }

    override fun observeHabits(userId: String): Flow<List<StudyHabit>> =
        habitsFlow.map { list -> list.sortedByDescending { it.streakDays } }

    override fun observeCountdowns(userId: String): Flow<List<StudyCountdown>> =
        countdownsFlow.map { list -> list.sortedBy { it.targetDateIso } }

    override suspend fun ensureUserProfileAndSeedIfNew(
        userId: String,
        displayName: String,
        email: String
    ) {
        if (profileFlow.value != null) return

        val today = LocalDate.now()
        val safeName = displayName.trim().ifEmpty { "Testeur" }.take(80)
        profileFlow.value = UserProfile(
            userId = userId,
            displayName = safeName,
            email = email.ifBlank { "mode.test@local.light" },
            major = "Génie Logiciel · 3ᵉ Année",
            selectedGradientId = "aurore_lavande",
            dailyGoalTasks = 5L,
            streakDays = 1L,
            lastStudyDate = today.toString(),
            gender = UserGender.FEMININE.key,
            birthYear = 2005L,
            age = 21L,
            profession = UserProfession.STUDENT.key,
            customProfession = "",
            notificationsEnabled = true,
            notificationFrequency = NotificationFrequency.DAILY.key,
            onboardingCompleted = false
        )

        if (coursesFlow.value.isEmpty()) {
            coursesFlow.value = listOf(
                Course("gl301", userId, "GL-301", "Architecture Logicielle & Design Patterns", "Prof. Laurent", 0L, 1440L, 620L, 16.5, iconKey = "code"),
                Course("gl302", userId, "GL-302", "Systèmes Distribués & Cloud", "Prof. Moreau", 1L, 1200L, 480L, 15.2, iconKey = "network"),
                Course("gl303", userId, "GL-303", "Bases de Données Avancées & NoSQL", "Prof. Benali", 2L, 1080L, 540L, 17.0, iconKey = "database"),
                Course("gl304", userId, "GL-304", "Algorithmique & Graphes", "Prof. Chen", 3L, 1320L, 710L, 16.0, iconKey = "math-symbols"),
                Course("gl305", userId, "GL-305", "Génie des Exigences & DevOps CI/CD", "Prof. Dupuis", 4L, 960L, 350L, 15.8, iconKey = "sparkles"),
                Course("gl306", userId, "GL-306", "Systèmes d'Exploitation & Concurrence", "Prof. Girard", 5L, 1200L, 420L, 14.8, iconKey = "device-laptop")
            )
        }

        if (focusProfilesFlow.value.isEmpty()) {
            focusProfilesFlow.value = listOf(
                FocusProfile(
                    id = "fp1",
                    userId = userId,
                    title = "Deep Code & Architecture",
                    learningTopic = "Maîtriser Kotlin, Clean Architecture & Graphes",
                    iconKey = "code",
                    colorIndex = 0L,
                    defaultMinutes = 45L,
                    totalFocusMinutes = 50L
                ),
                FocusProfile(
                    id = "fp2",
                    userId = userId,
                    title = "Apprentissage Libre",
                    learningTopic = "Explorer un nouveau sujet à mon rythme",
                    iconKey = "brain",
                    colorIndex = 3L,
                    defaultMinutes = 25L,
                    totalFocusMinutes = 0L
                )
            )
        }

        if (tasksFlow.value.isEmpty()) {
            tasksFlow.value = listOf(
                StudyTask("t1", userId, "gl301", "Architecture Logicielle & Design Patterns", "Implémenter Observer & Strategy en Kotlin", "TP Clean Architecture + tests unitaires", today.toString(), "09:00", "high", TaskCategory.PROJECT.key, 50L, 0L, false, false, focusProfileId = "fp1", focusProfileTitle = "Deep Code & Architecture"),
                StudyTask("t2", userId, "gl302", "Systèmes Distribués & Cloud", "Revoir Consensus Raft & horloges de Lamport", "Fiches de synthèse chapitre 4", today.toString(), "11:15", "high", TaskCategory.EXAM.key, 45L, 0L, false, false),
                StudyTask("t3", userId, "gl304", "Algorithmique & Graphes", "Exercices Dijkstra & Bellman-Ford", "Série TD n°5 — complexité O(E log V)", today.toString(), "14:30", "medium", TaskCategory.ASSIGNMENT.key, 40L, 0L, true, false, focusProfileId = "fp1", focusProfileTitle = "Deep Code & Architecture"),
                StudyTask("t4", userId, "gl303", "Bases de Données Avancées & NoSQL", "Modélisation agrégats & indexation", "Schéma multi-tenant + règles sécurité", today.plusDays(1).toString(), "10:00", "medium", TaskCategory.PROJECT.key, 45L, 0L, false, false),
                StudyTask("t5", userId, "gl305", "Génie des Exigences & DevOps CI/CD", "Configurer pipeline GitHub Actions & lint", "Projet intégrateur S5", today.plusDays(2).toString(), "16:00", "low", TaskCategory.ASSIGNMENT.key, 30L, 0L, false, false)
            )
        }

        if (sessionsFlow.value.isEmpty()) {
            sessionsFlow.value = listOf(
                FocusSession("s1", userId, "gl304", "Algorithmique & Graphes", "Exercices Dijkstra & Bellman-Ford", 50L, "deep_code", today.minusDays(2).toString(), focusProfileId = "fp1", focusProfileTitle = "Deep Code & Architecture", taskId = "t3"),
                FocusSession("s2", userId, "gl301", "Architecture Logicielle & Design Patterns", "Diagrammes UML & SOLID", 25L, "pomodoro", today.minusDays(1).toString()),
                FocusSession("s3", userId, "gl303", "Bases de Données Avancées & NoSQL", "Requêtes d'agrégation & transactions", 50L, "deep_code", today.toString())
            )
        }

        if (habitsFlow.value.isEmpty()) {
            habitsFlow.value = listOf(
                StudyHabit(
                    id = "h1",
                    userId = userId,
                    title = "Levé tôt",
                    subtitle = "Levez-vous et soyez incroyable",
                    category = HabitCategory.HEALTH.key,
                    iconKey = "sun",
                    colorIndex = 2L,
                    streakDays = 2L,
                    completedDates = listOf(today.minusDays(2).toString(), today.minusDays(1).toString())
                ),
                StudyHabit(
                    id = "h2",
                    userId = userId,
                    title = "Prendre soin de ses yeux",
                    subtitle = "Pause 20-20-20 loin des écrans",
                    category = HabitCategory.HEALTH.key,
                    iconKey = "eye",
                    colorIndex = 3L,
                    streakDays = 1L,
                    completedDates = listOf(today.minusDays(1).toString())
                ),
                StudyHabit(
                    id = "h3",
                    userId = userId,
                    title = "Apprendre de nouvelles choses",
                    subtitle = "Petit nombre, grand résultat",
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
        }

        if (countdownsFlow.value.isEmpty()) {
            countdownsFlow.value = listOf(
                StudyCountdown(
                    id = "c1",
                    userId = userId,
                    title = "Partiel Systèmes Distribués",
                    notes = "Consensus Raft, horloges logiques & gRPC",
                    targetDateIso = today.plusDays(14).toString(),
                    category = CountdownCategory.EXAM.key,
                    colorIndex = 4L
                ),
                StudyCountdown(
                    id = "c2",
                    userId = userId,
                    title = "Soutenance Projet",
                    notes = "Démo Clean Architecture & couverture CI/CD",
                    targetDateIso = today.plusDays(28).toString(),
                    category = CountdownCategory.PROJECT.key,
                    colorIndex = 0L
                )
            )
        }
        persistToDisk()
    }

    override suspend fun updateSelectedGradient(userId: String, gradientId: String) {
        profileFlow.update { current ->
            current?.copy(selectedGradientId = gradientId)
        }
        markModifiedAndPersist()
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
        profileFlow.update { current ->
            current?.copy(
                displayName = displayName.trim().ifBlank { "Utilisateur" }.take(80),
                major = major.trim().ifBlank { "Organisation" }.take(80),
                dailyGoalTasks = dailyGoalTasks.coerceIn(1L, 50L),
                gender = gender?.let { UserGender.fromKey(it).key } ?: current.gender,
                birthYear = birthYear?.coerceIn(1930L, 2020L) ?: current.birthYear,
                age = age?.coerceIn(6L, 100L) ?: current.age,
                profession = profession?.let { UserProfession.fromKey(it).key } ?: current.profession,
                customProfession = customProfession?.trim()?.take(80) ?: current.customProfession
            )
        }
        markModifiedAndPersist()
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
        val safeMajor = major.trim().ifBlank { profession.defaultFocusDomain() }.take(80)
        val safeCustom = if (profession == UserProfession.OTHER) customProfession.trim().take(80) else ""
        val safeName = displayName.trim().ifBlank { profession.labelFor(gender, safeCustom) }.take(80)
        val safeBirthYear = birthYear.coerceIn(1930L, 2020L)
        val safeAge = age.coerceIn(6L, 100L)

        profileFlow.update { current ->
            (current ?: UserProfile(userId = userId)).copy(
                displayName = safeName,
                gender = gender.key,
                birthYear = safeBirthYear,
                age = safeAge,
                profession = profession.key,
                customProfession = safeCustom,
                major = safeMajor,
                selectedGradientId = selectedGradientId.trim().ifBlank { "aurore_lavande" }.take(40),
                dailyGoalTasks = dailyGoalTasks.coerceIn(1L, 50L),
                notificationsEnabled = notificationsEnabled,
                notificationFrequency = notificationFrequency.key,
                onboardingCompleted = true
            )
        }

        if (selectedHabits.isNotEmpty()) {
            habitsFlow.update { existing ->
                val existingTitles = existing.map { it.title.trim().lowercase() }.toHashSet()
                val added = selectedHabits
                    .filter { !existingTitles.contains(it.title.trim().lowercase()) }
                    .map { h ->
                        StudyHabit(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            title = h.title.trim().take(80),
                            subtitle = h.subtitle.trim().take(140),
                            category = h.category.key,
                            iconKey = h.iconKey.take(30),
                            colorIndex = h.colorIndex.coerceIn(0L, 15L),
                            streakDays = 0L,
                            completedDates = emptyList()
                        )
                    }
                existing + added
            }
        }

        if (adaptModulesToProfession) {
            coursesFlow.update { existing ->
                if (existing.all { it.id in STARTER_COURSE_IDS }) {
                    adaptiveStarterModulesFor(profession, safeCustom).map { mod ->
                        Course(
                            id = mod.id,
                            userId = userId,
                            code = mod.code,
                            title = mod.title,
                            professor = mod.lead,
                            colorIndex = mod.colorIndex,
                            targetMinutes = mod.targetMinutes,
                            completedMinutes = 0L,
                            grade = 15.0,
                            iconKey = mod.iconKey
                        )
                    }
                } else {
                    existing
                }
            }
        }
        markModifiedAndPersist()
    }

    override suspend fun updateNotificationSettings(
        userId: String,
        enabled: Boolean,
        frequency: NotificationFrequency
    ) {
        profileFlow.update { current ->
            current?.copy(
                notificationsEnabled = enabled,
                notificationFrequency = frequency.key
            )
        }
        markModifiedAndPersist()
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
        val id = UUID.randomUUID().toString()
        val course = Course(
            id = id,
            userId = userId,
            code = code.trim().take(20),
            title = title.trim().take(100),
            professor = professor.trim().take(80),
            colorIndex = colorIndex.coerceIn(0L, 15L),
            targetMinutes = targetMinutes.coerceIn(30L, 60000L),
            completedMinutes = 0L,
            grade = grade.coerceIn(0.0, 20.0),
            iconKey = iconKey.trim().ifEmpty { "book" }.take(40)
        )
        coursesFlow.update { it + course }
        markModifiedAndPersist()
        return id
    }

    override suspend fun updateCourseGrade(userId: String, courseId: String, newGrade: Double) {
        coursesFlow.update { list ->
            list.map { c ->
                if (c.id == courseId) c.copy(grade = newGrade.coerceIn(0.0, 20.0)) else c
            }
        }
        markModifiedAndPersist()
    }

    override suspend fun deleteCourse(userId: String, courseId: String) {
        coursesFlow.update { list -> list.filterNot { it.id == courseId } }
        markModifiedAndPersist()
    }

    override suspend fun createProject(
        userId: String,
        title: String,
        description: String,
        startDateIso: String,
        dueDateIso: String
    ): String {
        val start = LocalDate.parse(startDateIso)
        val due = LocalDate.parse(dueDateIso)
        require(title.isNotBlank() && !due.isBefore(start))
        val project = StudyProject(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title.trim().take(120),
            description = description.trim().take(500),
            startDateIso = start.toString(),
            dueDateIso = due.toString()
        )
        projectsFlow.update { it + project }
        markModifiedAndPersist()
        return project.id
    }

    override suspend fun updateProject(project: StudyProject) {
        val start = LocalDate.parse(project.startDateIso)
        val due = LocalDate.parse(project.dueDateIso)
        require(project.title.isNotBlank() && !due.isBefore(start) && project.status in setOf("active", "on_hold", "completed"))
        projectsFlow.update { list -> list.map { if (it.id == project.id) project else it } }
        markModifiedAndPersist()
    }

    override suspend fun deleteProject(userId: String, projectId: String) {
        projectsFlow.update { it.filterNot { project -> project.id == projectId } }
        tasksFlow.update { tasks -> tasks.map { if (it.projectId == projectId) it.copy(projectId = "") else it } }
        markModifiedAndPersist()
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
        focusProfileTitle: String,
        subtasks: List<String>
    ): String {
        val project = projectsFlow.value.firstOrNull { it.id == projectId }
        require(projectId.isBlank() || project != null) { "Le projet sélectionné n'existe plus." }
        val resolvedProfile = focusProfilesFlow.value.firstOrNull { it.id == focusProfileId }
        val id = UUID.randomUUID().toString()
        val safePriority = if (priority in listOf("high", "medium", "low")) priority else "medium"
        val task = StudyTask(
            id = id,
            userId = userId,
            courseId = courseId.take(128),
            courseTitle = courseTitle.take(100),
            title = title.trim().take(140),
            notes = notes.trim().take(500),
            subtasks = normalizeSubtasks(subtasks),
            dueDateIso = dueDateIso.trim().take(20),
            startDateIso = startDateIso.trim().take(20).ifBlank { dueDateIso.trim().take(20) },
            projectId = projectId,
            focusProfileId = focusProfileId.trim().take(128),
            focusProfileTitle = (resolvedProfile?.title ?: focusProfileTitle).trim().take(100),
            scheduledTime = scheduledTime.trim().take(20),
            priority = safePriority,
            category = TaskCategory.sanitizeKey(category),
            estimatedMinutes = estimatedMinutes.coerceIn(5L, 600L),
            quadrant = quadrant?.takeIf { it in 1L..4L } ?: 0L,
            completed = false,
            aiGenerated = aiGenerated
        )
        tasksFlow.update { it + task }
        markModifiedAndPersist()
        return id
    }

    override suspend fun createTasksBatch(userId: String, tasks: List<StudyTask>) {
        if (tasks.isEmpty()) return
        val newTasks = tasks.map { task ->
            val safePriority = if (task.priority in listOf("high", "medium", "low")) task.priority else "medium"
            task.copy(
                id = UUID.randomUUID().toString(),
                userId = userId,
                priority = safePriority,
                category = TaskCategory.sanitizeKey(task.category),
                estimatedMinutes = task.estimatedMinutes.coerceIn(5L, 600L),
                startDateIso = task.startDateIso.ifBlank { task.dueDateIso },
                completed = false
            )
        }
        tasksFlow.update { it + newTasks }
        markModifiedAndPersist()
    }

    override suspend fun toggleTaskCompleted(userId: String, task: StudyTask) {
        tasksFlow.update { list ->
            list.map { item -> if (item.id == task.id) item.copy(completed = !item.completed) else item }
        }
        markModifiedAndPersist()
    }

    override suspend fun moveTask(userId: String, taskId: String, start: String, due: String) {
        tasksFlow.update { tasks -> tasks.map { if (it.id == taskId && it.userId == userId) it.copy(startDateIso = start, dueDateIso = due) else it } }
        markModifiedAndPersist()
    }

    override suspend fun updateTaskQuadrant(userId: String, taskId: String, quadrant: Long) {
        val safeQuadrant = quadrant.coerceIn(1L, 4L)
        tasksFlow.update { list ->
            list.map { item -> if (item.id == taskId) item.copy(quadrant = safeQuadrant) else item }
        }
        markModifiedAndPersist()
    }

    override suspend fun assignTaskToFocusProfile(
        userId: String,
        taskId: String,
        focusProfileId: String,
        focusProfileTitle: String
    ) {
        val safeProfileId = focusProfileId.trim().take(128)
        val safeProfileTitle = if (safeProfileId.isBlank()) "" else {
            focusProfilesFlow.value.firstOrNull { it.id == safeProfileId }?.title ?: focusProfileTitle.trim().take(100)
        }
        tasksFlow.update { list ->
            list.map { item ->
                if (item.id == taskId) {
                    item.copy(focusProfileId = safeProfileId, focusProfileTitle = safeProfileTitle)
                } else item
            }
        }
        markModifiedAndPersist()
    }

    override suspend fun deleteTask(userId: String, taskId: String) {
        tasksFlow.update { list -> list.filterNot { it.id == taskId } }
        markModifiedAndPersist()
    }

    override suspend fun createFocusProfile(
        userId: String,
        title: String,
        learningTopic: String,
        iconKey: String,
        colorIndex: Long,
        defaultMinutes: Long
    ): String {
        val safeTitle = title.trim().take(100)
        require(safeTitle.isNotBlank()) { "Le nom du profil de focus est obligatoire." }
        val id = UUID.randomUUID().toString()
        val profile = FocusProfile(
            id = id,
            userId = userId,
            title = safeTitle,
            learningTopic = learningTopic.trim().take(220),
            iconKey = iconKey.trim().ifEmpty { "brain" }.take(40),
            colorIndex = colorIndex.coerceIn(0L, 15L),
            defaultMinutes = defaultMinutes.coerceIn(5L, 180L),
            totalFocusMinutes = 0L
        )
        focusProfilesFlow.update { it + profile }
        markModifiedAndPersist()
        return id
    }

    override suspend fun deleteFocusProfile(userId: String, profileId: String) {
        focusProfilesFlow.update { list -> list.filterNot { it.id == profileId } }
        tasksFlow.update { tasks ->
            tasks.map { task ->
                if (task.focusProfileId == profileId) {
                    task.copy(focusProfileId = "", focusProfileTitle = "")
                } else task
            }
        }
        markModifiedAndPersist()
    }

    override suspend fun createHabit(
        userId: String,
        title: String,
        subtitle: String,
        category: String,
        iconKey: String,
        colorIndex: Long
    ): String {
        val id = UUID.randomUUID().toString()
        val habit = StudyHabit(
            id = id,
            userId = userId,
            title = title.trim().take(80),
            subtitle = subtitle.trim().take(140),
            category = HabitCategory.sanitizeKey(category),
            iconKey = iconKey.trim().ifEmpty { "sparkles" }.take(30),
            colorIndex = colorIndex.coerceIn(0L, 15L),
            streakDays = 0L,
            completedDates = emptyList()
        )
        habitsFlow.update { it + habit }
        markModifiedAndPersist()
        return id
    }

    override suspend fun toggleHabitForDate(userId: String, habit: StudyHabit, dateIso: String) {
        habitsFlow.update { list ->
            list.map { current ->
                if (current.id != habit.id) {
                    current
                } else {
                    val updatedDates = if (current.completedDates.contains(dateIso)) {
                        current.completedDates.filterNot { it == dateIso }
                    } else {
                        (current.completedDates + dateIso).distinct().sorted().takeLast(365)
                    }
                    val streak = computeHabitStreak(updatedDates, LocalDate.now())
                    current.copy(completedDates = updatedDates, streakDays = streak)
                }
            }
        }
        markModifiedAndPersist()
    }

    override suspend fun deleteHabit(userId: String, habitId: String) {
        habitsFlow.update { list -> list.filterNot { it.id == habitId } }
        markModifiedAndPersist()
    }

    override suspend fun createCountdown(
        userId: String,
        title: String,
        notes: String,
        targetDateIso: String,
        category: String,
        colorIndex: Long
    ): String {
        val id = UUID.randomUUID().toString()
        val safeDate = targetDateIso.trim().take(20).ifBlank { LocalDate.now().plusDays(7).toString() }
        val countdown = StudyCountdown(
            id = id,
            userId = userId,
            title = title.trim().take(100),
            notes = notes.trim().take(300),
            targetDateIso = safeDate,
            category = CountdownCategory.sanitizeKey(category),
            colorIndex = colorIndex.coerceIn(0L, 15L)
        )
        countdownsFlow.update { it + countdown }
        markModifiedAndPersist()
        return id
    }

    override suspend fun deleteCountdown(userId: String, countdownId: String) {
        countdownsFlow.update { list -> list.filterNot { it.id == countdownId } }
        markModifiedAndPersist()
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
        val id = UUID.randomUUID().toString()
        val safeMode = if (mode in listOf("pomodoro", "deep_code", "short_break", "stopwatch")) mode else "pomodoro"
        val safeDuration = durationMinutes.coerceIn(1L, 240L)
        val resolvedProfile = focusProfilesFlow.value.firstOrNull { it.id == focusProfileId }
        val safeProfileId = focusProfileId.trim().take(128)
        val safeProfileTitle = (resolvedProfile?.title ?: focusProfileTitle).trim().take(100)
        val session = FocusSession(
            id = id,
            userId = userId,
            courseId = courseId.take(128),
            courseTitle = courseTitle.take(100),
            taskTitle = taskTitle.trim().take(140),
            durationMinutes = safeDuration,
            mode = safeMode,
            dateIso = dateIso.take(20),
            focusProfileId = safeProfileId,
            focusProfileTitle = safeProfileTitle,
            taskId = taskId.trim().take(128)
        )
        sessionsFlow.update { it + session }

        if (courseId.isNotBlank() && safeMode != "short_break") {
            coursesFlow.update { list ->
                list.map { c ->
                    if (c.id == courseId) c.copy(completedMinutes = c.completedMinutes + safeDuration) else c
                }
            }
        }
        if (safeProfileId.isNotBlank() && safeMode != "short_break") {
            focusProfilesFlow.update { list ->
                list.map { p ->
                    if (p.id == safeProfileId) p.copy(totalFocusMinutes = p.totalFocusMinutes + safeDuration) else p
                }
            }
        }
        if (safeMode != "short_break") {
            profileFlow.update { current ->
                if (current == null) null else {
                    val today = LocalDate.now()
                    val todayStr = today.toString()
                    val yesterdayStr = today.minusDays(1).toString()
                    val nextStreak = when (current.lastStudyDate) {
                        todayStr -> maxOf(1L, current.streakDays)
                        yesterdayStr -> current.streakDays + 1L
                        else -> 1L
                    }
                    current.copy(streakDays = nextStreak, lastStudyDate = todayStr)
                }
            }
        }
        markModifiedAndPersist()
        return id
    }

    /**
     * Synchronizes locally created/modified data (from offline / Test Mode) into Cloud Firestore
     * once per cloud account without duplicating starter items or overwriting a completed cloud profile.
     */
    suspend fun syncToCloudIfNeeded(
        cloudRepo: StudyWorkspaceRepository,
        cloudUserId: String,
        displayName: String = "",
        email: String = ""
    ): Boolean = cloudSyncMutex.withLock {
        if (cloudUserId.isBlank()) return false
        if (!hasUserModifications || cloudUserId in syncedCloudUserIds) return false

        val localProfile = profileFlow.value ?: return false
        cloudRepo.ensureUserProfileAndSeedIfNew(
            userId = cloudUserId,
            displayName = displayName.ifBlank { localProfile.displayName },
            email = email.ifBlank { localProfile.email }
        )

        val remoteProfile = runCatching { cloudRepo.observeUserProfile(cloudUserId).first() }.getOrNull()
        val remoteCourses = runCatching { cloudRepo.observeCourses(cloudUserId).first() }.getOrDefault(emptyList()).toMutableList()
        val remoteFocusProfiles = runCatching { cloudRepo.observeFocusProfiles(cloudUserId).first() }.getOrDefault(emptyList()).toMutableList()
        val remoteProjects = runCatching { cloudRepo.observeProjects(cloudUserId).first() }.getOrDefault(emptyList())
        val remoteTasks = runCatching { cloudRepo.observeTasks(cloudUserId).first() }.getOrDefault(emptyList())
        val remoteHabits = runCatching { cloudRepo.observeHabits(cloudUserId).first() }.getOrDefault(emptyList())
        val remoteCountdowns = runCatching { cloudRepo.observeCountdowns(cloudUserId).first() }.getOrDefault(emptyList())
        val remoteSessions = runCatching { cloudRepo.observeFocusSessions(cloudUserId).first() }.getOrDefault(emptyList())

        var syncedAnything = false

        if (localProfile.onboardingCompleted && remoteProfile?.onboardingCompleted != true) {
            cloudRepo.saveOnboardingConfiguration(
                userId = cloudUserId,
                displayName = localProfile.displayName,
                gender = UserGender.fromKey(localProfile.gender),
                birthYear = localProfile.birthYear,
                age = localProfile.age,
                profession = UserProfession.fromKey(localProfile.profession),
                customProfession = localProfile.customProfession,
                major = localProfile.major,
                selectedGradientId = localProfile.selectedGradientId,
                dailyGoalTasks = localProfile.dailyGoalTasks,
                notificationsEnabled = localProfile.notificationsEnabled,
                notificationFrequency = NotificationFrequency.fromKey(localProfile.notificationFrequency),
                selectedHabits = emptyList(),
                adaptModulesToProfession = coursesFlow.value.all { it.id in STARTER_COURSE_IDS }
            )
            syncedAnything = true
        }

        val courseIdMap = mutableMapOf<String, String>()
        coursesFlow.value.forEach { localCourse ->
            val existingRemote = remoteCourses.firstOrNull {
                it.id == localCourse.id ||
                    (it.code.equals(localCourse.code, ignoreCase = true) && it.title.equals(localCourse.title, ignoreCase = true))
            }
            if (existingRemote != null) {
                courseIdMap[localCourse.id] = existingRemote.id
            } else if (localCourse.id !in STARTER_COURSE_IDS) {
                val createdId = cloudRepo.createCourse(
                    userId = cloudUserId,
                    code = localCourse.code,
                    title = localCourse.title,
                    professor = localCourse.professor,
                    colorIndex = localCourse.colorIndex,
                    targetMinutes = localCourse.targetMinutes,
                    grade = localCourse.grade,
                    iconKey = localCourse.iconKey
                )
                courseIdMap[localCourse.id] = createdId
                remoteCourses += localCourse.copy(id = createdId, userId = cloudUserId)
                syncedAnything = true
            }
        }

        val projectIdMap = mutableMapOf<String, String>()
        projectsFlow.value.forEach { localProject ->
            val existingRemote = remoteProjects.firstOrNull {
                it.title.equals(localProject.title, ignoreCase = true) &&
                    it.startDateIso == localProject.startDateIso &&
                    it.dueDateIso == localProject.dueDateIso
            }
            if (existingRemote != null) {
                projectIdMap[localProject.id] = existingRemote.id
            } else {
                val createdId = cloudRepo.createProject(
                    userId = cloudUserId,
                    title = localProject.title,
                    description = localProject.description,
                    startDateIso = localProject.startDateIso,
                    dueDateIso = localProject.dueDateIso
                )
                if (localProject.status != "active") {
                    cloudRepo.updateProject(localProject.copy(id = createdId, userId = cloudUserId))
                }
                projectIdMap[localProject.id] = createdId
                syncedAnything = true
            }
        }

        val focusProfileIdMap = mutableMapOf<String, String>()
        focusProfilesFlow.value.forEach { localFp ->
            val existingRemote = remoteFocusProfiles.firstOrNull {
                it.id == localFp.id || it.title.equals(localFp.title, ignoreCase = true)
            }
            if (existingRemote != null) {
                focusProfileIdMap[localFp.id] = existingRemote.id
            } else if (localFp.id !in STARTER_FOCUS_PROFILE_IDS) {
                val createdId = cloudRepo.createFocusProfile(
                    userId = cloudUserId,
                    title = localFp.title,
                    learningTopic = localFp.learningTopic,
                    iconKey = localFp.iconKey,
                    colorIndex = localFp.colorIndex,
                    defaultMinutes = localFp.defaultMinutes
                )
                focusProfileIdMap[localFp.id] = createdId
                remoteFocusProfiles += localFp.copy(id = createdId, userId = cloudUserId)
                syncedAnything = true
            }
        }

        val remoteTaskKeys = remoteTasks.map { "${it.title.trim().lowercase()}|${it.dueDateIso}" }.toHashSet()
        tasksFlow.value.forEach { localTask ->
            val key = "${localTask.title.trim().lowercase()}|${localTask.dueDateIso}"
            val isStarterTask = localTask.id in STARTER_TASK_IDS
            if (!isStarterTask && !remoteTaskKeys.contains(key)) {
                val mappedProjectId = if (localTask.projectId.isNotBlank()) {
                    projectIdMap[localTask.projectId].orEmpty()
                } else ""
                val mappedCourseId = if (localTask.courseId.isNotBlank()) {
                    courseIdMap[localTask.courseId].orEmpty()
                } else ""
                val mappedFocusProfileId = if (localTask.focusProfileId.isNotBlank()) {
                    focusProfileIdMap[localTask.focusProfileId].orEmpty()
                } else ""
                val createdId = cloudRepo.createTask(
                    userId = cloudUserId,
                    courseId = mappedCourseId,
                    courseTitle = localTask.courseTitle,
                    title = localTask.title,
                    notes = localTask.notes,
                    dueDateIso = localTask.dueDateIso,
                    scheduledTime = localTask.scheduledTime,
                    priority = localTask.priority,
                    category = localTask.category,
                    estimatedMinutes = localTask.estimatedMinutes,
                    aiGenerated = localTask.aiGenerated,
                    quadrant = localTask.quadrant.takeIf { it in 1L..4L },
                    startDateIso = localTask.startDateIso,
                    projectId = mappedProjectId,
                    focusProfileId = mappedFocusProfileId,
                    focusProfileTitle = localTask.focusProfileTitle,
                    subtasks = localTask.subtasks
                )
                if (localTask.completed) {
                    cloudRepo.toggleTaskCompleted(cloudUserId, localTask.copy(id = createdId, completed = false))
                }
                remoteTaskKeys += key
                syncedAnything = true
            }
        }

        val remoteHabitKeys = remoteHabits.map { it.title.trim().lowercase() }.toHashSet()
        habitsFlow.value.forEach { localHabit ->
            val key = localHabit.title.trim().lowercase()
            if (localHabit.id !in STARTER_HABIT_IDS && !remoteHabitKeys.contains(key)) {
                val createdId = cloudRepo.createHabit(
                    userId = cloudUserId,
                    title = localHabit.title,
                    subtitle = localHabit.subtitle,
                    category = localHabit.category,
                    iconKey = localHabit.iconKey,
                    colorIndex = localHabit.colorIndex
                )
                if (localHabit.completedDates.isNotEmpty()) {
                    var runningHabit = localHabit.copy(id = createdId, userId = cloudUserId, completedDates = emptyList(), streakDays = 0L)
                    localHabit.completedDates.forEach { dateIso ->
                        cloudRepo.toggleHabitForDate(cloudUserId, runningHabit, dateIso)
                        runningHabit = runningHabit.copy(completedDates = runningHabit.completedDates + dateIso)
                    }
                }
                remoteHabitKeys += key
                syncedAnything = true
            }
        }

        val remoteCountdownKeys = remoteCountdowns.map { "${it.title.trim().lowercase()}|${it.targetDateIso}" }.toHashSet()
        countdownsFlow.value.forEach { localCountdown ->
            val key = "${localCountdown.title.trim().lowercase()}|${localCountdown.targetDateIso}"
            if (localCountdown.id !in STARTER_COUNTDOWN_IDS && !remoteCountdownKeys.contains(key)) {
                cloudRepo.createCountdown(
                    userId = cloudUserId,
                    title = localCountdown.title,
                    notes = localCountdown.notes,
                    targetDateIso = localCountdown.targetDateIso,
                    category = localCountdown.category,
                    colorIndex = localCountdown.colorIndex
                )
                remoteCountdownKeys += key
                syncedAnything = true
            }
        }

        val remoteSessionKeys = remoteSessions.map {
            "${it.taskTitle.trim().lowercase()}|${it.dateIso}|${it.durationMinutes}|${it.mode}"
        }.toHashSet()
        sessionsFlow.value.forEach { localSession ->
            val key = "${localSession.taskTitle.trim().lowercase()}|${localSession.dateIso}|${localSession.durationMinutes}|${localSession.mode}"
            if (localSession.id !in STARTER_SESSION_IDS && !remoteSessionKeys.contains(key)) {
                cloudRepo.logFocusSession(
                    userId = cloudUserId,
                    courseId = courseIdMap[localSession.courseId] ?: localSession.courseId,
                    courseTitle = localSession.courseTitle,
                    taskTitle = localSession.taskTitle,
                    durationMinutes = localSession.durationMinutes,
                    mode = localSession.mode,
                    dateIso = localSession.dateIso,
                    focusProfileId = focusProfileIdMap[localSession.focusProfileId] ?: localSession.focusProfileId,
                    focusProfileTitle = localSession.focusProfileTitle,
                    taskId = localSession.taskId
                )
                remoteSessionKeys += key
                syncedAnything = true
            }
        }

        syncedCloudUserIds += cloudUserId
        persistToDisk()
        syncedAnything
    }

    private fun markModifiedAndPersist() {
        hasUserModifications = true
        persistToDisk()
    }

    private fun persistToDisk() {
        val file = storageFile ?: return
        synchronized(persistLock) {
            runCatching {
                file.parentFile?.mkdirs()
                val jsonText = encodeWorkspaceSnapshot().toString()
                val tempFile = File(file.parentFile, "${file.name}.tmp")
                tempFile.writeText(jsonText, Charsets.UTF_8)
                if (!tempFile.renameTo(file)) {
                    file.writeText(jsonText, Charsets.UTF_8)
                    tempFile.delete()
                }
            }.onFailure { err ->
                Log.w(TAG, "Unable to persist local workspace snapshot", err)
            }
        }
    }

    private fun loadFromDisk() {
        val file = storageFile ?: return
        if (!file.exists()) return
        synchronized(persistLock) {
            runCatching {
                val root = Json.parseToJsonElement(file.readText(Charsets.UTF_8)).jsonObject
                hasUserModifications = root["hasUserModifications"]?.jsonPrimitive?.booleanOrNull ?: false
                syncedCloudUserIds.clear()
                root["syncedCloudUserIds"]?.jsonArray?.forEach { elem ->
                    elem.jsonPrimitive.contentOrNull?.takeIf { it.isNotBlank() }?.let { syncedCloudUserIds += it }
                }
                profileFlow.value = root["profile"]?.let { elem ->
                    (elem as? JsonObject)?.let(::decodeProfile)
                }
                coursesFlow.value = root["courses"]?.jsonArray?.mapNotNull { elem ->
                    (elem as? JsonObject)?.let(::decodeCourse)
                }.orEmpty()
                projectsFlow.value = root["projects"]?.jsonArray?.mapNotNull { elem ->
                    (elem as? JsonObject)?.let(::decodeProject)
                }.orEmpty()
                tasksFlow.value = root["tasks"]?.jsonArray?.mapNotNull { elem ->
                    (elem as? JsonObject)?.let(::decodeTask)
                }.orEmpty()
                sessionsFlow.value = root["sessions"]?.jsonArray?.mapNotNull { elem ->
                    (elem as? JsonObject)?.let(::decodeSession)
                }.orEmpty()
                focusProfilesFlow.value = root["focusProfiles"]?.jsonArray?.mapNotNull { elem ->
                    (elem as? JsonObject)?.let(::decodeFocusProfile)
                }.orEmpty()
                habitsFlow.value = root["habits"]?.jsonArray?.mapNotNull { elem ->
                    (elem as? JsonObject)?.let(::decodeHabit)
                }.orEmpty()
                countdownsFlow.value = root["countdowns"]?.jsonArray?.mapNotNull { elem ->
                    (elem as? JsonObject)?.let(::decodeCountdown)
                }.orEmpty()
            }.onFailure { err ->
                Log.w(TAG, "Corrupted local workspace snapshot ignored", err)
            }
        }
    }

    private fun encodeWorkspaceSnapshot(): JsonObject = buildJsonObject {
        put("version", 1)
        put("hasUserModifications", hasUserModifications)
        put("syncedCloudUserIds", buildJsonArray {
            syncedCloudUserIds.forEach { add(JsonPrimitive(it)) }
        })
        profileFlow.value?.let { put("profile", encodeProfile(it)) }
        put("courses", buildJsonArray { coursesFlow.value.forEach { add(encodeCourse(it)) } })
        put("projects", buildJsonArray { projectsFlow.value.forEach { add(encodeProject(it)) } })
        put("tasks", buildJsonArray { tasksFlow.value.forEach { add(encodeTask(it)) } })
        put("sessions", buildJsonArray { sessionsFlow.value.forEach { add(encodeSession(it)) } })
        put("focusProfiles", buildJsonArray { focusProfilesFlow.value.forEach { add(encodeFocusProfile(it)) } })
        put("habits", buildJsonArray { habitsFlow.value.forEach { add(encodeHabit(it)) } })
        put("countdowns", buildJsonArray { countdownsFlow.value.forEach { add(encodeCountdown(it)) } })
    }

    private fun encodeProfile(p: UserProfile): JsonObject = buildJsonObject {
        put("userId", p.userId)
        put("displayName", p.displayName)
        put("email", p.email)
        put("major", p.major)
        put("selectedGradientId", p.selectedGradientId)
        put("dailyGoalTasks", p.dailyGoalTasks)
        put("streakDays", p.streakDays)
        put("lastStudyDate", p.lastStudyDate)
        put("gender", p.gender)
        put("birthYear", p.birthYear)
        put("age", p.age)
        put("profession", p.profession)
        put("customProfession", p.customProfession)
        put("notificationsEnabled", p.notificationsEnabled)
        put("notificationFrequency", p.notificationFrequency)
        put("onboardingCompleted", p.onboardingCompleted)
    }

    private fun decodeProfile(obj: JsonObject): UserProfile = UserProfile(
        id = obj["userId"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        userId = obj["userId"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        displayName = obj["displayName"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        email = obj["email"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        major = obj["major"]?.jsonPrimitive?.contentOrNull ?: "Génie Logiciel · 3ᵉ Année",
        selectedGradientId = obj["selectedGradientId"]?.jsonPrimitive?.contentOrNull ?: "aurore_lavande",
        dailyGoalTasks = obj["dailyGoalTasks"]?.jsonPrimitive?.longOrNull ?: 5L,
        streakDays = obj["streakDays"]?.jsonPrimitive?.longOrNull ?: 1L,
        lastStudyDate = obj["lastStudyDate"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        gender = obj["gender"]?.jsonPrimitive?.contentOrNull ?: UserGender.FEMININE.key,
        birthYear = obj["birthYear"]?.jsonPrimitive?.longOrNull ?: 2005L,
        age = obj["age"]?.jsonPrimitive?.longOrNull ?: 21L,
        profession = obj["profession"]?.jsonPrimitive?.contentOrNull ?: UserProfession.STUDENT.key,
        customProfession = obj["customProfession"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        notificationsEnabled = obj["notificationsEnabled"]?.jsonPrimitive?.booleanOrNull ?: true,
        notificationFrequency = obj["notificationFrequency"]?.jsonPrimitive?.contentOrNull ?: NotificationFrequency.DAILY.key,
        onboardingCompleted = obj["onboardingCompleted"]?.jsonPrimitive?.booleanOrNull ?: false
    )

    private fun encodeCourse(c: Course): JsonObject = buildJsonObject {
        put("id", c.id)
        put("userId", c.userId)
        put("code", c.code)
        put("title", c.title)
        put("professor", c.professor)
        put("colorIndex", c.colorIndex)
        put("targetMinutes", c.targetMinutes)
        put("completedMinutes", c.completedMinutes)
        put("grade", c.grade)
        put("iconKey", c.iconKey)
    }

    private fun decodeCourse(obj: JsonObject): Course = Course(
        id = obj["id"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        userId = obj["userId"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        code = obj["code"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        title = obj["title"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        professor = obj["professor"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        colorIndex = obj["colorIndex"]?.jsonPrimitive?.longOrNull ?: 0L,
        targetMinutes = obj["targetMinutes"]?.jsonPrimitive?.longOrNull ?: 1200L,
        completedMinutes = obj["completedMinutes"]?.jsonPrimitive?.longOrNull ?: 0L,
        grade = obj["grade"]?.jsonPrimitive?.doubleOrNull ?: 15.0,
        iconKey = obj["iconKey"]?.jsonPrimitive?.contentOrNull ?: "book"
    )

    private fun encodeProject(p: StudyProject): JsonObject = buildJsonObject {
        put("id", p.id)
        put("userId", p.userId)
        put("title", p.title)
        put("description", p.description)
        put("startDateIso", p.startDateIso)
        put("dueDateIso", p.dueDateIso)
        put("status", p.status)
    }

    private fun decodeProject(obj: JsonObject): StudyProject = StudyProject(
        id = obj["id"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        userId = obj["userId"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        title = obj["title"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        description = obj["description"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        startDateIso = obj["startDateIso"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        dueDateIso = obj["dueDateIso"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        status = obj["status"]?.jsonPrimitive?.contentOrNull ?: "active"
    )

    private fun encodeTask(t: StudyTask): JsonObject = buildJsonObject {
        put("id", t.id)
        put("userId", t.userId)
        put("courseId", t.courseId)
        put("courseTitle", t.courseTitle)
        put("title", t.title)
        put("notes", t.notes)
        put("subtasks", buildJsonArray { t.subtasks.forEach { add(JsonPrimitive(it)) } })
        put("dueDateIso", t.dueDateIso)
        put("scheduledTime", t.scheduledTime)
        put("priority", t.priority)
        put("category", t.category)
        put("estimatedMinutes", t.estimatedMinutes)
        put("quadrant", t.quadrant)
        put("completed", t.completed)
        put("aiGenerated", t.aiGenerated)
        put("startDateIso", t.startDateIso)
        put("projectId", t.projectId)
        put("focusProfileId", t.focusProfileId)
        put("focusProfileTitle", t.focusProfileTitle)
    }

    private fun decodeTask(obj: JsonObject): StudyTask = StudyTask(
        id = obj["id"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        userId = obj["userId"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        courseId = obj["courseId"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        courseTitle = obj["courseTitle"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        title = obj["title"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        notes = obj["notes"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        subtasks = normalizeSubtasks((obj["subtasks"] as? JsonArray).orEmpty().mapNotNull { (it as? JsonPrimitive)?.contentOrNull }),
        dueDateIso = obj["dueDateIso"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        scheduledTime = obj["scheduledTime"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        priority = obj["priority"]?.jsonPrimitive?.contentOrNull ?: "medium",
        category = obj["category"]?.jsonPrimitive?.contentOrNull ?: TaskCategory.REVISION.key,
        estimatedMinutes = obj["estimatedMinutes"]?.jsonPrimitive?.longOrNull ?: 30L,
        quadrant = obj["quadrant"]?.jsonPrimitive?.longOrNull ?: 0L,
        completed = obj["completed"]?.jsonPrimitive?.booleanOrNull ?: false,
        aiGenerated = obj["aiGenerated"]?.jsonPrimitive?.booleanOrNull ?: false,
        startDateIso = obj["startDateIso"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        projectId = obj["projectId"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        focusProfileId = obj["focusProfileId"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        focusProfileTitle = obj["focusProfileTitle"]?.jsonPrimitive?.contentOrNull.orEmpty()
    )

    private fun encodeFocusProfile(p: FocusProfile): JsonObject = buildJsonObject {
        put("id", p.id)
        put("userId", p.userId)
        put("title", p.title)
        put("learningTopic", p.learningTopic)
        put("iconKey", p.iconKey)
        put("colorIndex", p.colorIndex)
        put("defaultMinutes", p.defaultMinutes)
        put("totalFocusMinutes", p.totalFocusMinutes)
    }

    private fun decodeFocusProfile(obj: JsonObject): FocusProfile = FocusProfile(
        id = obj["id"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        userId = obj["userId"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        title = obj["title"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        learningTopic = obj["learningTopic"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        iconKey = obj["iconKey"]?.jsonPrimitive?.contentOrNull ?: "brain",
        colorIndex = obj["colorIndex"]?.jsonPrimitive?.longOrNull ?: 0L,
        defaultMinutes = obj["defaultMinutes"]?.jsonPrimitive?.longOrNull ?: 25L,
        totalFocusMinutes = obj["totalFocusMinutes"]?.jsonPrimitive?.longOrNull ?: 0L
    )

    private fun encodeSession(s: FocusSession): JsonObject = buildJsonObject {
        put("id", s.id)
        put("userId", s.userId)
        put("courseId", s.courseId)
        put("courseTitle", s.courseTitle)
        put("taskTitle", s.taskTitle)
        put("durationMinutes", s.durationMinutes)
        put("mode", s.mode)
        put("dateIso", s.dateIso)
        put("focusProfileId", s.focusProfileId)
        put("focusProfileTitle", s.focusProfileTitle)
        put("taskId", s.taskId)
    }

    private fun decodeSession(obj: JsonObject): FocusSession = FocusSession(
        id = obj["id"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        userId = obj["userId"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        courseId = obj["courseId"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        courseTitle = obj["courseTitle"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        taskTitle = obj["taskTitle"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        durationMinutes = obj["durationMinutes"]?.jsonPrimitive?.longOrNull ?: 25L,
        mode = obj["mode"]?.jsonPrimitive?.contentOrNull ?: "pomodoro",
        dateIso = obj["dateIso"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        focusProfileId = obj["focusProfileId"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        focusProfileTitle = obj["focusProfileTitle"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        taskId = obj["taskId"]?.jsonPrimitive?.contentOrNull.orEmpty()
    )

    private fun encodeHabit(h: StudyHabit): JsonObject = buildJsonObject {
        put("id", h.id)
        put("userId", h.userId)
        put("title", h.title)
        put("subtitle", h.subtitle)
        put("category", h.category)
        put("iconKey", h.iconKey)
        put("colorIndex", h.colorIndex)
        put("streakDays", h.streakDays)
        put("completedDates", buildJsonArray { h.completedDates.forEach { add(JsonPrimitive(it)) } })
    }

    private fun decodeHabit(obj: JsonObject): StudyHabit = StudyHabit(
        id = obj["id"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        userId = obj["userId"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        title = obj["title"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        subtitle = obj["subtitle"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        category = obj["category"]?.jsonPrimitive?.contentOrNull ?: HabitCategory.STUDY.key,
        iconKey = obj["iconKey"]?.jsonPrimitive?.contentOrNull ?: "sparkles",
        colorIndex = obj["colorIndex"]?.jsonPrimitive?.longOrNull ?: 0L,
        streakDays = obj["streakDays"]?.jsonPrimitive?.longOrNull ?: 0L,
        completedDates = obj["completedDates"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }.orEmpty()
    )

    private fun encodeCountdown(c: StudyCountdown): JsonObject = buildJsonObject {
        put("id", c.id)
        put("userId", c.userId)
        put("title", c.title)
        put("notes", c.notes)
        put("targetDateIso", c.targetDateIso)
        put("category", c.category)
        put("colorIndex", c.colorIndex)
    }

    private fun decodeCountdown(obj: JsonObject): StudyCountdown = StudyCountdown(
        id = obj["id"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        userId = obj["userId"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        title = obj["title"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        notes = obj["notes"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        targetDateIso = obj["targetDateIso"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        category = obj["category"]?.jsonPrimitive?.contentOrNull ?: CountdownCategory.EXAM.key,
        colorIndex = obj["colorIndex"]?.jsonPrimitive?.longOrNull ?: 0L
    )

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

    private fun priorityRank(priority: String): Int = when (priority) {
        "high" -> 0
        "medium" -> 1
        else -> 2
    }

    private companion object {
        const val TAG = "LocalTestStudyRepo"
        val STARTER_COURSE_IDS = setOf(
            "gl301", "gl302", "gl303", "gl304", "gl305", "gl306",
            "ent01", "ent02", "ent03", "ent04", "ent05",
            "auto01", "auto02", "auto03", "auto04",
            "per01", "per02", "per03", "per04",
            "oth01", "oth02", "oth03", "oth04"
        )
        val STARTER_TASK_IDS = setOf("t1", "t2", "t3", "t4", "t5")
        val STARTER_SESSION_IDS = setOf("s1", "s2", "s3")
        val STARTER_FOCUS_PROFILE_IDS = setOf("fp1", "fp2")
        val STARTER_HABIT_IDS = setOf("h1", "h2", "h3")
        val STARTER_COUNTDOWN_IDS = setOf("c1", "c2")
    }
}
