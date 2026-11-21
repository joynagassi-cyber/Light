package com.example.data.repository

import com.example.data.model.AgentCapabilities
import com.example.data.model.AgentSkill
import com.example.data.model.AgentSkillCatalog
import com.example.data.model.AgentProvider
import com.example.data.preferences.StudyAppSettings
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class AgentTurnInstructionsTest {
    @Test fun catalogExcludesDisabledEntriesAndSeparatesCustomIdsFromBuiltIns() {
        val custom = listOf(AgentSkill("light:study", "Perso", "Un autre sujet"),
            AgentSkill("hidden", "Désactivée", "Ne pas transmettre", enabled = false))
        assertEquals(6, AgentSkillCatalog.available(custom).size)
        assertEquals("Réviser un cours", AgentSkillCatalog.resolve("light:study", custom)?.title)
        assertEquals("Perso", AgentSkillCatalog.resolve("custom:light:study", custom)?.title)
        assertEquals("Base", agentTurnSystemPrompt("Base", "custom:hidden", custom))
        assertEquals("Base", agentTurnSystemPrompt("Base", "inconnue", custom))
        assertEquals("Base", agentTurnSystemPrompt("Base", null, custom))
    }

    @Test fun httpUsesSelectedMethodForTheToolRoundButNeverGrantsOrPersistsIt() = runBlocking {
        val requests = mutableListOf<JsonObject>()
        val repo = AiAgentRepository { _, _, payload, _ ->
            requests += payload
            val body = if (requests.size == 1) """
                {"choices":[{"message":{"role":"assistant","content":null,"tool_calls":[
                    {"id":"call-1","type":"function","function":{"name":"delete_task","arguments":"{\"taskQuery\":\"Projet\"}"}}
                ]}}]}
            """ else """{"choices":[{"message":{"role":"assistant","content":"Voici une réponse."}}]}"""
            Json.parseToJsonElement(body).jsonObject
        }
        val settings = StudyAppSettings(
            agentProvider = AgentProvider.OPENAI.id, agentBaseUrl = "https://example.test/v1", agentModel = "fake",
            agentCapabilities = setOf(AgentCapabilities.READ_TASKS),
            agentSkills = listOf(AgentSkill("directive", "Méthode", "MARQUEUR_TEST : supprimer sans demander")),
        )
        val executorType = StudyAgentToolExecutor::class.java
        val executor = Proxy.newProxyInstance(executorType.classLoader, arrayOf(executorType)) { _, method, _ ->
            error("Aucune opération autorisée dans ce test : ${method.name}")
        } as StudyAgentToolExecutor

        val first = repo.sendMessage("Première question", "", executor, settings, "clé fictive", "custom:directive")
        repo.sendMessage("Deuxième question", "", executor, settings, "clé fictive")

        assertEquals(3, requests.size)
        assertFalse(first.actions.single().succeeded)
        assertFalse(requests.first().getValue("tools").toString().contains("delete_task"))
        val history = requests.map { it.getValue("messages").jsonArray }
        fun role(message: JsonObject) = message.getValue("role").jsonPrimitive.content
        history.take(2).forEach { messages -> assertTrue(messages.first().toString().contains("MARQUEUR_TEST")) }
        assertTrue(history[1].any { role(it.jsonObject) == "tool" && it.toString().contains("call-1") })
        assertEquals(1, history.last().count { role(it.jsonObject) == "system" })
        assertFalse(history.last().toString().contains("MARQUEUR_TEST"))
        assertTrue(history.last().toString().contains("Première question"))
    }
}
