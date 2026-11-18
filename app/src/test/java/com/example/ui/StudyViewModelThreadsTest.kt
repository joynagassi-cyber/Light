package com.example.ui

import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.AgentConversationThread
import com.example.data.preferences.AgentThreadsStore
import com.example.data.preferences.ThemePreferencesStore
import com.example.data.repository.AgentChatMessage
import com.example.data.repository.AiPlannerRepository
import com.example.data.repository.LocalTestStudyRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StudyViewModelThreadsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun waitUntil(timeoutMs: Long = 3_000L, predicate: () -> Boolean) {
        val deadlineNanos = System.nanoTime() + timeoutMs * 1_000_000L
        while (!predicate() && System.nanoTime() < deadlineNanos) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(15L)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun persistedThreads_restoreSwitchCreateClearAndDelete() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dir = tempFolder.newFolder("vm_threads")
        val threadsStore = AgentThreadsStore(dir, "uid_threads")
        threadsStore.saveThreads(
            listOf(
                AgentConversationThread(
                    id = "thread_a",
                    title = "Planning examen",
                    updatedAtMillis = 2_000L,
                    messages = listOf(
                        AgentChatMessage(id = "u1", isUser = true, text = "Prépare mes révisions GL"),
                        AgentChatMessage(id = "a1", isUser = false, text = "Voici deux créneaux GL.")
                    )
                ),
                AgentConversationThread(
                    id = "thread_b",
                    title = "Chronomètre Focus",
                    updatedAtMillis = 1_000L,
                    messages = listOf(
                        AgentChatMessage(id = "u2", isUser = true, text = "Lance un pomodoro"),
                        AgentChatMessage(id = "a2", isUser = false, text = "Pomodoro 25m prêt.")
                    )
                )
            )
        )

        val vm = StudyViewModel(
            repository = LocalTestStudyRepository(),
            aiPlannerRepository = AiPlannerRepository(),
            themePreferencesStore = ThemePreferencesStore(context),
            currentUserId = "uid_threads",
            initialDisplayName = "Nadia",
            initialEmail = "nadia@test.fr",
            isLocalMode = true,
            agentThreadsStore = threadsStore
        )

        waitUntil { vm.activeAgentThreadId.value == "thread_a" }
        assertEquals("thread_a", vm.activeAgentThreadId.value)
        assertEquals(2, vm.agentThreads.value.size)
        assertEquals("Prépare mes révisions GL", vm.agentMessages.value.first().text)

        // Switch to thread B
        vm.selectAgentThread("thread_b")
        assertEquals("thread_b", vm.activeAgentThreadId.value)
        assertEquals("Pomodoro 25m prêt.", vm.agentMessages.value.last().text)

        // Start a new fresh thread since thread B has user messages
        vm.startNewAgentThread()
        val freshThreadId = vm.activeAgentThreadId.value
        assertNotEquals("thread_b", freshThreadId)
        assertEquals(3, vm.agentThreads.value.size)
        assertTrue(vm.agentMessages.value.none { it.isUser })

        // Calling startNewAgentThread again while already on an empty thread shouldn't duplicate empty threads
        vm.startNewAgentThread()
        assertEquals(freshThreadId, vm.activeAgentThreadId.value)
        assertEquals(3, vm.agentThreads.value.size)

        // Delete active fresh thread -> falls back to top remaining thread
        vm.deleteAgentThread(freshThreadId)
        assertEquals(2, vm.agentThreads.value.size)
        assertEquals("thread_a", vm.activeAgentThreadId.value)

        // Clear active thread resets its title & messages
        vm.clearAgentConversation()
        assertEquals("Nouvelle discussion", vm.agentThreads.value.first { it.id == "thread_a" }.title)
        assertTrue(vm.agentMessages.value.none { it.isUser })

        // Wait briefly for background persist and verify store survived
        waitUntil {
            runBlocking {
                threadsStore.loadThreads().firstOrNull { it.id == "thread_a" }?.title == "Nouvelle discussion"
            }
        }
        val persistedAfterClear = threadsStore.loadThreads()
        assertEquals(2, persistedAfterClear.size)
        assertEquals("Nouvelle discussion", persistedAfterClear.first { it.id == "thread_a" }.title)
    }
}
