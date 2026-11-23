package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.LibraryFolder
import com.example.data.repository.SearchResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAgentSheet(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<SearchResult>,
    loading: Boolean,
    error: String?,
    enabled: Boolean,
    providerName: String,
    onSearch: () -> Unit,
    onPick: (SearchResult) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Rechercher sur le web", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (!enabled) {
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Recherche désactivée. Activez Tavily ou Exa dans Paramètres → Recherche & Images et ajoutez votre clé locale.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Text(
                    "Fournisseur: $providerName — requête bornée (max 300 car.), résultats avec URL source. Pas de navigation arbitraire. Clé chiffrée localement.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Requête") },
                placeholder = { Text("ex: meilleures techniques de révision") },
                singleLine = true,
                enabled = enabled && !loading,
                modifier = Modifier.fillMaxWidth().testTag("search_query_input")
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSearch,
                    enabled = enabled && query.trim().length >= 2 && !loading,
                    modifier = Modifier.testTag("search_execute_button")
                ) {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    else Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    androidx.compose.foundation.layout.Spacer(Modifier.size(6.dp))
                    Text(if (loading) "Recherche..." else "Rechercher")
                }
                TextButton(onClick = onClear, modifier = Modifier.testTag("search_clear_button")) { Text("Effacer") }
            }
            if (error != null) {
                Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("search_error"))
            }
            if (results.isNotEmpty()) {
                Text("${results.size} résultat(s) — sources visibles ci-dessous", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(results, key = { it.url }) { r ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth().testTag("search_result_${r.url.hashCode()}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(r.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(r.url, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (r.snippet.isNotBlank()) Text(r.snippet, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                Text("Source: ${r.source} — attribution obligatoire si réutilisé", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                TextButton(onClick = { onPick(r) }, modifier = Modifier.testTag("search_pick_${r.url.hashCode()}")) { Text("Insérer dans le message") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageAgentSheet(
    prompt: String,
    onPromptChange: (String) -> Unit,
    folderId: String,
    onFolderChange: (String) -> Unit,
    folders: List<LibraryFolder>,
    generating: Boolean,
    error: String?,
    enabled: Boolean,
    providerName: String,
    onGenerate: () -> Unit,
    onClearError: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Générer une image", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (!enabled) {
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Génération désactivée. Activez OpenAI ou OpenRouter dans Paramètres → Recherche & Images et ajoutez votre clé locale.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Text(
                    "Fournisseur: $providerName — image 1024x1024 (modifiable en réglages). Agnes non proposé (identité non vérifiée). L'image sera enregistrée dans la bibliothèque, dossier au choix.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                label = { Text("Prompt") },
                placeholder = { Text("ex: illustration aquarelle d'un laboratoire étudiant") },
                minLines = 2,
                maxLines = 4,
                enabled = enabled && !generating,
                modifier = Modifier.fillMaxWidth().testTag("image_prompt_input")
            )
            if (folders.isNotEmpty()) {
                Text("Dossier de destination", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = folderId == "", onClick = { onFolderChange("") }, label = { Text("Sans dossier") })
                    folders.take(5).forEach { f ->
                        FilterChip(selected = folderId == f.id, onClick = { onFolderChange(f.id) }, label = { Text(f.name) }, modifier = Modifier.testTag("image_folder_${f.id}"))
                    }
                }
            }
            if (error != null) {
                Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("image_error"))
                TextButton(onClick = onClearError) { Text("Effacer") }
            }
            Button(
                onClick = onGenerate,
                enabled = enabled && prompt.trim().length >= 3 && !generating,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().testTag("image_generate_button")
            ) {
                if (generating) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                else Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
                Text(if (generating) "Génération..." else "Générer et enregistrer")
            }
            Text(
                "Les images générées sont facturées par le fournisseur. Vérifiez quotas avant usage. Aucune clé ni URL brute n'est journalisée.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
