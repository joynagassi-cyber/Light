package com.example.data.repository

import com.example.data.model.AdaptiveHabitTemplate
import com.example.data.model.CountdownCategory
import com.example.data.model.Course
import com.example.data.model.FocusSession
import com.example.data.model.HabitCategory
import com.example.data.model.NotificationFrequency
import com.example.data.model.StudyCountdown
import com.example.data.model.StudyHabit
import com.example.data.model.StudyTask
import com.example.data.model.TaskCategory
import com.example.data.model.UserGender
import com.example.data.model.UserProfession
import com.example.data.model.UserProfile
import com.example.data.model.adaptiveStarterModulesFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.util.UUID

/**
 * In-memory test repository used strictly when Google Sign-In is paused for local testing.
 * Keeps all state reactive in Jetpack Compose without issuing any unauthenticated Firestore requests.
 */
class LocalTestStudyRepository : StudyWorkspaceRepository {

    private val profileFlow = MutableStateFlow<UserProfile?>(null)
    private val coursesFlow = MutableStateFlow<List<Course>>(emptyList())
    private val tasksFlow = MutableStateFlow<List<StudyTask>>(emptyList())
    private val sessionsFlow = MutableStateFlow<List<FocusSession>>(emptyList())
    private val habitsFlow = MutableStateFlow<List<StudyHabit>>(emptyList())
    private val countdownsFlow = MutableStateFlow<List<StudyCountdown>>(emptyList())

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

    override fun observeFocusSessions(userId: String): Flow<List<FocusSession>> =
        sessionsFlow.map { list -> list.reversed() }

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
            email = email.ifBlank { "mode.test@local.elan" },
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
                Course("gl301", userId, "GL-301", "Architecture Logicielle & Design Patterns", "Prof. Laurent", 0L, 1440L, 620L, 16.5),
                Course("gl302", userId, "GL-302", "Systèmes Distribués & Cloud", "Prof. Moreau", 1L, 1200L, 480L, 15.2),
                Course("gl303", userId, "GL-303", "Bases de Données Avancées & NoSQL", "Prof. Benali", 2L, 1080L, 540L, 17.0),
                Course("gl304", userId, "GL-304", "Algorithmique & Graphes", "Prof. Chen", 3L, 1320L, 710L, 16.0),
                Course("gl305", userId, "GL-305", "Génie des Exigences & DevOps CI/CD", "Prof. Dupuis", 4L, 960L, 350L, 15.8),
                Course("gl306", userId, "GL-306", "Systèmes d'Exploitation & Concurrence", "Prof. Girard", 5L, 1200L, 420L, 14.8)
            )
        }

        if (tasksFlow.value.isEmpty()) {
            tasksFlow.value = listOf(
                StudyTask("t1", userId, "gl301", "Architecture Logicielle & Design Patterns", "Implémenter Observer & Strategy en Kotlin", "TP Clean Architecture + tests unitaires", today.toString(), "09:00", "high", TaskCategory.PROJECT.key, 50L, 0L, false, false),
                StudyTask("t2", userId, "gl302", "Systèmes Distribués & Cloud", "Revoir Consensus Raft & horloges de Lamport", "Fiches de synthèse chapitre 4", today.toString(), "11:15", "high", TaskCategory.EXAM.key, 45L, 0L, false, false),
                StudyTask("t3", userId, "gl304", "Algorithmique & Graphes", "Exercices Dijkstra & Bellman-Ford", "Série TD n°5 — complexité O(E log V)", today.toString(), "14:30", "medium", TaskCategory.ASSIGNMENT.key, 40L, 0L, true, false),
                StudyTask("t4", userId, "gl303", "Bases de Données Avancées & NoSQL", "Modélisation agrégats & indexation", "Schéma multi-tenant + règles sécurité", today.plusDays(1).toString(), "10:00", "medium", TaskCategory.PROJECT.key, 45L, 0L, false, false),
                StudyTask("t5", userId, "gl305", "Génie des Exigences & DevOps CI/CD", "Configurer pipeline GitHub Actions & lint", "Projet intégrateur S5", today.plusDays(2).toString(), "16:00", "low", TaskCategory.ASSIGNMENT.key, 30L, 0L, false, false)
            )
        }

        if (sessionsFlow.value.isEmpty()) {
            sessionsFlow.value = listOf(
                FocusSession("s1", userId, "gl304", "Algorithmique & Graphes", "Exercices Dijkstra & Bellman-Ford", 50L, "deep_code", today.minusDays(2).toString()),
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
    }

    override suspend fun updateSelectedGradient(userId: String, gradientId: String) {
        profileFlow.update { current ->
            current?.copy(selectedGradientId = gradientId)
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
            val starterIds = setOf(
                "gl301", "gl302", "gl303", "gl304", "gl305", "gl306",
                "ent01", "ent02", "ent03", "ent04", "ent05",
                "auto01", "auto02", "auto03", "auto04",
                "per01", "per02", "per03", "per04",
                "oth01", "oth02", "oth03", "oth04"
            )
            coursesFlow.update { existing ->
                if (existing.all { it.id in starterIds }) {
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
                            grade = 15.0
                        )
                    }
                } else {
                    existing
                }
            }
        }
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
    }

    override suspend fun createCourse(
        userId: String,
        code: String,
        title: String,
        professor: String,
        colorIndex: Long,
        targetMinutes: Long,
        grade: Double
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
            grade = grade.coerceIn(0.0, 20.0)
        )
        coursesFlow.update { it + course }
        return id
    }

    override suspend fun updateCourseGrade(userId: String, courseId: String, newGrade: Double) {
        coursesFlow.update { list ->
            list.map { c ->
                if (c.id == courseId) c.copy(grade = newGrade.coerceIn(0.0, 20.0)) else c
            }
        }
    }

    override suspend fun deleteCourse(userId: String, courseId: String) {
        coursesFlow.update { list -> list.filterNot { it.id == courseId } }
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
        quadrant: Long?
    ): String {
        val id = UUID.randomUUID().toString()
        val safePriority = if (priority in listOf("high", "medium", "low")) priority else "medium"
        val task = StudyTask(
            id = id,
            userId = userId,
            courseId = courseId.take(128),
            courseTitle = courseTitle.take(100),
            title = title.trim().take(140),
            notes = notes.trim().take(500),
            dueDateIso = dueDateIso.trim().take(20),
            scheduledTime = scheduledTime.trim().take(20),
            priority = safePriority,
            category = TaskCategory.sanitizeKey(category),
            estimatedMinutes = estimatedMinutes.coerceIn(5L, 600L),
            quadrant = quadrant?.takeIf { it in 1L..4L } ?: 0L,
            completed = false,
            aiGenerated = aiGenerated
        )
        tasksFlow.update { it + task }
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
                completed = false
            )
        }
        tasksFlow.update { it + newTasks }
    }

    override suspend fun toggleTaskCompleted(userId: String, task: StudyTask) {
        tasksFlow.update { list ->
            list.map { item -> if (item.id == task.id) item.copy(completed = !item.completed) else item }
        }
    }

    override suspend fun updateTaskQuadrant(userId: String, taskId: String, quadrant: Long) {
        val safeQuadrant = quadrant.coerceIn(1L, 4L)
        tasksFlow.update { list ->
            list.map { item -> if (item.id == taskId) item.copy(quadrant = safeQuadrant) else item }
        }
    }

    override suspend fun deleteTask(userId: String, taskId: String) {
        tasksFlow.update { list -> list.filterNot { it.id == taskId } }
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
    }

    override suspend fun deleteHabit(userId: String, habitId: String) {
        habitsFlow.update { list -> list.filterNot { it.id == habitId } }
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
        return id
    }

    override suspend fun deleteCountdown(userId: String, countdownId: String) {
        countdownsFlow.update { list -> list.filterNot { it.id == countdownId } }
    }

    override suspend fun logFocusSession(
        userId: String,
        courseId: String,
        courseTitle: String,
        taskTitle: String,
        durationMinutes: Long,
        mode: String,
        dateIso: String
    ): String {
        val id = UUID.randomUUID().toString()
        val safeMode = if (mode in listOf("pomodoro", "deep_code", "short_break", "stopwatch")) mode else "pomodoro"
        val safeDuration = durationMinutes.coerceIn(1L, 240L)
        val session = FocusSession(
            id = id,
            userId = userId,
            courseId = courseId.take(128),
            courseTitle = courseTitle.take(100),
            taskTitle = taskTitle.trim().take(140),
            durationMinutes = safeDuration,
            mode = safeMode,
            dateIso = dateIso.take(20)
        )
        sessionsFlow.update { it + session }

        if (courseId.isNotBlank() && safeMode != "short_break") {
            coursesFlow.update { list ->
                list.map { c ->
                    if (c.id == courseId) c.copy(completedMinutes = c.completedMinutes + safeDuration) else c
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
        return id
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

    private fun priorityRank(priority: String): Int = when (priority) {
        "high" -> 0
        "medium" -> 1
        else -> 2
    }
}
