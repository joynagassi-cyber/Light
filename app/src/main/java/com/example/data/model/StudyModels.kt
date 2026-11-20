package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class UserGender(val key: String, val label: String) {
    FEMININE("feminine", "Féminin"),
    MASCULINE("masculine", "Masculin");

    companion object {
        fun fromKey(key: String?): UserGender =
            entries.firstOrNull { it.key == key } ?: FEMININE
    }
}

enum class UserProfession(val key: String) {
    STUDENT("student"),
    ENTREPRENEUR("entrepreneur"),
    SELF_TAUGHT("self_taught"),
    INDIVIDUAL("individual"),
    OTHER("other");

    fun labelFor(gender: UserGender, customProfession: String = ""): String = when (this) {
        STUDENT -> if (gender == UserGender.FEMININE) "Étudiante" else "Étudiant"
        ENTREPRENEUR -> if (gender == UserGender.FEMININE) "Entrepreneuse" else "Entrepreneur"
        SELF_TAUGHT -> "Autodidacte"
        INDIVIDUAL -> if (gender == UserGender.FEMININE) "Particulière" else "Particulier"
        OTHER -> customProfession.trim().ifBlank { "Autre profil" }
    }

    fun defaultFocusDomain(): String = when (this) {
        STUDENT -> "Génie Logiciel · 3ᵉ Année"
        ENTREPRENEUR -> "Startup, Produit & Croissance"
        SELF_TAUGHT -> "Apprentissage Continu & Projets"
        INDIVIDUAL -> "Organisation & Objectifs Perso"
        OTHER -> "Projets & Excellence Quotidienne"
    }

    companion object {
        fun fromKey(key: String?): UserProfession =
            entries.firstOrNull { it.key == key } ?: STUDENT
    }
}

enum class NotificationFrequency(
    val key: String,
    val label: String,
    val description: String
) {
    REALTIME("realtime", "Temps réel", "Rappels avant chaque bloc et fin de Focus"),
    DAILY("daily", "Quotidienne", "Briefing du matin (08h30) & bilan du soir"),
    EVERY_3H("every_3h", "Toutes les 3 h", "Rappel doux pendant vos créneaux actifs"),
    WEEKLY("weekly", "Hebdomadaire", "Résumé complet chaque dimanche soir"),
    OFF("off", "Désactivée", "Aucun rappel automatique");

    companion object {
        fun fromKey(key: String?): NotificationFrequency =
            entries.firstOrNull { it.key == key } ?: DAILY
    }
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val major: String = "Génie Logiciel · 3ᵉ Année",
    val selectedGradientId: String = "aurore_lavande",
    val dailyGoalTasks: Long = 5L,
    val streakDays: Long = 1L,
    val lastStudyDate: String = "",
    val gender: String = UserGender.FEMININE.key,
    val birthYear: Long = 2005L,
    val age: Long = 21L,
    val profession: String = UserProfession.STUDENT.key,
    val customProfession: String = "",
    val notificationsEnabled: Boolean = true,
    val notificationFrequency: String = NotificationFrequency.DAILY.key,
    val onboardingCompleted: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Course(
    @DocumentId val id: String = "",
    val userId: String = "",
    val code: String = "",
    val title: String = "",
    val professor: String = "",
    val colorIndex: Long = 0L,
    val targetMinutes: Long = 1200L,
    val completedMinutes: Long = 0L,
    val grade: Double = 15.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    val iconKey: String = "book"
)

enum class TaskCategory(val key: String, val label: String) {
    EXAM("exam", "Examen"),
    PROJECT("project", "Projet"),
    MILESTONE("milestone", "Jalon"),
    ASSIGNMENT("assignment", "TP / Devoir"),
    REVISION("revision", "Révision"),
    READING("reading", "Lecture");

    companion object {
        fun fromKey(key: String?): TaskCategory =
            entries.firstOrNull { it.key == key } ?: REVISION

        fun sanitizeKey(key: String?): String = fromKey(key).key
    }
}

@IgnoreExtraProperties
data class StudyProject(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val startDateIso: String = "",
    val dueDateIso: String = "",
    val status: String = "active",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StudyTask(
    @DocumentId val id: String = "",
    val userId: String = "",
    val courseId: String = "",
    val courseTitle: String = "",
    val title: String = "",
    val notes: String = "",
    val dueDateIso: String = "",
    val scheduledTime: String = "",
    val priority: String = "medium",
    val category: String = TaskCategory.REVISION.key,
    val estimatedMinutes: Long = 30L,
    val quadrant: Long = 0L,
    val completed: Boolean = false,
    val aiGenerated: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    /** Optional planned start date; older tasks default to their due date in the Gantt view. */
    val startDateIso: String = "",
    val projectId: String = "",
    val focusProfileId: String = "",
    val focusProfileTitle: String = "",
    val subtasks: List<String> = emptyList(),
    val predecessorIds: List<String> = emptyList()
)

const val MAX_TASK_SUBTASKS = 8
const val MAX_SUBTASK_LENGTH = 120

fun normalizeSubtasks(items: List<String>): List<String> = items
    .asSequence().map { it.trim().take(MAX_SUBTASK_LENGTH) }.filter { it.isNotEmpty() }.take(MAX_TASK_SUBTASKS).toList()

fun StudyTask.resolvedQuadrant(todayIso: String = java.time.LocalDate.now().toString()): Int {
    if (quadrant in 1L..4L) return quadrant.toInt()
    val isUrgent = dueDateIso.isNotBlank() && dueDateIso <= todayIso
    val isImportant = priority == "high" || category == TaskCategory.EXAM.key || category == TaskCategory.PROJECT.key || category == TaskCategory.MILESTONE.key
    return when {
        isUrgent && isImportant -> 1
        !isUrgent && isImportant -> 2
        isUrgent && !isImportant -> 3
        else -> 4
    }
}

@IgnoreExtraProperties
data class FocusProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val learningTopic: String = "",
    val iconKey: String = "brain",
    val colorIndex: Long = 0L,
    val defaultMinutes: Long = 25L,
    val totalFocusMinutes: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FocusSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val courseId: String = "",
    val courseTitle: String = "",
    val taskTitle: String = "",
    val durationMinutes: Long = 25L,
    val mode: String = "pomodoro",
    val dateIso: String = "",
    val focusProfileId: String = "",
    val focusProfileTitle: String = "",
    val taskId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class HabitCategory(val key: String, val label: String) {
    STUDY("study", "Proposé"),
    LIFE("life", "Vie"),
    HEALTH("health", "Santé"),
    SPORTS("sports", "Sports"),
    MINDSET("mindset", "État d'esprit");

    companion object {
        fun fromKey(key: String?): HabitCategory =
            entries.firstOrNull { it.key == key } ?: STUDY

        fun sanitizeKey(key: String?): String = fromKey(key).key
    }
}

@IgnoreExtraProperties
data class StudyHabit(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val category: String = HabitCategory.STUDY.key,
    val iconKey: String = "sparkles",
    val colorIndex: Long = 0L,
    val streakDays: Long = 0L,
    val completedDates: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class CountdownCategory(val key: String, val label: String) {
    EXAM("exam", "Partiel / Examen"),
    PROJECT("project", "Projet / Soutenance"),
    MILESTONE("milestone", "Événement L3"),
    PERSONAL("personal", "Personnel");

    companion object {
        fun fromKey(key: String?): CountdownCategory =
            entries.firstOrNull { it.key == key } ?: EXAM

        fun sanitizeKey(key: String?): String = fromKey(key).key
    }
}

@IgnoreExtraProperties
data class StudyCountdown(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val notes: String = "",
    val targetDateIso: String = "",
    val category: String = CountdownCategory.EXAM.key,
    val colorIndex: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class AiPlannedTask(
    val title: String,
    val notes: String,
    val courseCode: String,
    val dueDateIso: String,
    val scheduledTime: String,
    val priority: String,
    val category: String = TaskCategory.REVISION.key,
    val estimatedMinutes: Int
)

data class AiStudyPlanResult(
    val summary: String,
    val strategyTip: String,
    val tasks: List<AiPlannedTask>
)
