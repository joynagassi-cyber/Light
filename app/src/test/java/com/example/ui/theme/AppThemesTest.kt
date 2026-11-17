package com.example.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppThemesTest {
    @Test
    fun curatedPalettesAreTenDistinctNonGradientColorways() {
        assertEquals(10, LightColorThemes.all.size)
        assertEquals(10, LightColorThemes.all.map { it.id }.distinct().size)
        assertTrue(LightColorThemes.all.all { it.primary != it.background && it.surface != it.background })
        assertNotEquals(LightColorThemes.all[0].primary, LightColorThemes.all[1].primary)
    }

    @Test
    fun appearanceModesAreStableAndDefaultToLight() {
        assertEquals(AppearanceMode.LIGHT, AppearanceMode.fromKey("light"))
        assertEquals(AppearanceMode.OLED, AppearanceMode.fromKey("oled"))
        assertEquals(AppearanceMode.LIGHT, AppearanceMode.fromKey("glass"))
        assertEquals(AppearanceMode.LIGHT, AppearanceMode.fromKey("unknown"))
        assertTrue(ThemeSurfaceConfig(glassEnabled = true, appearanceMode = AppearanceMode.OLED).glassEnabled)
    }
}
