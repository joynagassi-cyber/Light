package com.example.ui.screens

import com.example.data.model.StudyTask
import com.example.data.model.wouldCreateTaskCycle
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class GanttMoveAndDependenciesTest {
    @Test fun detectsDirectAndTransitiveCyclesWithoutLoopingOnExistingBadData() {
        val graph = listOf(
            StudyTask(id = "draft"), StudyTask(id = "design", predecessorIds = listOf("draft")),
            StudyTask(id = "review", predecessorIds = listOf("design")),
            StudyTask(id = "x", predecessorIds = listOf("y")), StudyTask(id = "y", predecessorIds = listOf("x", "missing")),
        ).associateBy { it.id }
        assertTrue(wouldCreateTaskCycle("draft", "draft", graph))
        assertTrue(wouldCreateTaskCycle("draft", "design", graph))
        assertTrue(wouldCreateTaskCycle("draft", "review", graph))
        assertFalse(wouldCreateTaskCycle("draft", "x", graph))
        assertFalse(wouldCreateTaskCycle("review", "draft", graph))
    }

    @Test fun dragSnapsToTheScaleAndLeavesTheTimeUnchangedOnDateMoves() {
        val date = LocalDate.parse("2026-11-19")
        val span = StudyTask(startDateIso = date.toString(), dueDateIso = date.toString(), scheduledTime = "09:30").ganttSpan()!!
        assertNull(ganttDragMove(span, GanttScale.WEEK, 0.49f))
        assertEquals(GanttMove(date.plusDays(2), visualColumns = 2f), ganttDragMove(span, GanttScale.WEEK, 1.7f))
        assertEquals(GanttMove(date.minusDays(7), visualColumns = -1f), ganttDragMove(span, GanttScale.QUARTER, -1f))
        assertEquals(GanttMove(date, "10:30", 1f), ganttDragMove(span, GanttScale.DAY, 1f))
        assertEquals("23:30", ganttDragMove(span, GanttScale.DAY, 40f)?.time)
        assertNull(ganttDragMove(span.copy(endInclusive = date.plusDays(1)), GanttScale.DAY, 1f))
    }

    @Test fun quarterLastDayRendersAndMilestoneAppearsOnlyOnItsDueDay() {
        val last = LocalDate.parse("2026-12-31")
        val window = ganttWindow(last, GanttScale.QUARTER)
        val lastDay = StudyTask(dueDateIso = last.toString())
        val (start, end) = window.ganttBarRange(lastDay, GanttScale.QUARTER)!!
        assertTrue(start in 12f..<13f)
        assertEquals(13f, end, 0.001f)
        val marker = lastDay.copy(startDateIso = last.minusDays(3).toString(), category = "milestone", scheduledTime = "14:30")
        assertFalse(ganttWindow(last.minusDays(1), GanttScale.DAY).contains(marker))
        assertEquals(14.5f, ganttWindow(last, GanttScale.DAY).ganttBarRange(marker, GanttScale.DAY)!!.first, 0.001f)
    }
}
