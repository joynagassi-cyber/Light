package com.example.data.repository

import com.example.data.model.Course
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AiStudyPlanResponseTest {
    @Test fun responseFieldsAreNormalizedBeforePreview() {
        val today = LocalDate.parse("2026-11-19")
        val deadline = today.plusDays(3)
        val response = """```JSON
            {"tasks":[
              {"title":"  Relire ","dueDateIso":"2026-12-01","scheduledTime":"31:99","courseCode":"mistake","priority":"urgent","estimatedMinutes":999},
              {"title":"Résumer","dueDateIso":"2026-11-20","scheduledTime":"09:30:10","courseCode":"inf301"}
            ]}
            ```""".trimIndent()
        val tasks = decodeStudyPlan(response, today, deadline, listOf(Course(code = "INF301"))).tasks
        assertEquals("Relire", tasks[0].title)
        assertEquals(deadline.toString(), tasks[0].dueDateIso)
        assertEquals("10:00", tasks[0].scheduledTime)
        assertEquals("", tasks[0].courseCode)
        assertEquals("medium", tasks[0].priority)
        assertEquals(120, tasks[0].estimatedMinutes)
        assertEquals("INF301", tasks[1].courseCode)
        assertEquals("09:30", tasks[1].scheduledTime)
    }

    @Test fun unreadablePayloadIsNotEchoedInError() {
        val today = LocalDate.parse("2026-11-19")
        val marker = "donnée privée du fournisseur"
        val error = assertThrows(IllegalArgumentException::class.java) { decodeStudyPlan(marker, today, today, emptyList()) }
        assertFalse(error.message.orEmpty().contains(marker))
        assertThrows(IllegalArgumentException::class.java) {
            decodeStudyPlan("""{"tasks":[{"title":" "}]}""", today, today, emptyList())
        }
    }

    @Test fun configuredExternalTransportGetsThePlannerPromptWithoutFirebase() = runBlocking {
        var requests = 0
        val plan = AiPlannerRepository().generateSmartStudyPlan("Réviser les graphes", LocalDate.now().plusDays(5).toString(), 30,
            courses = emptyList(), modelName = "fixture/model-actif", externalCompletion = { prompt ->
                requests++
                check("Réviser les graphes" in prompt)
                """{"tasks":[{"title":"Faire un exercice"}]}"""
            })
        assertEquals(1, requests)
        assertEquals("Faire un exercice", plan.tasks.single().title)
    }
}
