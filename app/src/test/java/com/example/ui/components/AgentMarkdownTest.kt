package com.example.ui.components

import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.theme.LightThemeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h915dp-xxhdpi")
class AgentMarkdownTest {
    @get:Rule val compose = createComposeRule()

    @Test fun textAndCodeRenderNativelyAndCopyPreservesTheCode() {
        lateinit var clipboard: ClipboardManager
        compose.setContent {
            clipboard = LocalClipboardManager.current
            LightThemeProvider(appearanceModeKey = "oled") { AgentMarkdown("## Étape\n\n**Lire**\n\n```kt\nval n = 3\n```") }
        }
        compose.onNodeWithText("Étape").assertIsDisplayed()
        compose.onNodeWithText("Lire").assertIsDisplayed()
        compose.onNodeWithTag("copy_agent_code_2").performClick()
        compose.runOnIdle { assertEquals("val n = 3\n", clipboard.getText()?.text) }
    }

    @Test fun modelSuppliedHtmlAndLinksNeverBecomeActiveElements() {
        val parsed = parseAgentMarkdown("<script>danger()</script>\n\n[Texte](javascript:danger)\n\n![Image](https://example.org/file)")
        val text = parsed.filterIsInstance<AgentMarkdownBlock.Text>().map { it.text }
        assertTrue(text.any { it.text == "<script>danger()</script>" })
        assertTrue(text.any { it.text == "Texte" && it.getStringAnnotations(0, it.length).isEmpty() })
        assertTrue(text.any { it.text == "[Image : Image]" })
    }
}
