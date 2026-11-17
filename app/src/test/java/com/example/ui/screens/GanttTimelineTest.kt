package com.example.ui.screens

import com.example.data.model.StudyProject
import com.example.data.model.StudyTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GanttTimelineTest {
    @Test
    fun windowsCoverDayWeekMonthAndQuarterWithoutOffByOne() {
        val date = LocalDate.parse("2026-11-18")
        val day = ganttWindow(date, GanttScale.DAY)
        assertEquals(24, day.columnCount)
        assertEquals(date, day.start)

        val week = ganttWindow(date, GanttScale.WEEK)
        assertEquals(LocalDate.parse("2026-11-16"), week.start)
        assertEquals(LocalDate.parse("2026-11-22"), week.endInclusive)
        assertEquals(7, week.columnCount)

        val month = ganttWindow(date, GanttScale.MONTH)
        assertEquals(LocalDate.parse("2026-11-01"), month.start)
        assertEquals(LocalDate.parse("2026-11-30"), month.endInclusive)
        assertEquals(30, month.columnCount)

        val quarter = ganttWindow(date, GanttScale.QUARTER)
        assertEquals(LocalDate.parse("2026-10-01"), quarter.start)
        assertEquals(LocalDate.parse("2026-12-31"), quarter.endInclusive)
        assertEquals(13, quarter.columnCount)
    }

    @Test
    fun plannedTaskDatesProduceARealMultiDayBarAndOldTaskFallsBackToDueDate() {
        val task = StudyTask(
            id = "gantt-task",
            title = "Prototype",
            dueDateIso = "2026-11-20",
            startDateIso = "2026-11-17",
            scheduledTime = "09:30",
            estimatedMinutes = 120L
        )
        val span = task.ganttSpan()
        assertNotNull(span)
        assertEquals(LocalDate.parse("2026-11-17"), span!!.start)
        assertEquals(LocalDate.parse("2026-11-20"), span.endInclusive)
        assertEquals(570, span.startMinuteOfDay)
        assertTrue(ganttWindow(LocalDate.parse("2026-11-18"), GanttScale.WEEK).contains(task))
        assertEquals(0f to 24f, ganttWindow(LocalDate.parse("2026-11-18"), GanttScale.DAY).taskTimeColumnRange(task))
        val intraday = task.copy(
            startDateIso = "2026-11-18",
            dueDateIso = "2026-11-18"
        )
        assertEquals(9.5f to 11.5f, ganttWindow(LocalDate.parse("2026-11-18"), GanttScale.DAY).taskTimeColumnRange(intraday))

        val legacy = StudyTask(id = "legacy", title = "Legacy", dueDateIso = "2026-11-20")
        assertEquals(LocalDate.parse("2026-11-20"), legacy.ganttSpan()!!.start)
        assertNull(StudyTask(id = "bad", title = "Invalid", dueDateIso = "not-a-date").ganttSpan())
    }

    @Test
    fun projectSpansClipToTheVisibleWindowAtEachCalendarScale() {
        val project = StudyProject(
            id = "launch",
            title = "Product launch",
            startDateIso = "2026-11-10",
            dueDateIso = "2026-11-18"
        )
        val anchor = LocalDate.parse("2026-11-18")
        assertEquals(0..2, ganttWindow(anchor, GanttScale.WEEK).projectColumnRange(project))
        assertEquals(9..17, ganttWindow(anchor, GanttScale.MONTH).projectColumnRange(project))
        assertEquals(5..6, ganttWindow(anchor, GanttScale.QUARTER).projectColumnRange(project))
        assertEquals(0..23, ganttWindow(anchor, GanttScale.DAY).projectColumnRange(project))

        val invalid = project.copy(dueDateIso = "2026-11-01")
        assertNull(invalid.ganttSpan())
        assertNull(ganttWindow(anchor, GanttScale.WEEK).projectColumnRange(invalid))
    }

    @Test
    fun milestoneIsNotRenderedAsAWorkBar() {
        val milestone = StudyTask(
            id = "release",
            title = "Release candidate",
            dueDateIso = "2026-12-01",
            category = "milestone"
        )
        assertTrue(milestone.ganttSpan()!!.isMilestone)
        assertFalse(ganttWindow(LocalDate.parse("2026-11-01"), GanttScale.MONTH).contains(milestone))
        assertTrue(ganttWindow(LocalDate.parse("2026-12-01"), GanttScale.MONTH).contains(milestone))
    }
}
