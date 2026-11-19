package com.example.ui.screens

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.example.data.model.Course
import com.example.data.model.FocusProfile
import com.example.data.model.FocusSession
import com.example.data.model.StudyHabit
import com.example.data.model.StudyTask
import com.example.data.preferences.StudyAppSettings
import com.example.ui.components.buildActivityHeatmapGrid
import com.example.ui.components.buildCoursePerformancePoints
import com.example.ui.components.buildDailyVelocitySeries
import com.example.ui.components.buildFiveAxisMasteryScores
import com.example.ui.components.buildModeStackedBars
import com.example.ui.components.buildWeekdayRhythm
import com.example.ui.theme.AppleIndigo
import com.example.ui.theme.AppearanceMode
import com.example.ui.theme.LightThemeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h915dp-xxhdpi")
class FocusAnalyticsAndSettingsUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun aggregateDailyFocusPoints_andComputeStreak_excludeBreaksAndSumCorrectly() {
        val today = LocalDate.of(2026, 11, 18)
        val sessions = listOf(
            FocusSession(id = "s1", courseId = "c1", courseTitle = "Algo", durationMinutes = 25, dateIso = "2026-11-18", mode = "pomodoro"),
            FocusSession(id = "s2", courseId = "c1", courseTitle = "Algo", durationMinutes = 50, dateIso = "2026-11-18", mode = "deep_code"),
            FocusSession(id = "s3", courseId = "c1", courseTitle = "Algo", durationMinutes = 5, dateIso = "2026-11-18", mode = "short_break"),
            FocusSession(id = "s4", courseId = "c2", courseTitle = "BD", durationMinutes = 40, dateIso = "2026-11-17", mode = "stopwatch"),
            FocusSession(id = "s5", courseId = "c2", courseTitle = "BD", durationMinutes = 30, dateIso = "2026-11-15", mode = "pomodoro")
        )

        val points7 = aggregateDailyFocusPoints(sessions = sessions, windowDays = 7, today = today)
        assertEquals(7, points7.size)
        assertEquals(75, points7.last().minutes)
        assertEquals(2, points7.last().sessionCount)
        assertEquals(40, points7[points7.lastIndex - 1].minutes)
        assertEquals(0, points7[points7.lastIndex - 2].minutes)
        assertEquals(30, points7[points7.lastIndex - 3].minutes)

        // Streak counts Nov 18 + Nov 17 (Nov 16 missing -> streak 2)
        assertEquals(2, computeFocusStreakDays(sessions = sessions, today = today))
    }

    @Test
    fun focusAnalyticsScreen_displaysLineChartKpisAndSwitchesTimeWindows() {
        val today = LocalDate.now()
        val sessions = listOf(
            FocusSession(id = "1", courseId = "c1", courseTitle = "Algorithmes", durationMinutes = 50, dateIso = today.toString(), mode = "deep_code"),
            FocusSession(id = "2", courseId = "c2", courseTitle = "Systèmes", durationMinutes = 35, dateIso = today.minusDays(1).toString(), mode = "pomodoro"),
            FocusSession(id = "3", courseId = "c1", courseTitle = "Algorithmes", durationMinutes = 25, dateIso = today.minusDays(10).toString(), mode = "stopwatch")
        )
        val courses = listOf(
            Course(id = "c1", code = "INF301", title = "Algorithmes", colorIndex = 0),
            Course(id = "c2", code = "INF302", title = "Systèmes", colorIndex = 2)
        )

        composeRule.setContent {
            LightThemeProvider {
                FocusAnalyticsScreen(
                    sessions = sessions,
                    courses = courses,
                    onStartFocusSession = {},
                    onOpenSettings = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("focus_analytics_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("focus_analytics_line_chart").assertIsDisplayed()
        composeRule.onNodeWithTag("analytics_kpi_total").assertIsDisplayed()
            .assert(hasAnyDescendant(hasText("1h 50m")))
        composeRule.onNodeWithTag("analytics_kpi_average").assertIsDisplayed()
        composeRule.onNodeWithTag("analytics_kpi_peak").assertIsDisplayed()
        composeRule.onNodeWithTag("analytics_kpi_streak").assertIsDisplayed()

        // Switch to 7-day window: 10-day-old session drops out -> total becomes 1h 25m
        composeRule.onNodeWithTag("analytics_window_7d").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("analytics_kpi_total").assert(hasAnyDescendant(hasText("1h 25m")))

        // Switch to 30-day window: all sessions included -> 1h 50m
        composeRule.onNodeWithTag("analytics_window_30d").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("analytics_kpi_total").assert(hasAnyDescendant(hasText("1h 50m")))
    }

    @Test
    fun moreHubScreen_exposesFocusAnalyticsNavigationEntry() {
        var openedPage: MoreSubPage? = null
        composeRule.setContent {
            LightThemeProvider {
                MoreHubScreen(
                    habitsCount = 2,
                    countdownsCount = 1,
                    pendingTasksCount = 4,
                    onOpenPage = { openedPage = it },
                    onOpenGradientPicker = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_index_focus_analytics").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        assertEquals(MoreSubPage.FOCUS_ANALYTICS, openedPage)
    }

    @Test
    fun redesignedSettingsScreen_displaysGroupedCardsAndOpensDetailSheets() {
        var selectedCalendarView = "week"
        var selectedAppearance = "light"

        composeRule.setContent {
            LightThemeProvider {
                SettingsScreen(
                    profile = null,
                    photoUrl = null,
                    selectedGradientId = "aurore_lavande",
                    appSettings = StudyAppSettings(
                        defaultCalendarView = selectedCalendarView,
                        appearanceMode = selectedAppearance
                    ),
                    courses = listOf(Course(id = "c1", code = "GL301", title = "Architecture")),
                    onSelectGradient = {},
                    onUpdateProfile = { _, _, _, _, _, _, _, _ -> },
                    onUpdateTimerDurations = { _, _, _ -> },
                    onUpdateDefaultCalendarView = { selectedCalendarView = it },
                    onUpdateHapticsEnabled = {},
                    onUpdateAutoLogStopwatch = {},
                    onUpdateAutoPlayAudioWithFocus = {},
                    onSelectAppearanceMode = { selectedAppearance = it },
                    onCreateCourse = { _, _, _, _, _, _, _ -> },
                    onDeleteCourse = {},
                    onSignOut = {},
                    onClose = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Paramètres").assertIsDisplayed()
        composeRule.onNodeWithText("Compte").assertIsDisplayed()
        composeRule.onNodeWithText("Général").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_notifications_switch").assertIsDisplayed()

        // Open Appearance bottom sheet from clean row and pick OLED mode
        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("settings_appearance_mode_row"))
        composeRule.onNodeWithTag("settings_appearance_mode_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_mode_oled").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        assertEquals("oled", selectedAppearance)

        // Open Default Calendar View bottom sheet and pick month view
        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("settings_default_calendar_row"))
        composeRule.onNodeWithTag("settings_default_calendar_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_default_view_month").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        assertEquals("month", selectedCalendarView)

        // Verify Focus & Minuteur grouped rows exist
        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("settings_expand_focus_universe"))
        composeRule.onNodeWithTag("settings_expand_focus_universe").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_rendersCleanlyInOledAndGlassDesignSystem() {
        composeRule.setContent {
            LightThemeProvider(
                selectedThemeId = "indigo",
                appearanceModeKey = AppearanceMode.OLED.key,
                glassEnabled = true,
                wrapWithGradientSurface = true
            ) {
                SettingsScreen(
                    profile = null,
                    photoUrl = null,
                    selectedGradientId = "aurore_lavande",
                    appSettings = StudyAppSettings(
                        selectedThemeId = "indigo",
                        appearanceMode = AppearanceMode.OLED.key,
                        glassEnabled = true
                    ),
                    courses = emptyList(),
                    onSelectGradient = {},
                    onUpdateProfile = { _, _, _, _, _, _, _, _ -> },
                    onUpdateTimerDurations = { _, _, _ -> },
                    onUpdateDefaultCalendarView = {},
                    onUpdateHapticsEnabled = {},
                    onUpdateAutoLogStopwatch = {},
                    onUpdateAutoPlayAudioWithFocus = {},
                    onCreateCourse = { _, _, _, _, _, _, _ -> },
                    onDeleteCourse = {},
                    onSignOut = {},
                    onClose = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_close_button").assertIsDisplayed()
        composeRule.onNodeWithText("Paramètres").assertIsDisplayed()
        composeRule.onNodeWithText("Fréquence des rappels").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_notifications_switch").assertIsDisplayed()
    }

    @Test
    fun premiumChartAggregations_computeHeatmapStackedModesRadarAndMatrixAccurately() {
        val today = LocalDate.of(2026, 11, 18)
        val sessions = listOf(
            FocusSession(id = "s1", durationMinutes = 30, dateIso = today.toString(), mode = "pomodoro"),
            FocusSession(id = "s2", durationMinutes = 50, dateIso = today.toString(), mode = "deep_code"),
            FocusSession(id = "s3", durationMinutes = 20, dateIso = today.minusDays(2).toString(), mode = "stopwatch")
        )
        val tasks = listOf(
            StudyTask(id = "t1", title = "TP", dueDateIso = today.toString(), completed = true),
            StudyTask(id = "t2", title = "Cours", dueDateIso = today.toString(), completed = false)
        )
        val habits = listOf(
            StudyHabit(id = "h1", title = "Lecture", completedDates = listOf(today.toString()))
        )
        val courses = listOf(
            Course(id = "c1", code = "GL301", title = "Architecture", completedMinutes = 600L, targetMinutes = 1200L, grade = 16.0)
        )

        val heatmap = buildActivityHeatmapGrid(sessions, tasks, habits, weeks = 12, today = today)
        assertEquals(84, heatmap.size)
        val todayCell = heatmap.first { it.date == today }
        assertEquals(80, todayCell.focusMinutes)
        assertEquals(1, todayCell.completedTasks)
        assertEquals(1, todayCell.completedHabits)
        assertEquals(4, todayCell.intensityLevel)

        val stacked = buildModeStackedBars(sessions, windowDays = 7, today = today)
        assertEquals(7, stacked.size)
        assertEquals(30, stacked.last().pomodoroMinutes)
        assertEquals(50, stacked.last().deepCodeMinutes)
        assertEquals(80, stacked.last().totalMinutes)

        val weekday = buildWeekdayRhythm(sessions)
        assertEquals(7, weekday.size)
        assertTrue(weekday.any { it.totalMinutes == 80 })

        val axes = buildFiveAxisMasteryScores(tasks, sessions, courses, habits, AppleIndigo, today)
        assertEquals(5, axes.size)
        assertEquals(50, axes.first { it.key == "execution" }.scorePercent)
        assertEquals(80, axes.first { it.key == "grade" }.scorePercent)

        val velocity = buildDailyVelocitySeries(tasks, days = 7, today = today)
        assertEquals(1, velocity.last().completedCount)
        assertEquals(1, velocity.last().pendingCount)

        val bubbles = buildCoursePerformancePoints(courses)
        assertEquals(1, bubbles.size)
        assertEquals(50, bubbles.first().progressPercent)
        assertEquals(16.0, bubbles.first().grade, 0.01)
    }

    @Test
    fun focusAnalyticsScreen_displaysPremiumHeatmapStackedRadarAndDepthCharts() {
        val today = LocalDate.now()
        val sessions = listOf(
            FocusSession(id = "s1", courseId = "c1", courseTitle = "Algo", durationMinutes = 50, dateIso = today.toString(), mode = "deep_code", focusProfileId = "fp1", focusProfileTitle = "Rust"),
            FocusSession(id = "s2", courseId = "c1", courseTitle = "Algo", durationMinutes = 25, dateIso = today.minusDays(1).toString(), mode = "pomodoro")
        )
        val profiles = listOf(FocusProfile(id = "fp1", title = "Rust", learningTopic = "Async"))

        composeRule.setContent {
            LightThemeProvider {
                FocusAnalyticsScreen(
                    sessions = sessions,
                    courses = listOf(Course(id = "c1", code = "INF301", title = "Algo")),
                    onStartFocusSession = {},
                    onOpenSettings = {},
                    focusProfiles = profiles
                )
            }
        }

        composeRule.waitForIdle()
        // Existing line chart still works
        composeRule.onNodeWithTag("focus_analytics_line_chart").assertIsDisplayed()

        // New Heatmap card
        composeRule.onNodeWithTag("focus_analytics_screen").performScrollToNode(hasTestTag("focus_analytics_heatmap_card"))
        composeRule.onNodeWithTag("focus_analytics_heatmap").assertIsDisplayed()

        // New Stacked Mode Bar card
        composeRule.onNodeWithTag("focus_analytics_screen").performScrollToNode(hasTestTag("focus_analytics_stacked_card"))
        composeRule.onNodeWithTag("focus_analytics_stacked_bar_chart").assertIsDisplayed()

        // New Weekday Radar / Bars card + switch to Bars
        composeRule.onNodeWithTag("focus_analytics_screen").performScrollToNode(hasTestTag("focus_analytics_radar_card"))
        composeRule.onNodeWithTag("focus_analytics_radar_chart").assertIsDisplayed()
        composeRule.onNodeWithTag("weekday_mode_bars").performClick()
        composeRule.waitForIdle()

        // New Depth & Profiles Donut card + switch to Profiles
        composeRule.onNodeWithTag("focus_analytics_screen").performScrollToNode(hasTestTag("focus_analytics_depth_card"))
        composeRule.onNodeWithTag("focus_analytics_depth_donut_chart").assertIsDisplayed()
        composeRule.onNodeWithTag("depth_donut_mode_profiles").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Rust").assertIsDisplayed()
    }

    @Test
    fun progressScreen_preservesExistingChartsAndAddsHeatmapRadarVelocityAndMatrix() {
        val today = LocalDate.now()
        val courses = listOf(
            Course(id = "c1", code = "GL301", title = "Architecture", completedMinutes = 720, targetMinutes = 1200, grade = 16.5)
        )
        val tasks = listOf(
            StudyTask(id = "t1", title = "Design Pattern", dueDateIso = today.toString(), completed = true),
            StudyTask(id = "t2", title = "Tests", dueDateIso = today.toString(), completed = false)
        )
        val sessions = listOf(
            FocusSession(id = "s1", courseId = "c1", courseTitle = "Architecture", durationMinutes = 45, dateIso = today.toString(), mode = "deep_code")
        )
        val habits = listOf(
            StudyHabit(id = "h1", title = "Code review", completedDates = listOf(today.toString()))
        )

        composeRule.setContent {
            LightThemeProvider {
                ProgressScreen(
                    profile = null,
                    courses = courses,
                    tasks = tasks,
                    sessions = sessions,
                    onUpdateCourseGrade = { _, _ -> },
                    onOpenSettings = {},
                    habits = habits
                )
            }
        }

        composeRule.waitForIdle()
        // Existing Trend chart still present
        composeRule.onNodeWithTag("progress_trend_chart").assertIsDisplayed()

        // Switch existing Graphique d'étude card tabs to new Heatmap & Radar 5D modes
        composeRule.onNodeWithTag("chart_mode_heatmap").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("focus_analytics_heatmap").assertIsDisplayed()

        composeRule.onNodeWithTag("chart_mode_radar").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("progress_mastery_radar").assertIsDisplayed()

        // Dedicated premium cards in ProgressScreen
        composeRule.onNodeWithTag("progress_screen").performScrollToNode(hasTestTag("progress_habit_heatmap_card"))
        composeRule.onNodeWithTag("progress_habit_heatmap_card").assertIsDisplayed()

        composeRule.onNodeWithTag("progress_screen").performScrollToNode(hasTestTag("progress_velocity_card"))
        composeRule.onNodeWithTag("progress_velocity_chart").assertIsDisplayed()

        composeRule.onNodeWithTag("progress_screen").performScrollToNode(hasTestTag("progress_mastery_radar_card"))
        composeRule.onNodeWithTag("progress_mastery_radar_card").assertIsDisplayed()

        composeRule.onNodeWithTag("progress_screen").performScrollToNode(hasTestTag("progress_course_matrix_card"))
        composeRule.onNodeWithTag("progress_course_matrix_chart").assertIsDisplayed()
    }
}
