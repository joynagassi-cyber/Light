package com.example.ui.components

import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.material3.Icon
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.example.data.repository.AgentActionLog
import com.example.data.repository.AgentToolContract
import com.example.data.repository.AgentTurnPhase
import com.example.data.repository.AgentTurnProgress
import com.example.ui.theme.LightSpacing
import kotlinx.coroutines.delay

private fun AgentTurnPhase?.activityLabel() = when (this) {
    AgentTurnPhase.STREAMING -> "Réponse en cours"
    AgentTurnPhase.CHECKING_TOOL -> "Vérification d’un outil"
    AgentTurnPhase.AWAITING_APPROVAL -> "Autorisation nécessaire"
    AgentTurnPhase.RUNNING_TOOL -> "Outil en cours"
    AgentTurnPhase.RESUMING -> "Poursuite de la réponse"
    AgentTurnPhase.REQUESTING -> "Attente du fournisseur"
    null -> "Activité du message"
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
    var expanded by rememberSaveable(messageId) { mutableStateOf(false) }
    val startedAt = live?.startedAtMillis
    var elapsed by remember(startedAt, completedMillis) { mutableLongStateOf(completedMillis ?: 0L) }
    LaunchedEffect(startedAt) {
        if (startedAt != null) while (true) {
            elapsed = (SystemClock.elapsedRealtime() - startedAt).coerceAtLeast(0)
            delay(500)
        }
    }
    val label = live?.activity?.phase.activityLabel()
    val actions = live?.tools ?: completedActions
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(LightSpacing.half)) {
        TextButton(onClick = { expanded = !expanded }, modifier = Modifier.testTag("agent_activity_toggle")) {
            Text(label, modifier = Modifier.semantics { if (live != null) liveRegion = LiveRegionMode.Polite })
            if (startedAt != null || completedMillis != null) Text(" · ${formatAgentActivityTime(elapsed)}")
            if (actions.isNotEmpty()) Text(" · ${actions.size} outil${if (actions.size > 1) "s" else ""}")
            Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                if (expanded) "Replier les détails" else "Afficher les détails")
        }
        if (expanded) {
            Column(Modifier.padding(start = LightSpacing.two).testTag("agent_activity_details"),
                verticalArrangement = Arrangement.spacedBy(LightSpacing.half)) {
                Text("Étapes observées par Light", style = MaterialTheme.typography.labelMedium)
                live?.activity?.toolName?.let { name ->
                    val title = AgentToolContract[name]?.description?.substringBefore(". Demander confirmation") ?: "Outil non disponible"
                    Text(title.removeSuffix("."), style = MaterialTheme.typography.bodySmall)
                }
                if (actions.isEmpty()) Text("Aucun résultat d’outil à afficher", style = MaterialTheme.typography.bodySmall)
                actions.forEach { action ->
                    Row(horizontalArrangement = Arrangement.spacedBy(LightSpacing.one), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (action.succeeded) "Réussi" else "Non effectué", style = MaterialTheme.typography.labelSmall,
                            color = if (action.succeeded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        Text(action.label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        action.elapsedMillis?.let { Text(formatAgentActivityTime(it), style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }
    }
}
