package com.example.data.repository

import com.example.data.model.AdaptiveHabitTemplate
import com.example.data.model.HabitCategory
import com.example.data.model.NotificationFrequency
import com.example.data.model.UserGender
import com.example.data.model.UserProfession
import com.example.data.model.adaptation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalTestStudyRepositoryTest {

    @Test
    fun localTestMode_seedsInitialWorkspace_andAdaptsAfterOnboarding() = runBlocking {
        val repo = LocalTestStudyRepository()
        val uid = "local_test_user"

        repo.ensureUserProfileAndSeedIfNew(uid, "Testeur", "mode.test@local.light")

        val initialProfile = repo.observeUserProfile(uid).first()!!
        assertFalse(initialProfile.onboardingCompleted)
        assertEquals(6, repo.observeCourses(uid).first().size)
        assertEquals(5, repo.observeTasks(uid).first().size)

        repo.saveOnboardingConfiguration(
            userId = uid,
            displayName = "Lucas",
            gender = UserGender.MASCULINE,
            birthYear = 1998L,
            age = 28L,
            profession = UserProfession.ENTREPRENEUR,
            customProfession = "",
            major = "SaaS B2B",
            selectedGradientId = "nuit_electrique",
            dailyGoalTasks = 7L,
            notificationsEnabled = true,
            notificationFrequency = NotificationFrequency.EVERY_3H,
            selectedHabits = listOf(
                AdaptiveHabitTemplate("Deep Work matin", "90 min", HabitCategory.STUDY, "sparkles", 0L)
            ),
            adaptModulesToProfession = true
        )

        val updatedProfile = repo.observeUserProfile(uid).first()!!
        assertTrue(updatedProfile.onboardingCompleted)
        assertEquals("masculine", updatedProfile.gender)
        assertEquals(28L, updatedProfile.age)
        assertEquals(1998L, updatedProfile.birthYear)
        assertEquals("entrepreneur", updatedProfile.profession)
        assertEquals("Mes Pôles & Projets", updatedProfile.adaptation().workspaceSectionTitle)
        assertEquals(5, repo.observeCourses(uid).first().size)
        assertTrue(repo.observeHabits(uid).first().any { it.title == "Deep Work matin" })

        val taskId = repo.createTask(
            userId = uid,
            courseId = "ent01",
            courseTitle = "Produit",
            title = "Préparer démo client",
            notes = "Slides + prototype",
            dueDateIso = "2026-11-16",
            scheduledTime = "15:00",
            priority = "high",
            estimatedMinutes = 45L,
            startDateIso = "2026-11-10"
        )
        assertTrue(repo.observeTasks(uid).first().any { it.id == taskId && it.startDateIso == "2026-11-10" })
    }

    @Test
    fun courses_storeSelectedIconKeyAndSeedRecognizableSubjectIcons() = runBlocking {
        val repo = LocalTestStudyRepository()
        val uid = "icon_test_user"
        repo.ensureUserProfileAndSeedIfNew(uid, "Icon Test", "icons@local.light")

        val seeded = repo.observeCourses(uid).first()
        assertEquals("code", seeded.first { it.code == "GL-301" }.iconKey)
        assertEquals("database", seeded.first { it.code == "GL-303" }.iconKey)

        val courseId = repo.createCourse(
            userId = uid,
            code = "BIO-101",
            title = "Biologie",
            professor = "",
            colorIndex = 2L,
            targetMinutes = 1200L,
            grade = 15.0,
            iconKey = "microscope"
        )
        assertEquals("microscope", repo.observeCourses(uid).first().first { it.id == courseId }.iconKey)
    }

    @Test
    fun localProjects_supportCrudAndDetachLinkedTasksOnDelete() = runBlocking {
        val repo = LocalTestStudyRepository()
        val projectId = repo.createProject(
            userId = "local",
            title = "Refonte mobile",
            description = "Livraison progressive",
            startDateIso = "2026-11-01",
            dueDateIso = "2026-12-01"
        )
        val project = repo.observeProjects("local").first().single()
        assertEquals(projectId, project.id)
        assertEquals("active", project.status)

        repo.updateProject(project.copy(title = "Refonte application", status = "on_hold"))
        assertEquals("on_hold", repo.observeProjects("local").first().single().status)

        val taskId = repo.createTask(
            userId = "local",
            courseId = "",
            courseTitle = "",
            title = "Prototype écran",
            notes = "",
            dueDateIso = "2026-11-20",
            scheduledTime = "10:00",
            priority = "medium",
            estimatedMinutes = 45L,
            projectId = projectId
        )
        assertEquals(projectId, repo.observeTasks("local").first().single { it.id == taskId }.projectId)

        repo.deleteProject("local", projectId)
        assertTrue(repo.observeProjects("local").first().isEmpty())
        val detachedTask = repo.observeTasks("local").first().single { it.id == taskId }
        assertEquals("", detachedTask.projectId)
    }

}
