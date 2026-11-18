package com.example.data.preferences

import com.example.data.repository.AgentActionLog
import com.example.data.repository.AgentChatMessage
import com.example.data.repository.AgentPlanPreview
import com.example.data.repository.AgentPlanPreviewItem
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AgentThreadsStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun saveAndLoadThreads_preservesMessagesActionsAndPlanPreview() = runBlocking {
        val dir = tempFolder.newFolder("threads")
        val store = AgentThreadsStore(dir, "user@test.light")

        val plan = AgentPlanPreview(
            title = "Sprint Révisions GL-301",
            summary = "2 blocs ciblés",
            items = listOf(
                AgentPlanPreviewItem(
                    title = "Relire patterns",
                    detail = "Factory + Strategy",
                    dateIso = "2026-11-18",
                    estimatedMinutes = 45,
                    priority = "high"
                )
            )
        )
        val threads = listOf(
            AgentConversationThread(
                id = "t_older",
                title = "Ancienne discussion",
                updatedAtMillis = 1_000L,
                messages = listOf(
                    AgentChatMessage(id = "m1", isUser = true, text = "Bonjour"),
                    AgentChatMessage(id = "m2", isUser = false, text = "Salut !", isError = false)
                )
            ),
            AgentConversationThread(
                id = "t_newer",
                title = "Plan GL-301",
                updatedAtMillis = 2_000L,
                messages = listOf(
                    AgentChatMessage(id = "m3", isUser = true, text = "Organise ma semaine"),
                    AgentChatMessage(
                        id = "m4",
                        isUser = false,
                        text = "Voici un plan structuré.",
                        actions = listOf(AgentActionLog("createTask", "Création de tâche", succeeded = true)),
                        planPreview = plan
                    )
                )
            )
        )

        store.saveThreads(threads)
        val loaded = store.loadThreads()

        assertEquals(2, loaded.size)
        // Sorted by updatedAtMillis descending
        assertEquals("t_newer", loaded[0].id)
        assertEquals("Plan GL-301", loaded[0].title)
        assertEquals("t_older", loaded[1].id)

        val assistantMsg = loaded[0].messages[1]
        assertFalse(assistantMsg.isUser)
        assertEquals(1, assistantMsg.actions.size)
        assertEquals("createTask", assistantMsg.actions[0].toolName)
        assertTrue(assistantMsg.actions[0].succeeded)
        assertEquals("Sprint Révisions GL-301", assistantMsg.planPreview?.title)
        assertEquals(45, assistantMsg.planPreview?.items?.single()?.estimatedMinutes)
    }

    @Test
    fun store_sanitizesUserId_isolatesUsers_andEnforcesCaps() = runBlocking {
        val dir = tempFolder.newFolder("caps")
        val unsafeUserStore = AgentThreadsStore(dir, "alice/../bob:123")
        val otherUserStore = AgentThreadsStore(dir, "claire")

        val tooManyMessages = (1..150).map { idx ->
            AgentChatMessage(id = "msg_$idx", isUser = idx % 2 == 1, text = "Message #$idx")
        }
        val tooManyThreads = (1..50).map { idx ->
            AgentConversationThread(
                id = "thread_$idx",
                title = "Fil $idx",
                updatedAtMillis = idx.toLong(),
                messages = if (idx == 50) tooManyMessages else listOf(AgentChatMessage(isUser = true, text = "Hello $idx"))
            )
        }

        unsafeUserStore.saveThreads(tooManyThreads)
        otherUserStore.saveThreads(
            listOf(AgentConversationThread(id = "claire_1", title = "Claire", updatedAtMillis = 99L, messages = emptyList()))
        )

        assertTrue(File(dir, "agent_threads_alice_.._bob_123.json").exists())
        val loadedAlice = unsafeUserStore.loadThreads()
        assertEquals(40, loadedAlice.size)
        assertEquals("thread_50", loadedAlice.first().id)
        assertEquals(120, loadedAlice.first().messages.size)
        assertEquals("msg_31", loadedAlice.first().messages.first().id)

        val loadedClaire = otherUserStore.loadThreads()
        assertEquals(1, loadedClaire.size)
        assertEquals("claire_1", loadedClaire.single().id)
    }
}
