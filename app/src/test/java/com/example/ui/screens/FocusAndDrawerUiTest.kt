package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.example.data.model.Course
import com.example.data.preferences.AgentConversationThread
import com.example.data.preferences.StudyAppSettings
import com.example.data.repository.AgentChatMessage
import com.example.ui.FocusAudioUiState
import com.example.ui.FocusTimerState
import com.example.ui.components.AgentChatTopBar
import com.example.ui.components.AgentThreadsDrawerSheet
import com.example.ui.theme.LightThemeProvider
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h915dp-xxhdpi")
class FocusAndDrawerUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun scaffoldTopBarAndLeftNavigationDrawer_switchCreateAndDeleteThreads() {
        var activeThreadId by mutableStateOf("t1")
        var createdThreadCount = 0
        var deletedThreadId: String? = null
        val threads = listOf(
            AgentConversationThread(
                id = "t1",
                title = "Préparation Examen Algo",
                updatedAtMillis = 2000L,
                messages = listOf(AgentChatMessage(isUser = true, text = "Fais-moi un plan"))
            ),
            AgentConversationThread(
                id = "t2",
                title = "Architecture Compose",
                updatedAtMillis = 1000L,
                messages = listOf(AgentChatMessage(isUser = true, text = "Organise mes révisions"))
            )
        )

        composeRule.setContent {
            LightThemeProvider {
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val activeTitle = threads.firstOrNull { it.id == activeThreadId }?.title ?: "Nouveau fil"

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AgentThreadsDrawerSheet(
                            threads = threads,
                            activeThreadId = activeThreadId,
                            onSelectThread = { activeThreadId = it },
                            onNewThread = { createdThreadCount++ },
                            onDeleteThread = { deletedThreadId = it },
                            onCloseDrawer = { scope.launch { drawerState.close() } }
                        )
                    }
                ) {
                    Scaffold(
                        topBar = {
                            AgentChatTopBar(
                                activeThreadTitle = activeTitle,
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onClearConversation = {},
                                onOpenAgentConfig = {},
                                onOpenSettings = {}
                            )
                        }
                    ) { innerPadding ->
                        AgentChatScreen(
                            messages = threads.first { it.id == activeThreadId }.messages,
                            threads = threads,
                            activeThreadId = activeThreadId,
                            isBusy = false,
                            pendingAction = null,
                            onResolveAgentAction = { _, _ -> },
                            onSendMessage = { _, _, _ -> },
                            onClearConversation = {},
                            onSelectThread = { activeThreadId = it },
                            onNewThread = { createdThreadCount++ },
                            onDeleteThread = { deletedThreadId = it },
                            onOpenSettings = {},
                            settings = StudyAppSettings(),
                            apiKeyConfigured = false,
                            agentModels = emptyList(),
                            agentModelsLoading = false,
                            agentModelsError = null,
                            agentUsage = emptyList(),
                            onClearAgentUsage = {},
                            onLoadAgentModels = { _, _, _ -> },
                            onSaveAgentSettings = { _, _, _, _, _, _, _, _ -> },
                            showEmbeddedNavigationChrome = false,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                }
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("agent_top_app_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("agent_top_bar_thread_title").assertIsDisplayed()

        // Open drawer from TopAppBar navigation icon and switch thread
        composeRule.onNodeWithTag("agent_threads_drawer_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("agent_threads_drawer_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("agent_thread_item_t2").performClick()
        composeRule.waitForIdle()
        assertEquals("t2", activeThreadId)

        // Reopen drawer, create a new thread, and delete an old thread
        composeRule.onNodeWithTag("agent_threads_drawer_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("agent_new_thread_button").performClick()
        composeRule.waitForIdle()
        assertEquals(1, createdThreadCount)

        composeRule.onNodeWithTag("agent_threads_drawer_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("agent_delete_thread_t1").performClick()
        composeRule.waitForIdle()
        assertEquals("t1", deletedThreadId)
    }

    @Test
    fun focusScreen_heroCardClean_andCustomizationSeparatedInOverflowMenu() {
        var selectedIllustration by mutableStateOf("student")
        var selectedTheme by mutableStateOf("lunar")
        var audioPlaying by mutableStateOf(false)

        composeRule.setContent {
            LightThemeProvider {
                FocusScreen(
                    focusState = FocusTimerState(),
                    audioState = FocusAudioUiState(isPlaying = audioPlaying),
                    appSettings = StudyAppSettings(
                        focusIllustrationId = selectedIllustration,
                        focusThemeId = selectedTheme
                    ),
                    courses = listOf(Course(id = "c1", code = "INF301", title = "Algorithmes")),
                    sessions = emptyList(),
                    onSelectKind = {},
                    onSelectMode = {},
                    onSelectCourse = {},
                    onTogglePlayPause = {},
                    onRecordLap = {},
                    onReset = {},
                    onFinishAndLog = {},
                    onSelectAudioTrack = {},
                    onToggleAudioPlayPause = { audioPlaying = !audioPlaying },
                    onAudioVolumeChange = {},
                    onToggleAutoPlayAudio = {},
                    onSelectFocusIllustration = { selectedIllustration = it },
                    onSelectFocusTheme = { selectedTheme = it },
                    onOpenSettings = {}
                )
            }
        }

        composeRule.waitForIdle()
        // Hero card, clock, sub-modes, and quick audio pill are cleanly shown above the fold
        composeRule.onNodeWithTag("focus_hero_card").assertIsDisplayed()
        composeRule.onNodeWithTag("focus_clock_display").assertIsDisplayed()
        composeRule.onNodeWithText("25:00").assertIsDisplayed()
        composeRule.onNodeWithTag("focus_mode_short_break").assertIsDisplayed()
        composeRule.onNodeWithTag("focus_play_pause_button").assertIsDisplayed()
        composeRule.onNodeWithTag("focus_audio_quick_pill").assertIsDisplayed()

        // Inline customization & audio cards must NOT clutter the main Focus screen
        composeRule.onAllNodesWithTag("focus_customization_sheet").assertCountEquals(0)
        composeRule.onAllNodesWithTag("focus_illustration_coder").assertCountEquals(0)
        composeRule.onAllNodesWithTag("focus_theme_ocean").assertCountEquals(0)
        composeRule.onAllNodesWithTag("audio_play_pause_button").assertCountEquals(0)

        // Open 3-dot menu -> customize sheet
        composeRule.onNodeWithTag("focus_more_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("focus_menu_customize").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("focus_customization_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("focus_illustration_coder").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("focus_theme_ocean").performScrollTo().assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        assertEquals("coder", selectedIllustration)
        assertEquals("ocean", selectedTheme)

        // Switch to Audio tab inside the customization sheet
        composeRule.onNodeWithTag("focus_sheet_tab_audio").performScrollTo().assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("audio_play_pause_button").performScrollTo().assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        assertTrue(audioPlaying)

        // Switch to Timer tab inside the customization sheet
        composeRule.onNodeWithTag("focus_sheet_tab_timer").performScrollTo().assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Pomodoro standard").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun settingsScreen_opensFocusUniversePageFromRow() {
        var focusUniverseOpened = false

        composeRule.setContent {
            LightThemeProvider {
                SettingsScreen(
                    profile = null,
                    photoUrl = null,
                    selectedGradientId = "aurore_lavande",
                    appSettings = StudyAppSettings(),
                    courses = emptyList(),
                    onSelectGradient = {},
                    onUpdateProfile = { _, _, _, _, _, _, _, _ -> },
                    onUpdateTimerDurations = { _, _, _ -> },
                    onUpdateDefaultCalendarView = {},
                    onUpdateHapticsEnabled = {},
                    onUpdateAutoLogStopwatch = {},
                    onUpdateAutoPlayAudioWithFocus = {},
                    onCreateCourse = { _, _, _, _, _, _, _ -> },
                    onDeleteCourse = {},
                    onSignOut = {},
                    onClose = {},
                    onOpenFocusUniverse = { focusUniverseOpened = true }
                )
            }
        }

        composeRule.waitForIdle()
        // The Focus Universe & audio inline section was replaced by a navigation row
        composeRule.onAllNodesWithTag("focus_illustration_planner").assertCountEquals(0)

        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("settings_focus_universe_row"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_focus_universe_row").performClick()
        composeRule.waitForIdle()
        assertTrue(focusUniverseOpened)
    }
}
