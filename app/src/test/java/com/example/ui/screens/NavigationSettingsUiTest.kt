package com.example.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.data.model.NavigationLayout
import com.example.data.model.NavigationOptions
import com.example.ui.theme.LightThemeProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h915dp-xxhdpi")
class NavigationSettingsUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun reorderChooseIconAndHideWithoutLosingHomeOrTheLastOptionalFeature() {
        var result: NavigationLayout? = null
        compose.setContent {
            LightThemeProvider(appearanceModeKey = "oled") {
                NavigationSettingsScreen(NavigationLayout(), NavigationOptions.features.associateWith { it },
                    NavigationOptions.features.associateWith { Icons.Rounded.Home }, onSave = { result = it }, onBack = {})
            }
        }
        compose.onNodeWithTag("navigation_settings_page").assertIsDisplayed()
        compose.onNodeWithTag("nav_visible_home").assertIsNotEnabled()
        compose.onNodeWithTag("nav_up_planner").performScrollTo().performClick()
        compose.onNodeWithTag("nav_icon_planner").performScrollTo().performClick()
        compose.onNodeWithText("Étoile").performClick()
        compose.onNodeWithTag("nav_visible_focus").performScrollTo().performClick()
        compose.onNodeWithTag("nav_visible_matrix").performScrollTo().performClick()
        compose.onNodeWithTag("nav_visible_planner").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag("save_navigation_layout").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(listOf("planner", "home"), result?.visible)
            assertEquals(mapOf("planner" to "star"), result?.icons)
        }
    }
}
