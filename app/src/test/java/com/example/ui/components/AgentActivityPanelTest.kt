package com.example.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.repository.AgentActionLog
import com.example.ui.theme.LightThemeProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h915dp-xxhdpi")
class AgentActivityPanelTest {
    @get:Rule val compose = createComposeRule()

    @Test fun activityIsCollapsedByDefaultAndShowsMeasuredResultsWhenOpened() {
        compose.setContent {
            LightThemeProvider {
                AgentActivityPanel("m1", null,
                    listOf(AgentActionLog("list_tasks", "Planning consulté", elapsedMillis = 1_000L)), 3_000L)
            }
        }
        val toggle = compose.onNodeWithTag("agent_activity_toggle").assertIsDisplayed()
        compose.onNodeWithTag("agent_activity_details").assertDoesNotExist()
        toggle.performClick()
        compose.onNodeWithTag("agent_activity_details").assertIsDisplayed()
        compose.onNodeWithText("Planning consulté").assertIsDisplayed()
        toggle.performClick()
        compose.onNodeWithTag("agent_activity_details").assertDoesNotExist()
    }
}
