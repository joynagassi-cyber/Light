package com.example.data.repository

import com.example.data.model.AgentCapabilities
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class AgentToolContractTest {
    private val fixtures = mapOf(
        "list_tasks" to ("listTasks" to """{"filter":"pending","dateIso":"2026-11-19"}"""),
        "list_study_data" to ("listStudyData" to "{}"),
        "present_study_plan" to ("presentStudyPlan" to """{"title":"Plan","summary":"Résumé","items":[{"title":"Lire","detail":"Chapitre 1","dateIso":"","estimatedMinutes":0,"priority":"medium"}]}"""),
        "create_task" to ("createTask" to """{"title":"Lire","courseCode":"","category":"revision","priority":"high","dueDateIso":"2026-11-19","estimatedMinutes":25}"""),
        "complete_task" to ("completeTask" to """{"taskQuery":"Lire"}"""),
        "delete_task" to ("deleteTask" to """{"taskQuery":"Lire"}"""),
        "add_course" to ("addCourse" to """{"code":"INF-101","title":"Algorithmique"}"""),
        "create_project" to ("createProject" to """{"title":"Rapport","startDateIso":"2026-11-18","dueDateIso":"2026-11-20"}"""),
        "set_gradient_theme" to ("setGradientTheme" to """{"gradientId":"aurore_lavande"}"""),
        "start_focus_timer" to ("startFocusTimer" to """{"mode":"pomodoro"}"""),
        "create_habit" to ("createHabit" to """{"title":"Lire"}"""),
        "create_countdown" to ("createCountdown" to """{"title":"Examen","targetDateIso":"2026-11-20"}"""),
        "web_search" to ("webSearch" to """{"query":"révision espacée","maxResults":3}"""),
        "fetch_page" to ("fetchPage" to """{"url":"https://example.org/article"}"""),
        "academic_search" to ("academicSearch" to """{"query":"spaced repetition","maxResults":3}"""),
    )

    private fun fakeExecutor(calls: MutableList<String>, approves: Boolean): StudyAgentToolExecutor {
        val type = StudyAgentToolExecutor::class.java
        return Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { _, method, _ ->
            calls.add(method.name)
            if (method.name == "requestApproval") approves else JsonObject(emptyMap())
        } as StudyAgentToolExecutor
    }

    @Test fun aValidScenarioForEveryToolReachesOnlyItsTypedExecutor() = runBlocking {
        val repo = AiAgentRepository()
        val caps = AgentCapabilities.all.map { it.first }.toSet()
        assertEquals(caps, AgentToolContract.tools.map { it.capability }.toSet())
        assertEquals(fixtures.keys, AgentToolContract.tools.map { it.name }.toSet())
        for (spec in AgentToolContract.tools) {
            val (method, json) = fixtures.getValue(spec.name)
            val calls = mutableListOf<String>()
            val result = repo.executeTool(spec.name, Json.parseToJsonElement(json).jsonObject, fakeExecutor(calls, true), caps)
            assertTrue(spec.name, agentToolResultSucceeded(result.first))
            assertEquals(spec.name, if (spec.requiresApproval) listOf("requestApproval", method) else listOf(method), calls)
            assertNotNull(spec.firebaseDeclaration())
            assertEquals(spec.name, spec.httpDeclaration().getValue("function").jsonObject.getValue("name").jsonPrimitive.content)
        }
    }

    @Test fun malformedDisabledAndRejectedCallsNeverWrite() = runBlocking {
        val repo = AiAgentRepository()
        val calls = mutableListOf<String>()
        val executor = fakeExecutor(calls, false)
        val cap = setOf(AgentCapabilities.CREATE_TASKS, AgentCapabilities.READ_STUDY_DATA)
        val valid = fixtures.getValue("create_task").second
        for (invalid in listOf(valid.replace(":25", ":\"25\""), valid.replace("2026-11-19", "2026-02-30"),
            valid.replace(":25", ":999"), valid.replace("\"Lire\"", "[]"), valid.dropLast(1) + ",\"private key\":true}")) {
            val args = Json.parseToJsonElement(invalid).jsonObject
            assertFalse(agentToolResultSucceeded(repo.executeTool("create_task", args, executor, cap).first))
        }
        assertFalse(agentToolResultSucceeded(repo.executeTool("list_study_data", null, executor, cap).first))
        assertFalse(agentToolResultSucceeded(repo.executeTool("delete_task", Json.parseToJsonElement("""{"taskQuery":"Lire"}""").jsonObject, executor, cap).first))
        assertEquals(emptyList<String>(), calls)
        val rejected = repo.executeTool("create_task", Json.parseToJsonElement(valid).jsonObject, executor, cap).first
        assertFalse(agentToolResultSucceeded(rejected))
        assertNotNull(rejected["cancelled"])
        assertEquals(listOf("requestApproval"), calls)
    }
}
