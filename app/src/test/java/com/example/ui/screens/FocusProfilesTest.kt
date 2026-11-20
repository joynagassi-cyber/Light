package com.example.ui.screens

import android.content.Context
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Course
import com.example.data.model.FocusProfile
import com.example.data.model.StudyTask
import com.example.data.model.UiState
import com.example.data.preferences.ThemePreferencesStore
import com.example.data.repository.AiPlannerRepository
import com.example.data.repository.LocalTestStudyRepository
import com.example.ui.FocusTimerState
import com.example.ui.StudyViewModel
import com.example.ui.theme.LightThemeProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h915dp-xxhdpi")
class FocusProfilesTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun waitUntil(timeoutMs: Long = 3_000L, predicate: () -> Boolean) {
        val deadlineNanos = System.nanoTime() + timeoutMs * 1_000_000L
        while (!predicate() && System.nanoTime() < deadlineNanos) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(15L)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun repository_createAssignSessionAccumulationDeleteAndCloudSync_workCorrectly() = runBlocking {
        val tempFile = File.createTempFile("focus_profile_repo_", ".json")
        tempFile.delete()
        try {
            val uid = "fp_user"
            val repo = LocalTestStudyRepository(tempFile)
            repo.ensureUserProfileAndSeedIfNew(uid, "Amine", "amine@test.fr")
            val profileId = repo.createFocusProfile(
                userId = uid,
                title = "Japonais JLPT N4",
                learningTopic = "Kanji & grammaire conversationnelle",
                iconKey = "globe",
                colorIndex = 2L,
                defaultMinutes = 35L
            )

            val taskId = repo.createTask(
                userId = uid,
                courseId = "",
                courseTitle = "",
                title = "Réviser 20 kanji",
                notes = "",
                dueDateIso = "2026-11-20",
                scheduledTime = "18:00",
                priority = "high",
                estimatedMinutes = 35L
            )
            repo.assignTaskToFocusProfile(
                userId = uid,
                taskId = taskId,
                focusProfileId = profileId,
                focusProfileTitle = "Japonais JLPT N4"
            )

            repo.logFocusSession(
                userId = uid,
                courseId = "",
                courseTitle = "",
                taskTitle = "Réviser 20 kanji",
                durationMinutes = 35L,
                mode = "pomodoro",
                dateIso = "2026-11-18",
                focusProfileId = profileId,
                focusProfileTitle = "Japonais JLPT N4",
                taskId = taskId
            )

            // Reload from disk to verify offline persistence
            val reloaded = LocalTestStudyRepository(tempFile)
            reloaded.ensureUserProfileAndSeedIfNew(uid, "Ignored", "ignored@test.fr")
            val reloadedProfile = reloaded.observeFocusProfiles(uid).first().single { it.id == profileId }
            assertEquals("Japonais JLPT N4", reloadedProfile.title)
            assertEquals(35L, reloadedProfile.totalFocusMinutes)

            val reloadedTask = reloaded.observeTasks(uid).first().single { it.id == taskId }
            assertEquals(profileId, reloadedTask.focusProfileId)
            assertEquals("Japonais JLPT N4", reloadedTask.focusProfileTitle)

            // Sync to simulated cloud repo
            val cloudRepo = LocalTestStudyRepository()
            val cloudUid = "cloud_fp_user"
            assertTrue(reloaded.syncToCloudIfNeeded(cloudRepo, cloudUid, "Amine", "amine@test.fr"))
            val cloudProfiles = cloudRepo.observeFocusProfiles(cloudUid).first()
            val cloudProfile = cloudProfiles.single { it.title == "Japonais JLPT N4" }
            assertEquals(35L, cloudProfile.totalFocusMinutes)
            val cloudTask = cloudRepo.observeTasks(cloudUid).first().single { it.title == "Réviser 20 kanji" }
            assertEquals(cloudProfile.id, cloudTask.focusProfileId)

            // Deleting profile detaches tasks safely
            reloaded.deleteFocusProfile(uid, profileId)
            assertTrue(reloaded.observeFocusProfiles(uid).first().none { it.id == profileId })
            val detachedTask = reloaded.observeTasks(uid).first().single { it.id == taskId }
            assertEquals("", detachedTask.focusProfileId)
            assertEquals("", detachedTask.focusProfileTitle)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun viewModel_createAssignAndStartFocusForProfileTask_updatesLiveFocusState() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = LocalTestStudyRepository()
        val uid = "vm_fp_user"

        val vm = StudyViewModel(
            repository = repo,
            aiPlannerRepository = AiPlannerRepository(),
            themePreferencesStore = ThemePreferencesStore(context),
            currentUserId = uid,
            initialDisplayName = "Lina",
            initialEmail = "lina@test.fr",
            isLocalMode = true
        )

        val profilesCollector = mutableListOf<FocusProfile>()
        waitUntil { vm.tasksState.value is UiState.Success || true }

        vm.createFocusProfile(
            title = "Apprendre Rust",
            learningTopic = "Ownership et async Tokio",
            iconKey = "code",
            colorIndex = 1L,
            defaultMinutes = 45L
        )
        waitUntil {
            val loaded = runBlocking { repo.observeFocusProfiles(uid).first() }
            profilesCollector.clear()
            profilesCollector.addAll(loaded)
            loaded.any { it.title == "Apprendre Rust" }
        }
        val createdProfile = profilesCollector.first { it.title == "Apprendre Rust" }

        vm.createTask(
            course = null,
            title = "Implémenter un serveur TCP",
            notes = "",
            dueDateIso = "2026-11-22",
            scheduledTime = "10:00",
            priority = "high",
            estimatedMinutes = 45L,
            focusProfileId = createdProfile.id,
            focusProfileTitle = createdProfile.title
        )
        var createdTask: StudyTask? = null
        waitUntil {
            createdTask = runBlocking {
                repo.observeTasks(uid).first().firstOrNull { it.title == "Implémenter un serveur TCP" }
            }
            createdTask != null
        }

        val task = createdTask!!
        assertEquals(createdProfile.id, task.focusProfileId)

        vm.startFocusForProfileTask(createdProfile, task, emptyList<Course>())
        val focus = vm.focusState.value
        assertTrue(focus.isRunning)
        assertEquals(createdProfile.id, focus.selectedFocusProfileId)
        assertEquals("Apprendre Rust", focus.selectedFocusProfileTitle)
        assertEquals(task.id, focus.selectedTaskId)
        assertEquals("Implémenter un serveur TCP", focus.selectedTaskTitle)
        assertEquals(45 * 60, focus.totalSeconds)

        vm.resetFocusTimer()
    }

    @Test
    fun focusProfilesScreen_createProfileAssignTaskAndStartFocus_showsFocusedTaskInsideProfile() {
        val profiles = mutableStateListOf<FocusProfile>()
        val tasks = mutableStateListOf(
            StudyTask(
                id = "task_piano",
                title = "Déchiffrer Prélude en Do majeur",
                dueDateIso = "2026-11-20",
                estimatedMinutes = 30L
            )
        )
        var focusState by mutableStateOf(FocusTimerState())

        composeRule.setContent {
            LightThemeProvider {
                FocusProfilesScreen(
                    profiles = profiles,
                    tasks = tasks,
                    sessions = emptyList(),
                    focusState = focusState,
                    onCreateProfile = { title, topic, iconKey, colorIndex, defaultMins ->
                        profiles.add(
                            FocusProfile(
                                id = "fp_custom_1",
                                title = title,
                                learningTopic = topic,
                                iconKey = iconKey,
                                colorIndex = colorIndex,
                                defaultMinutes = defaultMins
                            )
                        )
                    },
                    onDeleteProfile = {},
                    onAssignTaskToProfile = { taskId, profile ->
                        val idx = tasks.indexOfFirst { it.id == taskId }
                        if (idx >= 0) {
                            tasks[idx] = tasks[idx].copy(
                                focusProfileId = profile?.id ?: "",
                                focusProfileTitle = profile?.title ?: ""
                            )
                        }
                    },
                    onToggleTaskCompleted = {},
                    onStartFocusForTaskInProfile = { profile, task ->
                        focusState = focusState.copy(
                            isRunning = true,
                            selectedFocusProfileId = profile.id,
                            selectedFocusProfileTitle = profile.title,
                            selectedTaskId = task.id,
                            selectedTaskTitle = task.title,
                            totalSeconds = profile.defaultMinutes.toInt() * 60,
                            remainingSeconds = profile.defaultMinutes.toInt() * 60
                        )
                    },
                    onToggleTimerPlayPause = {
                        focusState = focusState.copy(isRunning = !focusState.isRunning)
                    },
                    onOpenTimerScreen = {},
                    onOpenCreateTaskForProfile = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("create_focus_profile_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("focus_profile_title_input").performTextInput("Piano Classique")
        composeRule.onNodeWithTag("focus_profile_topic_input").performTextInput("Travailler Bach et lecture à vue")
        composeRule.onNodeWithTag("confirm_create_focus_profile_button").performClick()
        composeRule.waitForIdle()

        // Open created profile detail screen
        composeRule.onNodeWithTag("focus_profile_card_fp_custom_1").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("focus_profile_detail_screen").assertIsDisplayed()

        // Assign existing task to this Focus Profile
        composeRule.onNodeWithTag("assign_task_to_profile_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("assign_candidate_task_task_piano").performClick()
        composeRule.waitForIdle()

        // Scroll to the newly assigned task inside LazyColumn and start Focus on it
        composeRule.onNodeWithTag("focus_profile_detail_screen")
            .performScrollToNode(hasTestTag("profile_assigned_task_task_piano"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("profile_assigned_task_task_piano").assertIsDisplayed()

        // Launch Focus on the assigned task -> profile screen displays active focused task
        composeRule.onNodeWithTag("focus_profile_detail_screen")
            .performScrollToNode(hasTestTag("focus_profile_start_task_task_piano"))
        composeRule.onNodeWithTag("focus_profile_start_task_task_piano").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("focus_profile_detail_screen")
            .performScrollToNode(hasTestTag("focus_profile_live_status_card"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("focus_profile_live_status_card").assertIsDisplayed()
        composeRule.onNodeWithTag("focus_profile_current_task_text")
            .assertIsDisplayed()
            .assertTextContains("Vous vous êtes mis en focus pour : Déchiffrer Prélude en Do majeur")
    }

    @Test
    fun taskCreationScreen_allowsSelectingFocusProfileOnTaskCreation() {
        var capturedProfileId = ""
        var capturedProfileTitle = ""
        val profiles = listOf(
            FocusProfile(
                id = "fp_ai",
                title = "Apprendre PyTorch",
                learningTopic = "Réseaux de neurones"
            )
        )

        composeRule.setContent {
            LightThemeProvider {
                TaskCreationScreen(
                    courses = emptyList(),
                    projects = emptyList(),
                    focusProfiles = profiles,
                    initialDateIso = "2026-11-20",
                    initialFocusProfileId = "fp_ai",
                    onCancel = {},
                    onCreateTask = { _, _, _, _, _, _, _, _, _, _, _, focusProfileId, focusProfileTitle, _ ->
                        capturedProfileId = focusProfileId
                        capturedProfileTitle = focusProfileTitle
                    }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("new_task_title_input").performTextInput("Coder un ResNet")
        composeRule.onNodeWithTag("save_new_task_button").performClick()
        composeRule.waitForIdle()

        assertEquals("fp_ai", capturedProfileId)
        assertEquals("Apprendre PyTorch", capturedProfileTitle)
    }
}
