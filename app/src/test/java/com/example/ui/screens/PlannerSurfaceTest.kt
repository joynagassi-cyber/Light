package com.example.ui.screens

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.ui.AiPlanUiState
import com.example.ui.theme.AppearanceMode
import com.example.ui.theme.LightThemeProvider
import com.example.ui.theme.LightTouchTarget
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h915dp-xxhdpi")
class PlannerSurfaceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun selectorsInLightTheme() = checkSelector(AppearanceMode.LIGHT, false)
    @Test fun selectorsInOledWithGlassRequested() = checkSelector(AppearanceMode.OLED, true)

    private fun checkSelector(mode: AppearanceMode, glass: Boolean) {
        compose.setContent {
            LightThemeProvider(appearanceModeKey = mode.key, glassEnabled = glass, wrapWithGradientSurface = true) {
                PlannerScreen(
                    selectedDateIso = "2026-11-19", defaultCalendarViewKey = "day", onSelectDate = {},
                    courses = emptyList(), tasks = emptyList(), aiPlanState = AiPlanUiState.Idle,
                    onGenerateAiPlan = { _, _, _ -> }, onApplyAiPlan = {}, onResetAiPlan = {}, onOpenCreateTask = {},
                    onOpenSettings = {}, onToggleTask = {}, onDeleteTask = {}, onStartFocusForTask = {}
                )
            }
        }
        for (tag in listOf("calendar_active_view_pill", "calendar_views_menu_button")) {
            compose.onNodeWithTag(tag).assertWidthIsAtLeast(LightTouchTarget.minimum)
                .assertHeightIsAtLeast(LightTouchTarget.minimum)
        }
        compose.onNodeWithTag("calendar_active_view_pill").performClick()
        compose.onNodeWithTag("calendar_view_week").assertExists()
    }
}
