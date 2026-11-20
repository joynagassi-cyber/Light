package com.example.ui.theme

import android.app.ActivityManager
import android.content.Context
import android.os.Build
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

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

internal val LocalGlassBackdrop = staticCompositionLocalOf<HazeState?> { null }

internal fun canUseBackdropBlur(requested: Boolean, sdk: Int, lowRam: Boolean, hardware: Boolean): Boolean =
    requested && sdk >= 31 && !lowRam && hardware

/** Record the decorative backdrop once; foreground content is never part of the capture. */
@Composable
fun GradientScreenSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val gradient = LocalLightGradient.current
    val config = LocalThemeSurfaceConfig.current
    val dark = config.appearanceMode == AppearanceMode.OLED
    val isGradient = config.selectedThemeId == LightColorThemes.GRADIENT_ID
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val view = LocalView.current
    val lowRam = remember(context) {
        (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.isLowRamDevice ?: true
    }
    val hazeState = remember { HazeState() }
    val backdrop = hazeState.takeIf {
        canUseBackdropBlur(config.glassEnabled, Build.VERSION.SDK_INT, lowRam, view.isHardwareAccelerated)
    }
    val useBlur = backdrop != null
    val background = when {
        dark -> Brush.verticalGradient(listOf(scheme.background, scheme.background))
        isGradient -> gradient.screenBackgroundBrush()
        else -> Brush.verticalGradient(listOf(config.palette.background, config.palette.background))
    }
    Surface(
        modifier = modifier.fillMaxSize(),
        color = scheme.background,
        contentColor = scheme.onBackground,
    ) {
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.matchParentSize().then(if (backdrop != null) Modifier.haze(backdrop) else Modifier)) {
                drawRect(background)
                if (useBlur || (!dark && isGradient)) {
                    val alpha = when {
                        !useBlur -> LightSurfaceTokens.plainBackdropLight
                        dark -> LightSurfaceTokens.glassBackdropDark
                        else -> LightSurfaceTokens.glassBackdropLight
                    }
                    val radius = size.maxDimension * .44f
                    listOf(
                        Offset(size.width * .91f, size.height * .13f) to gradient.startColor,
                        Offset(size.width * .04f, size.height * .51f) to gradient.midColor,
                        Offset(size.width * .82f, size.height * .86f) to gradient.endColor,
                    ).forEach { (center, color) ->
                        val tint = if (useBlur) color.glassChroma() else color
                        drawCircle(Brush.radialGradient(listOf(tint.copy(alpha = alpha), Color.Transparent), center, radius),
                            radius = radius, center = center)
                    }
                }
                if (useBlur) {
                    val w = size.width
                    val h = size.height
                    val ribbon = Path().apply {
                        moveTo(0f, h * .45f)
                        cubicTo(w * .44f, h * .13f, w * .74f, h * .59f, w, h * .25f)
                        lineTo(w, h * .43f)
                        cubicTo(w * .74f, h * .77f, w * .44f, h * .31f, 0f, h * .63f)
                        close()
                    }
                    // Saturated theme accents add chroma without repainting foreground text or icons.
                    drawPath(ribbon, Brush.linearGradient(listOf(
                        gradient.startColor.glassChroma().copy(alpha = LightSurfaceTokens.glassBackdropLight),
                        gradient.endColor.glassChroma().copy(alpha = LightSurfaceTokens.glassBackdropDark),
                    ), start = Offset.Zero, end = Offset(w, h)))
                }
            }
            CompositionLocalProvider(LocalGlassBackdrop provides backdrop) { content() }
        }
    }
}

/** Saturation is limited to the decorative source; text and controls keep their theme colors. */
private fun Color.glassChroma(): Color {
    val gray = red * .2126f + green * .7152f + blue * .0722f
    fun enrich(channel: Float) = (gray + (channel - gray) * LightSurfaceTokens.glassChroma).coerceIn(0f, 1f)
    return Color(enrich(red), enrich(green), enrich(blue), alpha)
}
