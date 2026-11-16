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
data class ElanThemeController(
    val activeGradient: ElanGradientTheme,
    val availableGradients: List<ElanGradientTheme>,
    val selectGradient: (String) -> Unit
)

val LocalElanThemeController = staticCompositionLocalOf {
    ElanThemeController(
        activeGradient = ElanGradients.all.first(),
        availableGradients = ElanGradients.all,
        selectGradient = {}
    )
}

/**
 * Provides the Material 3 theme, smoothly animated gradient tokens, and theme switching
 * controller across all Compose screens.
 */
@Composable
fun ElanThemeProvider(
    selectedGradientId: String = "aurore_lavande",
    onSelectGradient: (String) -> Unit = {},
    wrapWithGradientSurface: Boolean = false,
    content: @Composable () -> Unit
) {
    ElanTheme(selectedGradientId = selectedGradientId) {
        val activeGradient = LocalElanGradient.current
        val controller = ElanThemeController(
            activeGradient = activeGradient,
            availableGradients = ElanGradients.all,
            selectGradient = onSelectGradient
        )
        CompositionLocalProvider(LocalElanThemeController provides controller) {
            if (wrapWithGradientSurface) {
                GradientScreenSurface(content = content)
            } else {
                content()
            }
        }
    }
}

/**
 * Custom Material 3 Surface wrapper that renders the active animated gradient backdrop
 * and subtle luminous ambient orbs behind the main Compose screens.
 */
@Composable
fun GradientScreenSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val gradient = LocalElanGradient.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient.screenBackgroundBrush())
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val topRightRadius = size.maxDimension * 0.48f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            gradient.startColor.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.92f, size.height * 0.08f),
                        radius = topRightRadius
                    ),
                    radius = topRightRadius,
                    center = Offset(size.width * 0.92f, size.height * 0.08f)
                )

                val midLeftRadius = size.maxDimension * 0.42f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            gradient.midColor.copy(alpha = 0.11f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.06f, size.height * 0.42f),
                        radius = midLeftRadius
                    ),
                    radius = midLeftRadius,
                    center = Offset(size.width * 0.06f, size.height * 0.42f)
                )

                val bottomRightRadius = size.maxDimension * 0.44f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            gradient.endColor.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.88f),
                        radius = bottomRightRadius
                    ),
                    radius = bottomRightRadius,
                    center = Offset(size.width * 0.85f, size.height * 0.88f)
                )
            }
            content()
        }
    }
}
