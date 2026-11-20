package com.example.ui.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.ui.theme.AppearanceMode
import com.example.ui.theme.LightThemeProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OfflineBannerTest {
    @get:Rule val compose = createComposeRule()

    @Test fun connectivityTransitionsInLight() = verifyConnectivity(AppearanceMode.LIGHT)
    @Test fun connectivityTransitionsInOled() = verifyConnectivity(AppearanceMode.OLED)

    private fun verifyConnectivity(mode: AppearanceMode) {
        val online = mutableStateOf(true)
        compose.setContent { LightThemeProvider(appearanceModeKey = mode.key) { OfflineBanner(online.value) } }
        val banner = compose.onNodeWithTag("home_offline_banner")
        banner.assertDoesNotExist()
        compose.runOnIdle { online.value = false }
        banner.assertExists()
        compose.runOnIdle { online.value = true }
        banner.assertDoesNotExist()
    }
}
