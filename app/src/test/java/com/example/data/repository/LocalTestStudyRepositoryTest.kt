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

    @Test
    fun diskPersistence_survivesRestart_andSyncsIdempotentlyToCloud() = runBlocking {
        val tempFile = java.io.File.createTempFile("light_workspace_test_", ".json")
        tempFile.delete()
        try {
            val uid = "local_test_user"
            val firstInstance = LocalTestStudyRepository(tempFile)
            firstInstance.ensureUserProfileAndSeedIfNew(uid, "Clara", "clara@local.light")
            firstInstance.saveOnboardingConfiguration(
                userId = uid,
                displayName = "Clara",
                gender = UserGender.FEMININE,
                birthYear = 2004L,
                age = 22L,
                profession = UserProfession.STUDENT,
                customProfession = "",
                major = "IA & Systèmes",
                selectedGradientId = "lagon_boreal",
                dailyGoalTasks = 6L,
                notificationsEnabled = true,
                notificationFrequency = NotificationFrequency.DAILY,
                selectedHabits = emptyList(),
                adaptModulesToProfession = true
            )
            val customCourseId = firstInstance.createCourse(
                userId = uid,
                code = "IA-402",
                title = "Apprentissage Profond",
                professor = "Dr. Diallo",
                colorIndex = 4L,
                targetMinutes = 900L,
                grade = 17.5,
                iconKey = "sparkles"
            )
            val projectId = firstInstance.createProject(
                userId = uid,
                title = "Capstone IA",
                description = "Pipeline multimodal",
                startDateIso = "2026-11-10",
                dueDateIso = "2026-12-15"
            )
            val taskId = firstInstance.createTask(
                userId = uid,
                courseId = customCourseId,
                courseTitle = "Apprentissage Profond",
                title = "Valider jeu de test",
                notes = "Métriques F1",
                dueDateIso = "2026-11-28",
                scheduledTime = "14:00",
                priority = "high",
                estimatedMinutes = 60L,
                startDateIso = "2026-11-20",
                projectId = projectId
            )
            val habitId = firstInstance.createHabit(
                userId = uid,
                title = "Lecture papier",
                subtitle = "1 article par jour",
                category = "study",
                iconKey = "book",
                colorIndex = 1L
            )
            val createdHabit = firstInstance.observeHabits(uid).first().first { it.id == habitId }
            firstInstance.toggleHabitForDate(uid, createdHabit, "2026-11-17")
            firstInstance.createCountdown(
                userId = uid,
                title = "Grand Oral",
                notes = "Amphi A",
                targetDateIso = "2026-12-20",
                category = "exam",
                colorIndex = 2L
            )
            firstInstance.logFocusSession(
                userId = uid,
                courseId = customCourseId,
                courseTitle = "Apprentissage Profond",
                taskTitle = "Valider jeu de test",
                durationMinutes = 45L,
                mode = "deep_code",
                dateIso = "2026-11-17"
            )

            // Cold restart from disk
            val reloaded = LocalTestStudyRepository(tempFile)
            reloaded.ensureUserProfileAndSeedIfNew(uid, "Ignored", "ignored@local.light")

            val profile = reloaded.observeUserProfile(uid).first()!!
            assertTrue(profile.onboardingCompleted)
            assertEquals("Clara", profile.displayName)
            assertEquals("lagon_boreal", profile.selectedGradientId)
            assertTrue(reloaded.observeCourses(uid).first().any { it.code == "IA-402" && it.iconKey == "sparkles" })
            assertTrue(reloaded.observeProjects(uid).first().any { it.title == "Capstone IA" })
            val reloadedTask = reloaded.observeTasks(uid).first().first { it.id == taskId }
            assertEquals(projectId, reloadedTask.projectId)
            assertEquals("2026-11-20", reloadedTask.startDateIso)
            assertTrue(reloaded.observeHabits(uid).first().any { it.title == "Lecture papier" && "2026-11-17" in it.completedDates })
            assertTrue(reloaded.observeCountdowns(uid).first().any { it.title == "Grand Oral" })
            assertTrue(reloaded.observeFocusSessions(uid).first().any { it.taskTitle == "Valider jeu de test" && it.durationMinutes == 45L })

            // Sync to simulated cloud repo for a real cloud userId
            val simulatedCloud = LocalTestStudyRepository()
            val cloudUid = "cloud_uid_clara"
            val syncedFirst = reloaded.syncToCloudIfNeeded(simulatedCloud, cloudUid, "Clara", "clara@cloud.fr")
            assertTrue(syncedFirst)

            val cloudProjects = simulatedCloud.observeProjects(cloudUid).first()
            assertEquals(1, cloudProjects.size)
            val cloudProjectId = cloudProjects.single().id
            val cloudTask = simulatedCloud.observeTasks(cloudUid).first().first { it.title == "Valider jeu de test" }
            assertEquals(cloudProjectId, cloudTask.projectId)
            assertTrue(simulatedCloud.observeCourses(cloudUid).first().any { it.code == "IA-402" })
            assertTrue(simulatedCloud.observeHabits(cloudUid).first().any { it.title == "Lecture papier" && "2026-11-17" in it.completedDates })
            assertTrue(simulatedCloud.observeCountdowns(cloudUid).first().any { it.title == "Grand Oral" })
            assertTrue(simulatedCloud.observeFocusSessions(cloudUid).first().any { it.taskTitle == "Valider jeu de test" })

            // Idempotent: second call must not duplicate items
            val syncedSecond = reloaded.syncToCloudIfNeeded(simulatedCloud, cloudUid, "Clara", "clara@cloud.fr")
            assertFalse(syncedSecond)
            assertEquals(1, simulatedCloud.observeProjects(cloudUid).first().size)
            assertEquals(1, simulatedCloud.observeTasks(cloudUid).first().count { it.title == "Valider jeu de test" })
        } finally {
            tempFile.delete()
        }
    }

}
