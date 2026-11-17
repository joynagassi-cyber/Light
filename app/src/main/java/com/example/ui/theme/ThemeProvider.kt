package com.example.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class LightThemeController(
    val activeGradient: LightGradientTheme,
    val availableGradients: List<LightGradientTheme>,
    val selectGradient: (String) -> Unit
)

val LocalLightThemeController = staticCompositionLocalOf {
    LightThemeController(
        activeGradient = LightGradients.all.first(),
        availableGradients = LightGradients.all,
        selectGradient = {}
    )
}

/** Provides the selected palette, appearance mode, and Material 3 color scheme app-wide. */
@Composable
fun LightThemeProvider(
    selectedGradientId: String = "aurore_lavande",
    selectedThemeId: String = LightColorThemes.GRADIENT_ID,
    appearanceModeKey: String = AppearanceMode.LIGHT.key,
    glassEnabled: Boolean = false,
    onSelectGradient: (String) -> Unit = {},
    wrapWithGradientSurface: Boolean = false,
    content: @Composable () -> Unit
) {
    LightTheme(
        selectedGradientId = selectedGradientId,
        selectedThemeId = selectedThemeId,
        appearanceModeKey = appearanceModeKey,
        glassEnabled = glassEnabled
    ) {
        val activeGradient = LocalLightGradient.current
        val controller = LightThemeController(
            activeGradient = activeGradient,
            availableGradients = LightGradients.all,
            selectGradient = onSelectGradient
        )
        CompositionLocalProvider(LocalLightThemeController provides controller) {
            if (wrapWithGradientSurface) {
                GradientScreenSurface(content = content)
            } else {
                content()
            }
        }
    }
}

/** The app canvas is gradient, solid, or OLED black according to the selected appearance. */
@Composable
fun GradientScreenSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val gradient = LocalLightGradient.current
    val surfaceConfig = LocalThemeSurfaceConfig.current
    val isGradientTheme = surfaceConfig.selectedThemeId == LightColorThemes.GRADIENT_ID
    val isOled = surfaceConfig.appearanceMode == AppearanceMode.OLED
    val isGlass = surfaceConfig.glassEnabled
    val backgroundColor = when {
        isOled -> Color.Black
        isGradientTheme || isGlass -> surfaceConfig.palette.background
        else -> surfaceConfig.palette.background
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when {
                        isOled -> Color.Black
                        isGradientTheme -> gradient.screenBackgroundBrush()
                        isGlass -> Brush.verticalGradient(
                            listOf(
                                surfaceConfig.palette.background,
                                surfaceConfig.palette.background.copy(alpha = 0.92f),
                                gradient.softContainer.copy(alpha = 0.62f)
                            )
                        )
                        else -> Brush.verticalGradient(listOf(backgroundColor, backgroundColor))
                    }
                )
        ) {
            if (!isOled && (isGradientTheme || isGlass)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.maxDimension * 0.42f
                    listOf(
                        Offset(size.width * 0.92f, size.height * 0.08f) to gradient.startColor,
                        Offset(size.width * 0.06f, size.height * 0.48f) to gradient.midColor,
                        Offset(size.width * 0.86f, size.height * 0.9f) to gradient.endColor
                    ).forEach { (center, color) ->
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(color.copy(alpha = if (isGlass) 0.13f else 0.10f), Color.Transparent),
                                center = center,
                                radius = radius
                            ),
                            radius = radius,
                            center = center
                        )
                    }
                }
            }
            content()
        }
    }
}
