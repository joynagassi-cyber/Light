package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LightShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun LightTheme(
    selectedGradientId: String = "aurore_lavande",
    selectedThemeId: String = LightColorThemes.GRADIENT_ID,
    appearanceModeKey: String = AppearanceMode.LIGHT.key,
    glassEnabled: Boolean = false,
    content: @Composable () -> Unit
) {
    val appearanceMode = AppearanceMode.fromKey(appearanceModeKey)
    val palette = LightColorThemes.byId(selectedThemeId)
    val gradientTarget = if (selectedThemeId == LightColorThemes.GRADIENT_ID) {
        LightGradients.byId(selectedGradientId)
    } else {
        LightGradientTheme(
            id = palette.id,
            name = palette.name,
            subtitle = palette.audience,
            startColor = palette.primary,
            midColor = palette.secondary,
            endColor = palette.tertiary,
            ambientTop = palette.background,
            ambientMid = palette.background,
            ambientBottom = palette.background,
            accentColor = palette.primary,
            softContainer = palette.softSurface,
            onSoftContainer = palette.onSoftSurface
        )
    }
    val readableGradientTarget = if (appearanceMode == AppearanceMode.OLED) {
        gradientTarget.copy(accentColor = contrastAdjustedColor(gradientTarget.accentColor, Color.Black))
    } else {
        gradientTarget
    }
    val animatedGradient = rememberAnimatedGradientTheme(readableGradientTarget)
    val accent = animatedGradient.accentColor
    val background = if (selectedThemeId == LightColorThemes.GRADIENT_ID) {
        animatedGradient.ambientTop
    } else {
        palette.background
    }
    val surface = when {
        appearanceMode == AppearanceMode.OLED -> Color(0xFF08090B)
        selectedThemeId == LightColorThemes.GRADIENT_ID -> AppleCardSurface
        else -> palette.surface
    }
    val colorScheme = when (appearanceMode) {
        AppearanceMode.OLED -> darkColorScheme(
            primary = accent,
            onPrimary = accessibleForeground(accent),
            primaryContainer = accent.copy(alpha = 0.22f),
            onPrimaryContainer = Color(0xFFE8E9EF),
            secondary = animatedGradient.midColor,
            onSecondary = accessibleForeground(animatedGradient.midColor),
            secondaryContainer = Color(0xFF1C1D22),
            onSecondaryContainer = Color(0xFFE2E3E8),
            tertiary = animatedGradient.endColor,
            onTertiary = accessibleForeground(animatedGradient.endColor),
            background = Color.Black,
            onBackground = Color(0xFFF4F4F6),
            surface = surface,
            onSurface = Color(0xFFF4F4F6),
            surfaceVariant = Color(0xFF17181C),
            onSurfaceVariant = Color(0xFFB7B8C0),
            outline = Color(0xFF9A9BA1),
            outlineVariant = Color(0xFF2B2C31),
            error = Color(0xFFFF667A),
            onError = Color.Black
        )
        AppearanceMode.LIGHT -> lightColorScheme(
            primary = accent,
            onPrimary = accessibleForeground(accent),
            primaryContainer = animatedGradient.softContainer,
            onPrimaryContainer = animatedGradient.onSoftContainer,
            secondary = animatedGradient.midColor,
            onSecondary = accessibleForeground(animatedGradient.midColor),
            secondaryContainer = animatedGradient.softContainer,
            onSecondaryContainer = animatedGradient.onSoftContainer,
            tertiary = animatedGradient.endColor,
            onTertiary = accessibleForeground(animatedGradient.endColor),
            background = background,
            onBackground = AppleTextPrimary,
            surface = surface,
            onSurface = AppleTextPrimary,
            surfaceVariant = animatedGradient.softContainer,
            onSurfaceVariant = AppleTextSecondary,
            outline = AppleControlOutline,
            outlineVariant = AppleDivider,
            error = AppleRose,
            onError = Color.White
        )
    }

    CompositionLocalProvider(
        LocalLightGradient provides animatedGradient,
        LocalThemeSurfaceConfig provides ThemeSurfaceConfig(
            selectedThemeId = selectedThemeId,
            appearanceMode = appearanceMode,
            glassEnabled = glassEnabled,
            palette = palette
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LightTypography,
            shapes = LightShapes,
            content = content
        )
    }
}
