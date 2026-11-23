package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.LibraryFolder
import com.example.data.model.LibraryItem
import com.example.data.model.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryPickerSheet(
    foldersState: UiState<List<LibraryFolder>>,
    itemsState: UiState<List<LibraryItem>>,
    onPick: (LibraryItem) -> Unit,
    onDismiss: () -> Unit
) {
    val folders = (foldersState as? UiState.Success)?.data.orEmpty()
    val items = (itemsState as? UiState.Success)?.data.orEmpty()
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    val filtered = if (selectedFolder == null) items else items.filter { it.folderId == selectedFolder }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Choisir dans la bibliothèque", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Insère le texte extrait d'un document de ta bibliothèque dans le message. Le document reste privé à ton compte.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = selectedFolder == null,
                    onClick = { selectedFolder = null },
                    label = { Text("Tous (${items.size})") },
                    modifier = Modifier.testTag("libpick_all")
                )
                folders.take(5).forEach { f ->
                    FilterChip(
                        selected = selectedFolder == f.id,
                        onClick = { selectedFolder = f.id },
                        label = { Text(f.name, maxLines = 1) },
                        modifier = Modifier.testTag("libpick_folder_${f.id}")
                    )
                }
            }

            if (filtered.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Aucun document dans cette vue. Ajoute-en depuis Bibliothèque.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filtered.take(30), key = { it.id }) { item ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(item) }
                                .testTag("libpick_item_${item.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.Description, contentDescription = null, modifier = androidx.compose.ui.unit.dp.size(18.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        "${item.category} · ${item.mimeType} · ${folders.firstOrNull { it.id == item.folderId }?.name ?: "Sans dossier"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(onClick = { onPick(item) }) { Text("Insérer") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SaveToLibrarySheet(
    folders: List<LibraryFolder>,
    initialTitle: String,
    initialContent: String,
    onSave: (folderId: String, title: String, mimeType: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle.take(80)) }
    var folderId by remember { mutableStateOf(folders.firstOrNull()?.id ?: "") }
    var mime by remember { mutableStateOf("markdown") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Enregistrer dans la bibliothèque", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            androidx.compose.material3.OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(120) },
                label = { Text("Titre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("save_lib_title")
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("markdown" to "Markdown", "html" to "HTML", "text" to "Texte").forEach { (k, l) ->
                    FilterChip(selected = mime == k, onClick = { mime = k }, label = { Text(l) })
                }
            }
            if (folders.isNotEmpty()) {
                Text("Dossier", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = folderId == "", onClick = { folderId = "" }, label = { Text("Sans dossier") })
                    folders.take(5).forEach { f ->
                        FilterChip(selected = folderId == f.id, onClick = { folderId = f.id }, label = { Text(f.name) })
                    }
                }
            }
            Text(
                "Le contenu sera stocké comme texte borné (max 800k car.), visible uniquement par ce compte, avec cache hors-ligne.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                TextButton(onClick = onDismiss) { Text("Annuler") }
                androidx.compose.material3.Button(
                    onClick = { onSave(folderId, title.ifBlank { "Artefact du ${java.time.LocalDate.now()}" }, mime) },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.testTag("save_lib_confirm")
                ) { Text("Enregistrer") }
            }
        }
    }
}
