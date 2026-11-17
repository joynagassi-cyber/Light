package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Apple iOS-inspired off-white canvas & crisp surface hierarchy
val AppleCanvas = Color(0xFFF6F7FB)
val AppleCardSurface = Color(0xFFFFFFFF)
val AppleCardElevated = Color(0xFFFCFCFE)
val AppleDivider = Color(0xFFE7E9F2)
val AppleTextPrimary = Color(0xFF141628)
val AppleTextSecondary = Color(0xFF636880)
// Tertiary text is still readable on white; do not use low-opacity gray for labels.
val AppleTextTertiary = Color(0xFF6E7487)
val AppleControlOutline = Color(0xFF6E7487)

// Semantic feedback colors are chosen as foregrounds, not decorative pastel swatches.
val AppleEmerald = Color(0xFF047857)
val AppleAmber = Color(0xFFB45309)
val AppleRose = Color(0xFFBE123C)
val AppleSky = Color(0xFF0369A1)
val AppleIndigo = Color(0xFF4F46E5)

private fun linearize(channel: Float): Float =
    if (channel <= 0.04045f) channel / 12.92f else Math.pow(((channel + 0.055f) / 1.055f).toDouble(), 2.4).toFloat()

fun relativeLuminance(color: Color): Float =
    0.2126f * linearize(color.red) + 0.7152f * linearize(color.green) + 0.0722f * linearize(color.blue)

/** WCAG contrast ratio for opaque foreground/background colors. */
fun contrastRatio(foreground: Color, background: Color): Float {
    val first = relativeLuminance(foreground)
    val second = relativeLuminance(background)
    val lighter = maxOf(first, second)
    val darker = minOf(first, second)
    return (lighter + 0.05f) / (darker + 0.05f)
}

/** Prefer white when it passes AA; otherwise use black for strong contrast. */
fun accessibleForeground(background: Color): Color = when {
    contrastRatio(Color.White, background) >= 4.5f -> Color.White
    contrastRatio(Color.Black, background) >= 4.5f -> Color.Black
    contrastRatio(Color.White, background) >= contrastRatio(Color.Black, background) -> Color.White
    else -> Color.Black
}

/** Adjusts a theme accent only as much as needed to keep it readable on a known surface. */
fun contrastAdjustedColor(foreground: Color, background: Color, minimum: Float = 4.5f): Color {
    if (contrastRatio(foreground, background) >= minimum) return foreground
    val target = accessibleForeground(background)
    for (step in 1..100) {
        val amount = step / 100f
        val candidate = Color(
            red = foreground.red + (target.red - foreground.red) * amount,
            green = foreground.green + (target.green - foreground.green) * amount,
            blue = foreground.blue + (target.blue - foreground.blue) * amount,
            alpha = foreground.alpha + (target.alpha - foreground.alpha) * amount
        )
        if (contrastRatio(candidate, background) >= minimum) return candidate
    }
    return target
}

// Dark surface palette
val DarkCanvas = Color(0xFF0F101A)
val DarkCardSurface = Color(0xFF191B2B)
val DarkDivider = Color(0xFF282B40)
val DarkTextPrimary = Color(0xFFF5F6FA)
val DarkTextSecondary = Color(0xFFA6ABC4)
