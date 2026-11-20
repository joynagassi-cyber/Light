package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import com.example.ui.theme.AppearanceMode
import com.example.ui.theme.LightSurfaceTokens
import com.example.ui.theme.LightTouchTarget
import com.example.ui.theme.LocalGlassBackdrop
import com.example.ui.theme.LocalThemeSurfaceConfig
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild

private val LocalInsideGlass = staticCompositionLocalOf { false }

/** No decorative stroke or shadow for opaque cards; descendants never re-sample a glass pile. */
@Composable
internal fun LightPanel(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val insideGlass = LocalInsideGlass.current
    val backdrop = LocalGlassBackdrop.current.takeUnless { insideGlass }
    val scheme = MaterialTheme.colorScheme
    val dark = LocalThemeSurfaceConfig.current.appearanceMode == AppearanceMode.OLED
    val treatments = if (backdrop != null) {
        val tint = if (dark) LightSurfaceTokens.glassTintDark else LightSurfaceTokens.glassTintLight
        val highlight = if (dark) LightSurfaceTokens.glassHighlightDark else LightSurfaceTokens.glassHighlightLight
        Modifier.border(
            LightSurfaceTokens.glassRimWidth,
            Brush.linearGradient(listOf(Color.White.copy(alpha = highlight), Color.Transparent)),
            shape,
        ).hazeChild(backdrop, HazeStyle(
            backgroundColor = scheme.background,
            tint = HazeTint(scheme.surface.copy(alpha = tint)),
            fallbackTint = HazeTint(scheme.surface),
            blurRadius = LightSurfaceTokens.glassBlur,
            noiseFactor = 0f,
        ))
    } else Modifier
    val interaction = if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier
    Surface(
        modifier = modifier.clip(shape).then(treatments).then(interaction),
        shape = shape,
        color = if (backdrop != null) Color.Transparent else scheme.surface,
        contentColor = scheme.onSurface,
    ) {
        CompositionLocalProvider(LocalInsideGlass provides (insideGlass || backdrop != null)) {
            content()
        }
    }
}

/** Shared quiet/primary actions. No idle stroke; keyboard focus and ripple are retained. */
@Composable
internal fun LightControl(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.sizeIn(minWidth = LightTouchTarget.minimum, minHeight = LightTouchTarget.minimum),
        color = color,
        shape = CircleShape,
    ) {
        Box(contentAlignment = Alignment.Center, content = content)
    }
}
