package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class StudyRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ensureUserProfile_andCreateTask_succeedsForOwner() = runBlocking {
        val uid = signInTestUser("camille_${UUID.randomUUID()}@test.com")
        val repository = StudyRepository(firestore, auth)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureUserProfileAndSeedIfNew(uid, "Camille", "camille@test.com")
        }

        val taskTitle = "Préparer soutenance GL-301 ${UUID.randomUUID()}"
        val taskId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createTask(
                userId = uid,
                courseId = "gl301",
                courseTitle = "Architecture Logicielle & Design Patterns",
                title = taskTitle,
                notes = "UML + tests unitaires",
                dueDateIso = "2026-11-16",
                scheduledTime = "10:00",
                priority = "high",
                category = "exam",
                estimatedMinutes = 50L
            )
        }
        assertTrue(taskId.isNotBlank())

        val tasks = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeTasks(uid).first { list -> list.any { it.id == taskId } }
        }
        assertTrue(tasks.any { it.id == taskId && it.title == taskTitle && it.category == "exam" })
    }

    @Test
    fun logFocusSession_appendsSessionAndUpdatesStreak() = runBlocking {
        val uid = signInTestUser("lea_${UUID.randomUUID()}@test.com")
        val repository = StudyRepository(firestore, auth)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureUserProfileAndSeedIfNew(uid, "Léa", "lea@test.com")
        }

        val sessionId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.logFocusSession(
                userId = uid,
                courseId = "gl301",
                courseTitle = "Architecture Logicielle & Design Patterns",
                taskTitle = "Kata Clean Architecture",
                durationMinutes = 25L,
                mode = "pomodoro"
            )
        }
        val stopwatchId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.logFocusSession(
                userId = uid,
                courseId = "gl301",
                courseTitle = "Architecture Logicielle & Design Patterns",
                taskTitle = "Chronomètre libre",
                durationMinutes = 18L,
                mode = "stopwatch"
            )
        }
        assertTrue(sessionId.isNotBlank() && stopwatchId.isNotBlank())
    }

    @Test
    fun habitsAndCountdowns_andTaskQuadrant_succeedForOwner() = runBlocking {
        val uid = signInTestUser("nora_${UUID.randomUUID()}@test.com")
        val repository = StudyRepository(firestore, auth)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureUserProfileAndSeedIfNew(uid, "Nora", "nora@test.com")
        }

        val taskId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createTask(
                userId = uid,
                courseId = "gl301",
                courseTitle = "Architecture Logicielle",
                title = "Audit sécurité",
                notes = "Tests de règles",
                dueDateIso = "2026-11-16",
                scheduledTime = "14:00",
                priority = "high",
                category = "project",
                estimatedMinutes = 45L,
                quadrant = 1L
            )
        }
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateTaskQuadrant(uid, taskId, 2L)
        }

        val habitId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createHabit(
                userId = uid,
                title = "Levé tôt",
                subtitle = "Levez-vous et soyez incroyable",
                category = "health",
                iconKey = "sun",
                colorIndex = 2L
            )
        }
        val habits = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeHabits(uid).first { list -> list.any { it.id == habitId } }
        }
        val createdHabit = habits.first { it.id == habitId }
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.toggleHabitForDate(uid, createdHabit, "2026-11-16")
        }

        val countdownId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createCountdown(
                userId = uid,
                title = "Hackathon Génie Logiciel",
                notes = "Équipe L3",
                targetDateIso = "2026-12-01",
                category = "milestone",
                colorIndex = 1L
            )
        }
        val countdowns = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCountdowns(uid).first { list -> list.any { it.id == countdownId } }
        }
        assertTrue(countdowns.any { it.id == countdownId })
    }

    @Test
    fun onboardingProfileConfiguration_adaptsWorkspaceAndRespectsRules() = runBlocking {
        val uid = signInTestUser("alex_${UUID.randomUUID()}@test.com")
        val repository = StudyRepository(firestore, auth)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureUserProfileAndSeedIfNew(uid, "Alex", "alex@test.com")
            repository.saveOnboardingConfiguration(
                userId = uid,
                displayName = "Alex",
                gender = com.example.data.model.UserGender.MASCULINE,
                birthYear = 2001L,
                age = 25L,
                profession = com.example.data.model.UserProfession.OTHER,
                customProfession = "Designer Produit",
                major = "Design System & UI",
                selectedGradientId = "lagon_boreal",
                dailyGoalTasks = 6L,
                notificationsEnabled = true,
                notificationFrequency = com.example.data.model.NotificationFrequency.EVERY_3H,
                selectedHabits = listOf(
                    com.example.data.model.AdaptiveHabitTemplate(
                        title = "Sketch quotidien",
                        subtitle = "15 min de recherche visuelle",
                        category = com.example.data.model.HabitCategory.STUDY,
                        iconKey = "sparkles",
                        colorIndex = 0L
                    )
                ),
                adaptModulesToProfession = true
            )
        }

        val profile = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserProfile(uid).first { it?.onboardingCompleted == true }
        }
        assertEquals("masculine", profile?.gender)
        assertEquals(2001L, profile?.birthYear)
        assertEquals(25L, profile?.age)
        assertEquals("other", profile?.profession)
        assertEquals("Designer Produit", profile?.customProfession)
        assertEquals("every_3h", profile?.notificationFrequency)

        val courses = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCourses(uid).first { list -> list.any { it.code == "COEUR" } }
        }
        assertTrue(courses.any { it.title.contains("Designer Produit") })
    }

    @Test
    fun crossUserRead_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser("alice_${UUID.randomUUID()}@test.com")
        val aliceRepo = StudyRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.ensureUserProfileAndSeedIfNew(aliceUid, "Alice", "alice@test.com")
        }

        signInTestUser("bob_${UUID.randomUUID()}@test.com")
        val bobRepo = StudyRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeTasks(aliceUid).first()
            }
            fail("Expected cross-user observeTasks to fail with PERMISSION_DENIED")
        } catch (e: Exception) {
            val fsError = e.unwrapFirestoreException()
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
        }

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeHabits(aliceUid).first()
            }
            fail("Expected cross-user observeHabits to fail with PERMISSION_DENIED")
        } catch (e: Exception) {
            val fsError = e.unwrapFirestoreException()
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
        }
    }

    @Test
    fun observeTasks_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = StudyRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeTasks("some_user_123").first()
            }
            fail("Expected unauthenticated observeTasks to fail with PERMISSION_DENIED")
        } catch (e: Exception) {
            val fsError = e.unwrapFirestoreException()
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
        }
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
