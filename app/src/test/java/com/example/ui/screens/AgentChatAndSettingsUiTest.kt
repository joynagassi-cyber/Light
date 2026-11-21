package com.example.ui.screens

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollToNode
import com.example.data.preferences.AgentConversationThread
import com.example.data.preferences.StudyAppSettings
import com.example.data.repository.AgentChatMessage
import com.example.ui.theme.LightThemeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AgentChatAndSettingsUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun agentChat_menuUsesSkillForOneMessage_andDrawerListsThreads() {
        var selectedThread: String? = null
        var newThreadClicked = false
        val sent = mutableListOf<Pair<String, String?>>()
        val threads = listOf(
            AgentConversationThread(
                id = "t1",
                title = "Révisions Maths",
                updatedAtMillis = 2000L,
                messages = listOf(AgentChatMessage(isUser = true, text = "Planifie 2h de maths"))
            ),
            AgentConversationThread(
                id = "t2",
                title = "Projet Android",
                updatedAtMillis = 1000L,
                messages = listOf(AgentChatMessage(isUser = true, text = "Découpe le backlog"))
            )
        )

        composeRule.setContent {
            LightThemeProvider {
                AgentChatScreen(
                    messages = listOf(AgentChatMessage(isUser = false, text = "Bonjour !")),
                    threads = threads,
                    activeThreadId = "t1",
                    isBusy = false,
                    pendingAction = null,
                    onResolveAgentAction = { _, _ -> },
                    onSendMessage = { prompt, _, skill -> sent += prompt to skill },
                    onClearConversation = {},
                    onSelectThread = { selectedThread = it },
                    onNewThread = { newThreadClicked = true },
                    onDeleteThread = {},
                    onOpenSettings = {},
                    settings = StudyAppSettings(),
                    apiKeyConfigured = false,
                    agentModels = emptyList(),
                    agentModelsLoading = false,
                    agentModelsError = null,
                    agentUsage = emptyList(),
                    onClearAgentUsage = {},
                    onLoadAgentModels = { _, _, _ -> },
                    onSaveAgentSettings = { _, _, _, _, _, _, _, _ -> }
                )
            }
        }

        composeRule.waitForIdle()
        // Upload tray is NOT hardcoded/visible before clicking '+'
        composeRule.onAllNodesWithTag("agent_upload_tray").assertCountEquals(0)

        // Clicking '+' reveals the upload tray
        composeRule.onNodeWithTag("agent_attach_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("agent_upload_tray").assertIsDisplayed()
        composeRule.onNodeWithTag("agent_pick_files_button").assertIsDisplayed()

        // Clicking '+' again hides it
        composeRule.onNodeWithTag("agent_attach_button").performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag("agent_upload_tray").assertCountEquals(0)

        composeRule.onNodeWithTag("agent_attach_button").performClick()
        composeRule.onNodeWithTag("agent_pick_skill_button").performClick()
        composeRule.onNodeWithTag("agent_skill_light:study").performClick()
        composeRule.onNodeWithTag("agent_selected_skill").assertIsDisplayed()
        composeRule.onNodeWithTag("agent_chat_input").performTextInput("Les graphes")
        composeRule.onNodeWithTag("agent_send_button").performClick()
        composeRule.onNodeWithTag("agent_selected_skill").assertDoesNotExist()
        composeRule.onNodeWithTag("agent_chat_input").performTextInput("Et ensuite ?")
        composeRule.onNodeWithTag("agent_send_button").performClick()
        assertEquals(listOf("Les graphes" to "light:study", "Et ensuite ?" to null), sent)

        // Clicking top-left menu button opens the conversation threads drawer
        composeRule.onNodeWithTag("agent_threads_drawer_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("agent_new_thread_button").assertIsDisplayed()
        composeRule.onNodeWithTag("agent_thread_item_t2").performClick()
        composeRule.waitForIdle()
        assertEquals("t2", selectedThread)

        // Reopen drawer and click new thread button
        composeRule.onNodeWithTag("agent_threads_drawer_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("agent_new_thread_button").performClick()
        composeRule.waitForIdle()
        assertTrue(newThreadClicked)
    }

    @Test
    fun settingsScreen_themesAndGradientsAreCollapsedUntilExpanded() {
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
                    onClose = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Compte").assertIsDisplayed()
        composeRule.onNodeWithText("Général").assertIsDisplayed()
        val firstThemeTag = "theme_preset_${com.example.ui.theme.LightColorThemes.all.first().id}"
        // Theme presets stay collapsed until toggled
        composeRule.onAllNodesWithTag(firstThemeTag).assertCountEquals(0)

        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("settings_expand_themes"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_expand_themes").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag(firstThemeTag))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(firstThemeTag).assertIsDisplayed()
    }
}
