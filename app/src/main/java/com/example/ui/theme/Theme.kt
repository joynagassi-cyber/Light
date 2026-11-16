package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LightShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun LightTheme(
    selectedGradientId: String = "aurore_lavande",
    content: @Composable () -> Unit
) {
    val targetGradient = LightGradients.byId(selectedGradientId)
    val animatedGradient = rememberAnimatedGradientTheme(targetGradient)

    val colorScheme = lightColorScheme(
        primary = animatedGradient.accentColor,
        onPrimary = Color.White,
        primaryContainer = animatedGradient.softContainer,
        onPrimaryContainer = animatedGradient.onSoftContainer,
        secondary = animatedGradient.midColor,
        onSecondary = Color.White,
        secondaryContainer = animatedGradient.softContainer,
        onSecondaryContainer = animatedGradient.onSoftContainer,
        tertiary = animatedGradient.endColor,
        onTertiary = Color.White,
        background = animatedGradient.ambientTop,
        onBackground = AppleTextPrimary,
        surface = AppleCardSurface,
        onSurface = AppleTextPrimary,
        surfaceVariant = animatedGradient.softContainer,
        onSurfaceVariant = AppleTextSecondary,
        outline = AppleDivider,
        outlineVariant = AppleDivider,
        error = AppleRose,
        onError = Color.White
    )

    CompositionLocalProvider(LocalLightGradient provides animatedGradient) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LightTypography,
            shapes = LightShapes,
            content = content
        )
    }
}
