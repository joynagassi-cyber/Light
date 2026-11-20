package com.example.ui.screens

import android.view.View
import android.view.ViewConfiguration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.example.data.model.StudyTask
import com.example.ui.FocusTimerState
import com.example.ui.theme.AppearanceMode
import com.example.ui.theme.LightThemeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h915dp-xxhdpi")
class PhaseTwoInteractionTest {
    @get:Rule val compose = createComposeRule()

    @Test fun dragGanttBarCommitsOneMoveAndCancelCommitsNone() {
        val dates = mutableListOf<LocalDate>()
        val date = LocalDate.parse("2026-11-19")
        val task = StudyTask(id = "drag", title = "Relire le cours", dueDateIso = date.toString())
        compose.setContent {
            LightThemeProvider {
                Box(Modifier.fillMaxSize()) {
                    GanttTaskBar(task, task.ganttSpan()!!, GanttScale.WEEK, 56.dp, 1f, 2f, Color.Blue) { moved, start, time ->
                        assertEquals(task.id, moved.id)
                        assertEquals(null, time)
                        dates += start
                    }
                }
            }
        }
        val bar = compose.onNodeWithTag("gantt_bar_drag").assertIsDisplayed()
        val longPress = ViewConfiguration.getLongPressTimeout().toLong() + 100
        bar.performTouchInput {
            down(center)
            advanceEventTime(longPress)
            moveBy(Offset(width.toFloat(), 0f))
            cancel()
        }
        compose.runOnIdle { assertTrue(dates.isEmpty()) }
        bar.performTouchInput {
            down(center)
            advanceEventTime(longPress)
            moveBy(Offset(width.toFloat(), 0f))
            up()
        }
        compose.runOnIdle { assertEquals(listOf(date.plusDays(1)), dates) }
    }

    @Test fun exitingImmersiveFocusRestoresScreenFlagButDoesNotStopTimer() {
        var visible by mutableStateOf(true)
        val state = FocusTimerState(isRunning = true, remainingSeconds = 321)
        lateinit var view: View
        compose.setContent {
            view = LocalView.current
            LightThemeProvider(appearanceModeKey = AppearanceMode.OLED.key) {
                if (visible) ImmersiveFocusSession(state) { visible = false }
            }
        }
        compose.onNodeWithTag("immersive_focus_clock").assertIsDisplayed()
        compose.runOnIdle { assertTrue(view.keepScreenOn) }
        compose.onNodeWithTag("exit_immersive_focus").performClick()
        compose.onNodeWithTag("immersive_focus_clock").assertDoesNotExist()
        compose.runOnIdle { assertFalse(view.keepScreenOn); assertTrue(state.isRunning) }
    }
}
