package com.example.ui.components

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun AgentMessageDocumentCard(text: String, streaming: Boolean, onOpen: () -> Unit) {
    ListItem(
        leadingContent = { Icon(Icons.Rounded.Description, contentDescription = null) },
        headlineContent = { Text("Document Markdown") },
        supportingContent = { Text(text.take(220), maxLines = 3, overflow = TextOverflow.Ellipsis) },
        trailingContent = {
            if (streaming) Text("En cours", style = MaterialTheme.typography.labelSmall)
            else TextButton(onClick = onOpen, modifier = Modifier.testTag("agent_open_document")) { Text("Ouvrir") }
        },
        modifier = Modifier.testTag("agent_message_document"),
    )
}

/** The chooser resolves a user-selected URI; no work is done before that explicit choice. */
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
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                "Impossible d’enregistrer le fichier à cet emplacement."
            } finally {
                writing = false
            }
        }
    }
    AlertDialog(
        onDismissRequest = { if (!writing) onClose() },
        title = { Text("Document Markdown") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Source du message · page ${page + 1}/${pages.size}", style = MaterialTheme.typography.labelMedium)
                key(page) {
                    SelectionContainer(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                        Text(pages[page], modifier = Modifier.fillMaxWidth().padding(4.dp).testTag("agent_document_page"))
                    }
                }
                if (pages.size > 1) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { requestedPage = page - 1 }, enabled = page > 0) { Text("Précédente") }
                        TextButton(onClick = { requestedPage = page + 1 }, enabled = page < pages.lastIndex,
                            modifier = Modifier.testTag("agent_document_next")) { Text("Suivante") }
                    }
                }
                if (writing) Text("Enregistrement…") else feedback?.let { Text(it) }
            }
        },
        confirmButton = {
            TextButton(enabled = !writing, onClick = {
                try {
                    saveFile.launch("message-light.md")
                } catch (_: ActivityNotFoundException) {
                    feedback = "Aucun sélecteur de fichiers n’est disponible."
                }
            },
                modifier = Modifier.testTag("agent_export_document")) { Text("Exporter .md") }
        },
        dismissButton = { TextButton(enabled = !writing, onClick = onClose) { Text("Fermer") } },
    )
}
