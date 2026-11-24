package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.document.pageAgentMarkdown
import com.example.data.document.writeAgentMarkdown
import com.example.data.repository.AgentChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.ActivityNotFoundException

@Composable
internal fun AgentMessageDocumentCard(text: String, streaming: Boolean, onOpen: () -> Unit) {
    Surface(
        onClick = if (streaming) { {} } else onOpen,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().testTag("agent_message_document")
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), modifier = Modifier.padding(0.dp)) {
                Icon(Icons.Rounded.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp).size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("Document Markdown", style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                Text("Appuyez pour ouvrir la vue rendue sans caractères spéciaux", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (streaming) Text("En cours", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else TextButton(onClick = onOpen, modifier = Modifier.testTag("agent_open_document")) { Text("Ouvrir") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AgentMessageDocumentReader(message: AgentChatMessage, onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pages = remember(message.text) { pageAgentMarkdown(message.text) }
    var requestedPage by rememberSaveable(message.id) { mutableIntStateOf(0) }
    val page = requestedPage.coerceIn(pages.indices)
    var writing by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    val saveFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { uri ->
        if (uri != null) scope.launch {
            writing = true
            feedback = try {
                withContext(Dispatchers.IO) {
                    val output = checkNotNull(context.contentResolver.openOutputStream(uri, "wt"))
                    writeAgentMarkdown(output, message.text)
                }
                "Fichier Markdown enregistré."
            } catch (_: Exception) { "Impossible d’enregistrer le fichier à cet emplacement." } finally { writing = false }
        }
    }
    ModalBottomSheet(
        onDismissRequest = { if (!writing) onClose() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("agent_document_sheet")
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Document Markdown — rendu fidèle", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text("Source du message · page ${page + 1}/${pages.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AnimatedVisibility(visible = true, enter = slideInVertically(initialOffsetY = { -it / 3 }), exit = slideOutVertically(targetOffsetY = { -it })) {
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
                        key(page) {
                            // Render markdown cleanly without raw special chars — AgentMarkdown handles headings, lists, code
                            AgentMarkdown(text = pages[page].trim(), modifier = Modifier.fillMaxWidth().testTag("agent_document_page"))
                        }
                    }
                }
            }
            if (pages.size > 1) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { requestedPage = page - 1 }, enabled = page > 0) { Text("Précédente") }
                    Text("Page ${page + 1} / ${pages.size}", style = MaterialTheme.typography.labelMedium)
                    TextButton(onClick = { requestedPage = page + 1 }, enabled = page < pages.lastIndex, modifier = Modifier.testTag("agent_document_next")) { Text("Suivante") }
                }
            }
            if (writing) Text("Enregistrement…", style = MaterialTheme.typography.bodySmall) else feedback?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
                TextButton(enabled = !writing, onClick = onClose) { Text("Fermer") }
                androidx.compose.material3.Button(enabled = !writing, onClick = {
                    try { saveFile.launch("message-light.md") } catch (_: ActivityNotFoundException) { feedback = "Aucun sélecteur de fichiers n’est disponible." }
                }, modifier = Modifier.testTag("agent_export_document")) { Text("Exporter .md") }
            }
        }
    }
}
