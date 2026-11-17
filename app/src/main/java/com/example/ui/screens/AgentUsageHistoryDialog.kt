package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.AgentUsageRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AgentUsageHistoryDialog(records: List<AgentUsageRecord>, onClear: () -> Unit, onDismiss: () -> Unit) {
    val byModel = records.groupBy { it.providerId to it.modelId }.entries
        .sortedByDescending { entry -> entry.value.sumOf { it.createdAtMillis } }
    val maximum = byModel.maxOfOrNull { it.value.sumOf { record -> record.inputTokens + record.outputTokens } }?.coerceAtLeast(1L) ?: 1L
    val estimatedTotal = records.mapNotNull { it.estimatedCostUsd }.takeIf { costs -> costs.size == records.size }?.sum()
    val barColor = MaterialTheme.colorScheme.primary
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Historique d'utilisation") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "${records.size} appels · coût estimé: ${estimatedTotal?.let { "$" + String.format(Locale.US, "%.6f", it) } ?: "incomplet (tarifs ou tokens indisponibles)"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text("Tokens par modèle", style = MaterialTheme.typography.titleSmall)
                byModel.forEach { (key, items) ->
                    val tokens = items.sumOf { it.inputTokens + it.outputTokens }
                    Column {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${key.first} · ${key.second}", style = MaterialTheme.typography.bodySmall)
                            Text("$tokens", style = MaterialTheme.typography.bodySmall)
                        }
                        Canvas(Modifier.fillMaxWidth().height(8.dp).padding(top = 2.dp)) {
                            drawRect(Color.LightGray.copy(alpha = 0.35f))
                            drawRect(barColor, size = size.copy(width = size.width * tokens.toFloat() / maximum.toFloat()))
                        }
                        val groupCost = items.mapNotNull { it.estimatedCostUsd }.takeIf { it.size == items.size }?.sum()
                        Text(
                            groupCost?.let { "Coût indicatif \$${String.format(Locale.US, "%.6f", it)}" } ?: "Coût inconnu · consulter le fournisseur",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text("Appels récents", style = MaterialTheme.typography.titleSmall)
                LazyColumn(modifier = Modifier.height(200.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(records.sortedByDescending { it.createdAtMillis }.take(30), key = { it.id }) { record ->
                        val date = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(record.createdAtMillis))
                        Column {
                            Text("$date · ${record.providerId}/${record.modelId}", style = MaterialTheme.typography.bodySmall)
                            Text("${record.inputTokens} entrée · ${record.outputTokens} sortie · ${record.estimatedCostUsd?.let { "$" + String.format(Locale.US, "%.6f", it) } ?: "coût inconnu"}", style = MaterialTheme.typography.labelSmall)
                            Text(record.costNote, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fermer") } },
        dismissButton = { TextButton(onClick = onClear) { Text("Effacer") } }
    )
}
