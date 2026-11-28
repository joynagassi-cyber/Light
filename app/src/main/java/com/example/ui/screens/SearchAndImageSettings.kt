package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.preferences.StudyAppSettings

@Composable
fun SearchSettingsSection(
    settings: StudyAppSettings,
    onUpdateSearch: (provider: String, enabled: Boolean, apiKey: String?, clearKey: Boolean) -> Unit
) {
    var provider by remember(settings.searchProvider, settings.searchEnabled) { mutableStateOf(settings.searchProvider) }
    var enabled by remember(settings.searchEnabled) { mutableStateOf(settings.searchEnabled) }
    var apiKey by remember { mutableStateOf("") }
    var clearKey by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(16.dp)) {
        Text("Recherche web (Tavily / Exa)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(
            "Désactivée par défaut. Activez explicitement un fournisseur et ajoutez sa clé locale (chiffrée sur l'appareil). Aucune requête n'est effectuée sans votre action. Les résultats affichent toujours leurs sources.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = provider == "none", onClick = { provider = "none" }, label = { Text("Désactivée") }, modifier = Modifier.testTag("search_provider_none"))
            FilterChip(selected = provider == "tavily", onClick = { provider = "tavily" }, label = { Text("Tavily") }, modifier = Modifier.testTag("search_provider_tavily"))
            FilterChip(selected = provider == "exa", onClick = { provider = "exa" }, label = { Text("Exa") }, modifier = Modifier.testTag("search_provider_exa"))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Activer la recherche", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Switch(checked = enabled, onCheckedChange = { enabled = it }, modifier = Modifier.testTag("search_enabled_switch"))
        }
        if (provider != "none") {
            OutlinedTextField(
                value = apiKey, onValueChange = { apiKey = it; if (it.isNotBlank()) clearKey = false },
                label = { Text(if (provider == settings.searchProvider) "Nouvelle clé API (optionnel)" else "Clé API $provider") },
                placeholder = { Text("Saisir la clé API") },
                supportingText = { Text("Chiffrée via Android Keystore. Jamais journalisée.") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth().testTag("search_apikey_input")
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = clearKey, onClick = { clearKey = !clearKey; if (clearKey) apiKey = "" }, label = { Text("Effacer la clé enregistrée") })
            }
            Text(
                "Tavily: https://docs.tavily.com — Exa: https://docs.exa.ai — Vérifiez quotas et confidentialité avant usage.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(
            onClick = { onUpdateSearch(provider, enabled, apiKey.takeIf { it.isNotBlank() }, clearKey) },
            modifier = Modifier.testTag("search_save_button")
        ) { Text("Enregistrer la recherche") }
    }
}

@Composable
fun GroundingSettingsSection(
    settings: StudyAppSettings,
    onUpdateGrounding: (Boolean) -> Unit
) {
    var enabled by remember(settings.agentGroundingEnabled) { mutableStateOf(settings.agentGroundingEnabled) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(16.dp)) {
        Text("Grounding Google (Firebase)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(
            "Expérimental, désactivé par défaut. Le modèle Gemini appuie ses réponses sur Google Search, côté fournisseur. Nécessite que le grounding soit disponible pour votre projet Firebase (quota et conditions Gemini). Selon le modèle, la combinaison avec les outils Light peut être refusée par l'API — auquel cas les messages de l'agent afficheront l'erreur correspondante.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Activer le grounding Google", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Switch(checked = enabled, onCheckedChange = { enabled = it; onUpdateGrounding(it) }, modifier = Modifier.testTag("agent_grounding_switch"))
        }
    }
}

@Composable
fun ImageSettingsSection(
    settings: StudyAppSettings,
    onUpdateImage: (provider: String, enabled: Boolean, size: String, apiKey: String?, clearKey: Boolean) -> Unit
) {
    var provider by remember(settings.imageProvider, settings.imageEnabled) { mutableStateOf(settings.imageProvider) }
    var enabled by remember(settings.imageEnabled) { mutableStateOf(settings.imageEnabled) }
    var size by remember(settings.imageSize) { mutableStateOf(settings.imageSize) }
    var apiKey by remember { mutableStateOf("") }
    var clearKey by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(16.dp)) {
        Text("Génération d'images", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(
            "Désactivée par défaut. Fournisseurs vérifiables: OpenAI (https://platform.openai.com/docs/guides/images) et OpenRouter. Agnes (apihub.agnes-ai.com) non intégré — identité en attente de source primaire. Les images sont enregistrées dans la bibliothèque, dossier au choix.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = provider == "none", onClick = { provider = "none" }, label = { Text("Désactivée") }, modifier = Modifier.testTag("image_provider_none"))
            FilterChip(selected = provider == "openai", onClick = { provider = "openai" }, label = { Text("OpenAI") }, modifier = Modifier.testTag("image_provider_openai"))
            FilterChip(selected = provider == "openrouter", onClick = { provider = "openrouter" }, label = { Text("OpenRouter") }, modifier = Modifier.testTag("image_provider_openrouter"))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Activer la génération", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Switch(checked = enabled, onCheckedChange = { enabled = it }, modifier = Modifier.testTag("image_enabled_switch"))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("512x512", "1024x1024", "1024x1792").forEach { s ->
                FilterChip(selected = size == s, onClick = { size = s }, label = { Text(s) })
            }
        }
        if (provider != "none") {
            OutlinedTextField(
                value = apiKey, onValueChange = { apiKey = it; if (it.isNotBlank()) clearKey = false },
                label = { Text(if (provider == settings.imageProvider) "Nouvelle clé API (optionnel)" else "Clé API $provider") },
                placeholder = { Text("Saisir la clé API") },
                supportingText = { Text("Chiffrée via Android Keystore. Jamais journalisée.") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth().testTag("image_apikey_input")
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = clearKey, onClick = { clearKey = !clearKey; if (clearKey) apiKey = "" }, label = { Text("Effacer la clé enregistrée") })
            }
        }
        TextButton(
            onClick = { onUpdateImage(provider, enabled, size, apiKey.takeIf { it.isNotBlank() }, clearKey) },
            modifier = Modifier.testTag("image_save_button")
        ) { Text("Enregistrer les images") }
    }
}
