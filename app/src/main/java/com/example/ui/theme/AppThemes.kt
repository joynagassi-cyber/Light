package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** A deliberately non-gradient set of curated palettes for different work styles. */
@Immutable
data class LightColorTheme(
    val id: String,
    val name: String,
    val audience: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val softSurface: Color,
    val onSoftSurface: Color
)

object LightColorThemes {
    const val GRADIENT_ID = "gradient"

    val all = listOf(
        LightColorTheme("paper", "Papier", "Clair, calme et polyvalent", Color(0xFF345CFF), Color(0xFF5474B8), Color(0xFF6C778C), Color(0xFFF5F6FA), Color(0xFFFFFFFF), Color(0xFFE9EDFF), Color(0xFF1D326E)),
        LightColorTheme("graphite", "Graphite", "Minimaliste et concentré", Color(0xFF424B5A), Color(0xFF687386), Color(0xFF9A835B), Color(0xFFF2F3F5), Color(0xFFFFFFFF), Color(0xFFE7E9ED), Color(0xFF303744)),
        LightColorTheme("ocean", "Océan", "Études et travail approfondi", Color(0xFF126A83), Color(0xFF2E8C9C), Color(0xFF4D728F), Color(0xFFEEF6F8), Color(0xFFFFFFFF), Color(0xFFDCEFF2), Color(0xFF164C5A)),
        LightColorTheme("forest", "Forêt", "Équilibre et progression durable", Color(0xFF39734F), Color(0xFF598C62), Color(0xFF8B8052), Color(0xFFF0F6F0), Color(0xFFFFFFFF), Color(0xFFE0EDE1), Color(0xFF28543A)),
        LightColorTheme("plum", "Prune", "Créativité et projets personnels", Color(0xFF734E8F), Color(0xFF9670A9), Color(0xFFAD6D83), Color(0xFFF7F2F8), Color(0xFFFFFFFF), Color(0xFFEDE1F2), Color(0xFF56376C)),
        LightColorTheme("sand", "Sable", "Chaleureux et sans distraction", Color(0xFF8A5A31), Color(0xFFB27A4C), Color(0xFF6A7966), Color(0xFFFAF5ED), Color(0xFFFFFFFF), Color(0xFFF1E6D5), Color(0xFF654123)),
        LightColorTheme("rose", "Rose poudré", "Doux, soigné et accessible", Color(0xFFB44F72), Color(0xFFD16D89), Color(0xFF827098), Color(0xFFFFF3F6), Color(0xFFFFFFFF), Color(0xFFF8E1E8), Color(0xFF7A3049)),
        LightColorTheme("cobalt", "Cobalt", "Énergique pour les équipes produit", Color(0xFF2455A6), Color(0xFF3479B8), Color(0xFF497F78), Color(0xFFF0F4FA), Color(0xFFFFFFFF), Color(0xFFE0EAF8), Color(0xFF1D417B)),
        LightColorTheme("saffron", "Safran", "Décisions et objectifs ambitieux", Color(0xFF9A6813), Color(0xFFC18A25), Color(0xFF9D5B45), Color(0xFFFFF8E9), Color(0xFFFFFFFF), Color(0xFFF7EDCF), Color(0xFF694609)),
        LightColorTheme("mint", "Menthe", "Léger et orienté bien-être", Color(0xFF26766D), Color(0xFF46978A), Color(0xFF597DA0), Color(0xFFEEF8F5), Color(0xFFFFFFFF), Color(0xFFDCEFEA), Color(0xFF20594F))
    )

    fun byId(id: String?): LightColorTheme = all.firstOrNull { it.id == id } ?: all.first()
}

enum class AppearanceMode(val key: String, val label: String, val description: String) {
    LIGHT("light", "Clair", "Surfaces lumineuses"),
    OLED("oled", "OLED", "Noir profond, économe sur écran OLED");

    companion object {
        fun fromKey(key: String?): AppearanceMode = entries.firstOrNull { it.key == key } ?: LIGHT
    }
}

@Immutable
data class ThemeSurfaceConfig(
    val selectedThemeId: String = LightColorThemes.GRADIENT_ID,
    val appearanceMode: AppearanceMode = AppearanceMode.LIGHT,
    val glassEnabled: Boolean = false,
    val palette: LightColorTheme = LightColorThemes.all.first()
)

val LocalThemeSurfaceConfig = staticCompositionLocalOf { ThemeSurfaceConfig() }
