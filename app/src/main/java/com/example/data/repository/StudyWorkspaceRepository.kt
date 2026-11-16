package com.example.data.repository

import com.example.data.model.AdaptiveHabitTemplate
import com.example.data.model.Course
import com.example.data.model.FocusSession
import com.example.data.model.NotificationFrequency
import com.example.data.model.StudyCountdown
import com.example.data.model.StudyHabit
import com.example.data.model.StudyTask
import com.example.data.model.TaskCategory
import com.example.data.model.UserGender
import com.example.data.model.UserProfession
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface StudyWorkspaceRepository {
    fun observeUserProfile(userId: String): Flow<UserProfile?>
    fun observeCourses(userId: String): Flow<List<Course>>
    fun observeTasks(userId: String): Flow<List<StudyTask>>
    fun observeFocusSessions(userId: String): Flow<List<FocusSession>>
    fun observeHabits(userId: String): Flow<List<StudyHabit>>
    fun observeCountdowns(userId: String): Flow<List<StudyCountdown>>

    suspend fun ensureUserProfileAndSeedIfNew(
        userId: String,
        displayName: String,
        email: String
    )

    suspend fun updateSelectedGradient(userId: String, gradientId: String)

    suspend fun updateProfile(
        userId: String,
        displayName: String,
        major: String,
        dailyGoalTasks: Long,
        gender: String? = null,
        birthYear: Long? = null,
        age: Long? = null,
        profession: String? = null,
        customProfession: String? = null
    )

    suspend fun saveOnboardingConfiguration(
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
    )

    suspend fun updateNotificationSettings(
        userId: String,
        enabled: Boolean,
        frequency: NotificationFrequency
    )

    suspend fun createCourse(
        userId: String,
        code: String,
        title: String,
        professor: String,
        colorIndex: Long,
        targetMinutes: Long,
        grade: Double
    ): String

    suspend fun updateCourseGrade(userId: String, courseId: String, newGrade: Double)
    suspend fun deleteCourse(userId: String, courseId: String)

    suspend fun createTask(
        userId: String,
        courseId: String,
        courseTitle: String,
        title: String,
        notes: String,
        dueDateIso: String,
        scheduledTime: String,
        priority: String,
        category: String = TaskCategory.REVISION.key,
        estimatedMinutes: Long,
        aiGenerated: Boolean = false,
        quadrant: Long? = null
    ): String

    suspend fun createTasksBatch(userId: String, tasks: List<StudyTask>)
    suspend fun toggleTaskCompleted(userId: String, task: StudyTask)
    suspend fun updateTaskQuadrant(userId: String, taskId: String, quadrant: Long)
    suspend fun deleteTask(userId: String, taskId: String)

    suspend fun createHabit(
        userId: String,
        title: String,
        subtitle: String,
        category: String,
        iconKey: String,
        colorIndex: Long
    ): String

    suspend fun toggleHabitForDate(userId: String, habit: StudyHabit, dateIso: String)
    suspend fun deleteHabit(userId: String, habitId: String)

    suspend fun createCountdown(
        userId: String,
        title: String,
        notes: String,
        targetDateIso: String,
        category: String,
        colorIndex: Long
    ): String

    suspend fun deleteCountdown(userId: String, countdownId: String)

    suspend fun logFocusSession(
        userId: String,
        courseId: String,
        courseTitle: String,
        taskTitle: String,
        durationMinutes: Long,
        mode: String,
        dateIso: String = LocalDate.now().toString()
    ): String
}
