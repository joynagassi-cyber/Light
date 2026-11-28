package com.example.ui.components

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.preferences.AgentConversationThread
import com.example.data.repository.AgentChatMessage
import com.example.ui.theme.LightThemeProvider
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.ui.component.AIConversationAssistantMessageParams
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h915dp-xxhdpi")
class AgentReadingUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun selectingTodayHidesOlderThreadsWithoutChangingTheActiveThread() {
        val now = AgentConversationThread(id = "today", title = "Actuelle")
        val old = AgentConversationThread(id = "old", title = "Ancienne", updatedAtMillis = 946684800000)
        var selection: String? = null
        compose.setContent {
            LightThemeProvider {
                ModalNavigationDrawer(drawerState = rememberDrawerState(DrawerValue.Open), drawerContent = {
                    AgentThreadsDrawerSheet(listOf(old, now), "old", { selection = it }, {}, {}, {})
                }, content = {})
            }
        }
        compose.onNodeWithTag("agent_thread_item_old").assertIsDisplayed()
        compose.onNodeWithTag("agent_history_TODAY").performClick()
        compose.onNodeWithTag("agent_thread_item_old").assertDoesNotExist()
        compose.onNodeWithTag("agent_thread_item_today").assertIsDisplayed()
        compose.runOnIdle { assertEquals(null, selection) }
        compose.onNodeWithTag("agent_thread_item_today").performClick()
        compose.runOnIdle { assertEquals("today", selection) }
    }

    @Test fun longMessageOpensPagedRawReaderWithAnExplicitExport() {
        val message = AgentChatMessage(isUser = false, text = "a".repeat(6000) + "<script>inerte</script>")
        val readerOpen = mutableStateOf(false)
        compose.setContent {
            LightThemeProvider {
                val lightMessage = LightAgentMessage(
                    id = message.id,
                    role = AIMessageRole.Assistant,
                    text = message.text,
                    isGenerating = false,
                    parts = emptyList(),
                    isError = false,
                    planPreview = null,
                    elapsedMillis = null,
                )
                LightAgentComponentFactory(
                    onOpenDocument = { readerOpen.value = true },
                    onSaveToLibrary = null,
                ).AIConversationAssistantMessage(
                    AIConversationAssistantMessageParams(
                        message = lightMessage,
                        text = lightMessage.text,
                        isGenerating = false,
                        parts = emptyList(),
                        attachments = emptyList(),
                        reasoningText = { null },
                        approver = null,
                    )
                )
                if (readerOpen.value) AgentMessageDocumentReader(message) { readerOpen.value = false }
            }
        }
        compose.onNodeWithTag("agent_message_content").assertDoesNotExist()
        compose.onNodeWithTag("agent_open_document").performClick()
        compose.onNodeWithTag("agent_export_document").assertIsDisplayed()
        compose.onNodeWithTag("agent_document_next").performClick()
        compose.onNodeWithText("<script>inerte</script>").assertIsDisplayed()
        compose.onNodeWithText("Fermer").performClick()
        compose.onNodeWithTag("agent_document_page").assertDoesNotExist()
    }
}
