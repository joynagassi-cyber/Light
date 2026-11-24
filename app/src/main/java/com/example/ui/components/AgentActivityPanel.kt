package com.example.ui.components

import android.os.SystemClock
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.AgentActionLog
import com.example.data.repository.AgentToolContract
import com.example.data.repository.AgentTurnPhase
import com.example.data.repository.AgentTurnProgress
import com.example.ui.theme.LightSpacing
import kotlinx.coroutines.delay

private fun AgentTurnPhase?.activityLabel() = when (this) {
    AgentTurnPhase.STREAMING -> "Rédaction en cours"
    AgentTurnPhase.CHECKING_TOOL -> "Analyse des outils disponibles"
    AgentTurnPhase.AWAITING_APPROVAL -> "En attente de votre validation"
    AgentTurnPhase.RUNNING_TOOL -> "Exécution d’un outil"
    AgentTurnPhase.RESUMING -> "Reprise de la réflexion"
    AgentTurnPhase.REQUESTING -> "Réflexion en cours"
    null -> "Raisonnement terminé"
}

internal fun formatAgentActivityTime(elapsedMillis: Long): String {
    val seconds = elapsedMillis.coerceAtLeast(0) / 1000L
    return if (seconds < 60) "${seconds} s" else "${seconds / 60} min ${seconds % 60} s"
}

@Composable
internal fun AgentActivityPanel(
    messageId: String,
    live: AgentTurnProgress?,
    completedActions: List<AgentActionLog>,
    completedMillis: Long?,
) {
    if (live == null && completedActions.isEmpty() && completedMillis == null) return
    var expanded by rememberSaveable(messageId) { mutableStateOf(live != null) }
    val startedAt = live?.startedAtMillis
    var elapsed by remember(startedAt, completedMillis) { mutableLongStateOf(completedMillis ?: 0L) }
    LaunchedEffect(startedAt) {
        if (startedAt != null) while (true) {
            elapsed = (SystemClock.elapsedRealtime() - startedAt).coerceAtLeast(0)
            delay(500)
        }
    }
    val isThinking = live != null
    val label = live?.activity?.phase.activityLabel() ?: if (completedActions.isNotEmpty()) "Outils utilisés · ${completedActions.size}" else "Réflexion terminée"
    val actions = live?.tools ?: completedActions
    val infiniteTransition = rememberInfiniteTransition(label = "thinkingDots")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dotAlpha"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isThinking) Color(0xFFF4F1EA) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        tonalElevation = if (isThinking) 1.dp else 0.dp,
        modifier = Modifier.fillMaxWidth().testTag("agent_thinking_panel")
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(if (isThinking) Color(0xFFF0512B).copy(alpha = 0.14f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = if (isThinking) Color(0xFFF0512B) else MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp).alpha(if (isThinking) dotAlpha else 1f))
                }
                Column(Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.semantics { if (isThinking) liveRegion = LiveRegionMode.Polite })
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (startedAt != null || completedMillis != null) Text(formatAgentActivityTime(elapsed), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (actions.isNotEmpty()) Text("· ${actions.size} outil${if (actions.size > 1) "s" else ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (isThinking) Text("· en réflexion", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF0512B))
                    }
                }
                TextButton(onClick = { expanded = !expanded }, modifier = Modifier.testTag("agent_activity_toggle")) {
                    Text(if (expanded) "Masquer" else "Détails")
                    Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, contentDescription = if (expanded) "Replier" else "Afficher")
                }
            }
            if (isThinking) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    repeat(3) { idx ->
                        val alpha = when (idx) {
                            0 -> dotAlpha
                            1 -> (dotAlpha * 0.8f + 0.2f).coerceIn(0.3f, 1f)
                            else -> (1.1f - dotAlpha).coerceIn(0.3f, 1f)
                        }
                        Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFF0512B).copy(alpha = alpha)))
                    }
                    Text("L’agent analyse votre demande et prépare une réponse structurée…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
                }
            }
            if (expanded) {
                Column(Modifier.fillMaxWidth().padding(start = 4.dp).testTag("agent_activity_details"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Parcours de réflexion", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    live?.activity?.toolName?.let { name ->
                        val title = AgentToolContract[name]?.description?.substringBefore(". Demander confirmation") ?: "Outil en cours d’évaluation"
                        Surface(shape = RoundedCornerShape(10.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                            Text(title.removeSuffix("."), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                        }
                    }
                    if (actions.isEmpty()) {
                        Text("Aucun outil sollicité pour ce tour — réponse directe.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        actions.forEach { action ->
                            Surface(shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 0.5.dp, modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(8.dp).clip(CircleShape).background(if (action.succeeded) Color(0xFF0E7C6B) else Color(0xFFC62828)))
                                    Column(Modifier.weight(1f)) {
                                        Text(action.label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                        Text(if (action.succeeded) "Exécuté avec succès" else "Non effectué", style = MaterialTheme.typography.labelSmall, color = if (action.succeeded) Color(0xFF0E7C6B) else Color(0xFFC62828))
                                    }
                                    action.elapsedMillis?.let { Text(formatAgentActivityTime(it), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
