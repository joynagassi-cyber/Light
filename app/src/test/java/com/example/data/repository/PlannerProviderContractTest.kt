package com.example.data.repository

import com.example.data.model.AgentProvider
import com.example.data.preferences.StudyAppSettings
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PlannerProviderContractTest {
    @Test fun anthropicUsesMessagesWithTheModelFromSettings() {
        val request = buildPlannerProviderRequest("Objectif", StudyAppSettings(
            agentProvider = AgentProvider.ANTHROPIC.id,
            agentBaseUrl = "https://api.anthropic.com/v1/chat/completions/",
            agentModel = "  actif-configuré  ",
        ))
        assertEquals("https://api.anthropic.com/v1/messages", request.endpoint)
        assertEquals(AgentProvider.ANTHROPIC.id, request.providerId)
        assertEquals("actif-configuré", request.payload["model"]!!.jsonPrimitive.content)
        assertTrue(request.payload["max_tokens"]!!.jsonPrimitive.int > 0)
        assertEquals("Objectif", request.payload["messages"]!!.jsonArray.single().jsonObject["content"]!!.jsonPrimitive.content)
        assertFalse(request.payload.containsKey("tools"))
        val response = Json.parseToJsonElement("""{"content":[{"type":"thinking","thinking":"privé"},{"type":"text","text":"plan"}],"stop_reason":"end_turn"}""").jsonObject
        assertEquals("plan", plannerProviderText(response, request.providerId))
    }

    @Test fun compatibleProviderKeepsItsConfiguredPathAndChatCompletionEnvelope() {
        val request = buildPlannerProviderRequest("Objectif", StudyAppSettings(
            agentProvider = AgentProvider.OPENROUTER.id,
            agentBaseUrl = "https://openrouter.ai/api/v1/",
            agentModel = "fixture/modele",
        ))
        assertEquals("https://openrouter.ai/api/v1/chat/completions", request.endpoint)
        assertFalse(request.payload.containsKey("max_tokens"))
        val response = Json.parseToJsonElement("""{"choices":[{"message":{"content":"plan"}}]}""").jsonObject
        assertEquals("plan", plannerProviderText(response, request.providerId))
    }

    @Test fun rejectsUnsafeEndpointsEmptyResponsesAndMissingKey() {
        val settings = StudyAppSettings(agentProvider = AgentProvider.ANTHROPIC.id, agentModel = "fixture")
        for (url in listOf("http://example.org", "https://user@example.org", "https://example.org/?query=1")) {
            assertThrows(IllegalArgumentException::class.java) { buildPlannerProviderRequest("Objectif", settings.copy(agentBaseUrl = url)) }
        }
        val short = Json.parseToJsonElement("""{"stop_reason":"max_tokens","content":[{"type":"text","text":"tronqué"}]}""").jsonObject
        assertThrows(IllegalArgumentException::class.java) { plannerProviderText(short, settings.agentProvider) }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { AiAgentRepository().completePlannerPrompt("Objectif", settings, " ") }
        }
    }
}
