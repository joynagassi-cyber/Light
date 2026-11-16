package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class ElanGradientTheme(
    val id: String,
    val name: String,
    val subtitle: String,
    val startColor: Color,
    val midColor: Color,
    val endColor: Color,
    val ambientTop: Color,
    val ambientMid: Color,
    val ambientBottom: Color,
    val accentColor: Color,
    val softContainer: Color,
    val onSoftContainer: Color
) {
    fun linearBrush(): Brush = Brush.linearGradient(
        colors = listOf(startColor, midColor, endColor),
        start = Offset(0f, 0f),
        end = Offset(1000f, 700f)
    )

    fun verticalBrush(): Brush = Brush.verticalGradient(
        colors = listOf(startColor, midColor, endColor)
    )

    fun screenBackgroundBrush(): Brush = Brush.verticalGradient(
        colors = listOf(ambientTop, ambientMid, ambientBottom)
    )
}

object ElanGradients {
    val all: List<ElanGradientTheme> = listOf(
        ElanGradientTheme(
            id = "aurore_lavande",
            name = "Aurore Lavande",
            subtitle = "Violet néon & magenta soyeux",
            startColor = Color(0xFF6B21F5),
            midColor = Color(0xFF9D3CF7),
            endColor = Color(0xFFE935C1),
            ambientTop = Color(0xFFE9DFFF),
            ambientMid = Color(0xFFF6E8FF),
            ambientBottom = Color(0xFFFCF7FF),
            accentColor = Color(0xFF7C3AED),
            softContainer = Color(0xFFF2ECFF),
            onSoftContainer = Color(0xFF4C1D95)
        ),
        ElanGradientTheme(
            id = "lagon_boreal",
            name = "Lagon Boréal",
            subtitle = "Cyan polaire & bleu saphir",
            startColor = Color(0xFF06B6D4),
            midColor = Color(0xFF2563EB),
            endColor = Color(0xFF4338CA),
            ambientTop = Color(0xFFD7F3FE),
            ambientMid = Color(0xFFE6EEFF),
            ambientBottom = Color(0xFFF5F8FF),
            accentColor = Color(0xFF0284C7),
            softContainer = Color(0xFFE8F6FE),
            onSoftContainer = Color(0xFF075985)
        ),
        ElanGradientTheme(
            id = "menthe_emeraude",
            name = "Menthe Émeraude",
            subtitle = "Vert frais & océan profond",
            startColor = Color(0xFF10B981),
            midColor = Color(0xFF0891B2),
            endColor = Color(0xFF1E3A8A),
            ambientTop = Color(0xFFD4FAED),
            ambientMid = Color(0xFFE1F6FA),
            ambientBottom = Color(0xFFF4FBFC),
            accentColor = Color(0xFF059669),
            softContainer = Color(0xFFE6FAF3),
            onSoftContainer = Color(0xFF065F46)
        ),
        ElanGradientTheme(
            id = "peche_solaire",
            name = "Pêche Solaire",
            subtitle = "Ambre doré & corail vibrant",
            startColor = Color(0xFFF59E0B),
            midColor = Color(0xFFF97316),
            endColor = Color(0xFFE11D48),
            ambientTop = Color(0xFFFFEBD0),
            ambientMid = Color(0xFFFFE5DC),
            ambientBottom = Color(0xFFFFF8F5),
            accentColor = Color(0xFFEA580C),
            softContainer = Color(0xFFFFF2E8),
            onSoftContainer = Color(0xFF9A3412)
        ),
        ElanGradientTheme(
            id = "rose_quartz",
            name = "Rose Quartz",
            subtitle = "Ciel irisé & framboise douce",
            startColor = Color(0xFF0EA5E9),
            midColor = Color(0xFFC026D3),
            endColor = Color(0xFFE11D48),
            ambientTop = Color(0xFFDDF3FF),
            ambientMid = Color(0xFFFCE2F6),
            ambientBottom = Color(0xFFFFF6FB),
            accentColor = Color(0xFFC026D3),
            softContainer = Color(0xFFFCEEFF),
            onSoftContainer = Color(0xFF86198F)
        ),
        ElanGradientTheme(
            id = "nebuleuse_douce",
            name = "Nébuleuse Douce",
            subtitle = "Indigo velours & orchidée",
            startColor = Color(0xFF3C4487),
            midColor = Color(0xFF8935CA),
            endColor = Color(0xFFD5539E),
            ambientTop = Color(0xFFDFE2FB),
            ambientMid = Color(0xFFF0E0FC),
            ambientBottom = Color(0xFFFCF4FA),
            accentColor = Color(0xFF7E22CE),
            softContainer = Color(0xFFF3EEFD),
            onSoftContainer = Color(0xFF581C87)
        ),
        ElanGradientTheme(
            id = "matcha_azur",
            name = "Matcha Azur",
            subtitle = "Pistache lumineuse & lagon",
            startColor = Color(0xFF65A30D),
            midColor = Color(0xFF10B981),
            endColor = Color(0xFF0284C7),
            ambientTop = Color(0xFFE4FAD4),
            ambientMid = Color(0xFFDDF8EE),
            ambientBottom = Color(0xFFF4FBFE),
            accentColor = Color(0xFF0D9488),
            softContainer = Color(0xFFEBFBF4),
            onSoftContainer = Color(0xFF115E59)
        ),
        ElanGradientTheme(
            id = "sakura_velours",
            name = "Fleur de Cerisier",
            subtitle = "Sakura électrique & prune",
            startColor = Color(0xFFDB2777),
            midColor = Color(0xFF9333EA),
            endColor = Color(0xFF4338CA),
            ambientTop = Color(0xFFFFDCEB),
            ambientMid = Color(0xFFF3E0FF),
            ambientBottom = Color(0xFFFAF5FF),
            accentColor = Color(0xFFDB2777),
            softContainer = Color(0xFFFFEDF5),
            onSoftContainer = Color(0xFF9D174D)
        )
    )

    fun byId(id: String?): ElanGradientTheme =
        all.firstOrNull { it.id == id } ?: all.first()
}

@Composable
fun rememberAnimatedGradientTheme(target: ElanGradientTheme): ElanGradientTheme {
    val spec = spring<Color>(
        dampingRatio = 0.85f,
        stiffness = Spring.StiffnessLow
    )
    val start = animateColorAsState(target.startColor, animationSpec = spec, label = "gradStart")
    val mid = animateColorAsState(target.midColor, animationSpec = spec, label = "gradMid")
    val end = animateColorAsState(target.endColor, animationSpec = spec, label = "gradEnd")
    val ambientTop = animateColorAsState(target.ambientTop, animationSpec = spec, label = "gradAmbTop")
    val ambientMid = animateColorAsState(target.ambientMid, animationSpec = spec, label = "gradAmbMid")
    val ambientBottom = animateColorAsState(target.ambientBottom, animationSpec = spec, label = "gradAmbBot")
    val accent = animateColorAsState(target.accentColor, animationSpec = spec, label = "gradAccent")
    val soft = animateColorAsState(target.softContainer, animationSpec = spec, label = "gradSoft")
    val onSoft = animateColorAsState(target.onSoftContainer, animationSpec = spec, label = "gradOnSoft")

    return ElanGradientTheme(
        id = target.id,
        name = target.name,
        subtitle = target.subtitle,
        startColor = start.value,
        midColor = mid.value,
        endColor = end.value,
        ambientTop = ambientTop.value,
        ambientMid = ambientMid.value,
        ambientBottom = ambientBottom.value,
        accentColor = accent.value,
        softContainer = soft.value,
        onSoftContainer = onSoft.value
    )
}

val LocalElanGradient = staticCompositionLocalOf { ElanGradients.all.first() }
