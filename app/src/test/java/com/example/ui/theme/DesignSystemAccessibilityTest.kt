package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignSystemAccessibilityTest {
    @Test
    fun `gradient stops used behind white hero text meet WCAG AA`() {
        LightGradients.all.forEach { theme ->
            listOf(theme.startColor, theme.midColor, theme.endColor).forEach { color ->
                assertTrue(
                    "${theme.id} hero stop contrast was ${contrastRatio(Color.White, color)}",
                    contrastRatio(Color.White, color) >= 4.5f
                )
            }
        }
    }

    @Test
    fun `theme foreground colors meet WCAG AA against their fills`() {
        LightGradients.all.forEach { theme ->
            assertTrue(contrastRatio(theme.accentColor, Color.White) >= 4.5f)
            assertTrue(contrastRatio(theme.onSoftContainer, theme.softContainer) >= 4.5f)
            assertTrue(contrastRatio(accessibleForeground(theme.midColor), theme.midColor) >= 4.5f)
            assertTrue(contrastRatio(accessibleForeground(theme.endColor), theme.endColor) >= 4.5f)
        }
        LightColorThemes.all.forEach { theme ->
            assertTrue(contrastRatio(theme.primary, Color.White) >= 4.5f)
            assertTrue(contrastRatio(theme.onSoftSurface, theme.softSurface) >= 4.5f)
            listOf(theme.secondary, theme.tertiary).forEach { fill ->
                assertTrue(contrastRatio(accessibleForeground(fill), fill) >= 4.5f)
            }
        }
    }

    @Test
    fun `OLED accents are adjusted to remain readable on black`() {
        LightGradients.all.forEach { theme ->
            val readable = contrastAdjustedColor(theme.accentColor, Color.Black)
            assertTrue(contrastRatio(readable, Color.Black) >= 4.5f)
        }
    }

    @Test
    fun `spacing and hit targets follow the shared 8 point scale`() {
        assertEquals(8f, LightSpacing.one.value, 0f)
        assertEquals(16f, LightSpacing.two.value, 0f)
        assertEquals(24f, LightSpacing.three.value, 0f)
        assertEquals(48f, LightTouchTarget.minimum.value, 0f)
        assertEquals(12f, LightTypeScale.micro.value, 0f)
    }
}
