package com.example.data.repository

import com.example.data.model.AgentCapabilities
import com.example.data.model.AgentReflection
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class AgentTurnActivityTest {
    @Test fun aMetadataOnlyLastChunkDoesNotTruncateAnswerOrDropCalls() {
        val stream = AgentReplyBuffer<Pair<String, String>> { it.first }
        stream.append("Première ", listOf("id1" to "first"))
        stream.append("ligne\n", listOf("id1" to "updated", "id2" to "other"))
        stream.append(null, emptyList())
        assertTrue(stream.received)
        assertEquals("Première ligne\n", stream.reply)
        assertEquals(listOf("id1" to "updated", "id2" to "other"), stream.requestedCalls)
    }

    @Test fun unknownArgumentsAndDeniedWritesAreNotReportedAsRunning() = runBlocking {
        val repo = AiAgentRepository()
        val phases = mutableListOf<AgentActivity>()
        val type = StudyAgentToolExecutor::class.java
        val executor = Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { _, method, _ ->
            if (method.name == "requestApproval") false else error("Should not execute ${method.name}")
        } as StudyAgentToolExecutor
        val caps = setOf(AgentCapabilities.COMPLETE_TASKS)
        val (_, rejected) = repo.runObservedTool("complete_task", mapOf("taskQuery" to JsonPrimitive("Relire")), executor, caps) { phases += it }
        assertEquals(listOf(AgentTurnPhase.CHECKING_TOOL, AgentTurnPhase.AWAITING_APPROVAL, AgentTurnPhase.RESUMING), phases.map { it.phase })
        assertFalse(rejected.succeeded)
        assertTrue((rejected.elapsedMillis ?: -1L) >= 0)
        phases.clear()
        repo.runObservedTool("instruction_non_fiable", JsonObject(emptyMap()), executor, caps) { phases += it }
        assertTrue(phases.all { it.toolName == null && it.action?.toolName.orEmpty().isEmpty() })
    }

    @Test fun progressBelongsOnlyToItsTurnAndReflectionKeepsLegacyPreferences() {
        val old = AgentTurnProgress("old", 10L)
        val current = AgentTurnProgress("new", 20L)
        assertFalse(current.belongsTo(old.messageId))
        assertTrue(current.belongsTo("new"))
        val action = AgentActionLog("list_tasks", "Planning consulté", elapsedMillis = 20L)
        assertEquals(listOf(action), current.record(AgentActivity(AgentTurnPhase.RESUMING, action = action)).tools)
        assertEquals(AgentReflection.LOW, AgentReflection.fromKey("quick"))
        assertEquals(AgentReflection.MEDIUM, AgentReflection.fromKey("balanced"))
        assertEquals(AgentReflection.HIGH, AgentReflection.fromKey("deep"))
        assertEquals(AgentReflection.MAX, AgentReflection.fromKey("max"))
        assertTrue("deux revues" in AgentReflection.MAX.instruction)
    }
}
