package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.model.AgentProvider
import com.example.data.model.AgentModelOption
import androidx.compose.ui.unit.dp
import com.example.data.model.AgentSkill
import com.example.data.preferences.StudyAppSettings
import java.util.UUID

@Composable
fun AgentSettingsDialog(
    settings: StudyAppSettings,
    apiKeyConfigured: Boolean,
    models: List<AgentModelOption>,
    modelsLoading: Boolean,
    modelsError: String?,
    onLoadModels: (String, String, String?) -> Unit,
    onDismiss: () -> Unit,
    onSave: (provider: String, baseUrl: String, model: String, reflection: String, capabilities: Set<String>, skills: List<AgentSkill>, apiKey: String?, clearApiKey: Boolean) -> Unit
) {
    var provider by remember(settings.agentProvider) { mutableStateOf(settings.agentProvider) }
    var baseUrl by remember(settings.agentBaseUrl) { mutableStateOf(settings.agentBaseUrl) }
    var model by remember(settings.agentModel) { mutableStateOf(settings.agentModel) }
    var reflection by remember(settings.agentReflection) { mutableStateOf(settings.agentReflection) }
    var skills by remember(settings.agentSkills) { mutableStateOf(settings.agentSkills) }
    var apiKey by remember { mutableStateOf("") }
    var clearKey by remember { mutableStateOf(false) }
    var newSkillName by remember { mutableStateOf("") }
    var newSkillInstructions by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurer Light") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Fournisseur IA", style = MaterialTheme.typography.titleSmall)
                (AgentProvider.entries.filter { it != AgentProvider.CUSTOM } + AgentProvider.CUSTOM).forEach { option ->
                    val logo = when (option) {
                        AgentProvider.AGNES -> R.drawable.provider_agnes
                        AgentProvider.OPENAI -> R.drawable.provider_openai
                        AgentProvider.ANTHROPIC -> R.drawable.provider_anthropic
                        AgentProvider.OPENROUTER -> R.drawable.provider_openrouter
                        AgentProvider.GROQ -> R.drawable.provider_groq
                        else -> null
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            selected = provider == option.id,
                            onClick = {
                                provider = option.id
                                baseUrl = option.baseUrl
                                model = option.defaultModel
                            },
                            label = { Text(option.displayName) }
                        )
                        if (logo != null) Image(painterResource(logo), contentDescription = "Logo ${option.displayName}", modifier = Modifier.padding(start = 8.dp).height(24.dp))
                    }
                }
                Text(
                    "La clé et les messages sont transmis directement au fournisseur choisi. L'URL doit utiliser HTTPS; vérifiez les conditions du fournisseur avant usage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (provider != AgentProvider.FIREBASE.id) {
                    OutlinedTextField(
                        value = baseUrl, onValueChange = { baseUrl = it },
                        label = { Text("URL de base HTTPS") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = apiKey, onValueChange = { apiKey = it; if (it.isNotBlank()) clearKey = false },
                        label = { Text(if (apiKeyConfigured && provider == settings.agentProvider) "Nouvelle clé API (facultatif)" else "Clé API") },
                        placeholder = { Text(if (apiKeyConfigured && provider == settings.agentProvider) "Une clé est enregistrée pour ce fournisseur" else "Saisir la clé API") },
                        supportingText = { Text("Chiffrée sur cet appareil avec Android Keystore.") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    if (apiKeyConfigured && provider == settings.agentProvider) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = clearKey, onCheckedChange = { clearKey = it; if (it) apiKey = "" })
                            Spacer(Modifier.width(8.dp))
                            Text("Effacer la clé enregistrée", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onLoadModels(provider, baseUrl, apiKey.takeIf { it.isNotBlank() }) }, enabled = !modelsLoading) {
                            if (modelsLoading) CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                            else Text("Charger les modèles")
                        }
                        if (modelsError != null) Text(modelsError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Button(onClick = { onLoadModels(provider, baseUrl, null) }) { Text("Afficher les modèles Gemini") }
                }
                models.forEach { candidate ->
                    val badge = candidate.costDescription
                    FilterChip(
                        selected = model == candidate.id,
                        onClick = { model = candidate.id },
                        label = { Text("${candidate.displayName} · $badge") }
                    )
                }
                if (models.isEmpty() && provider != AgentProvider.FIREBASE.id) {
                    OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Identifiant du modèle (secours)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }

                Text("Niveau de réflexion", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("quick" to "Rapide", "balanced" to "Équilibré", "deep" to "Approfondi").forEach { (key, label) ->
                        FilterChip(selected = reflection == key, onClick = { reflection = key }, label = { Text(label) })
                    }
                }

                Text("Compétences personnalisées", style = MaterialTheme.typography.titleSmall)
                Text("Ajoutez des consignes réutilisables pour apprendre à l'agent votre façon de travailler.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(newSkillName, { newSkillName = it }, label = { Text("Nom de la compétence") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(newSkillInstructions, { newSkillInstructions = it }, label = { Text("Consignes et méthode") }, minLines = 2, maxLines = 4, modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = {
                        val name = newSkillName.trim()
                        val instructions = newSkillInstructions.trim()
                        if (name.isNotEmpty() && instructions.isNotEmpty() && skills.size < 20) {
                            skills = skills + AgentSkill(UUID.randomUUID().toString(), name.take(80), instructions.take(2000))
                            newSkillName = ""
                            newSkillInstructions = ""
                        }
                    },
                    enabled = newSkillName.isNotBlank() && newSkillInstructions.isNotBlank() && skills.size < 20
                ) { Text("Ajouter la compétence") }
                skills.forEach { skill ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = skill.enabled,
                                onCheckedChange = { enabled -> skills = skills.map { if (it.id == skill.id) it.copy(enabled = enabled) else it } }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(skill.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = { skills = skills.filterNot { it.id == skill.id } }) { Text("Retirer") }
                        }
                        Text(skill.instructions, Modifier.padding(start = 56.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(provider, baseUrl.trim(), model.trim(), reflection, settings.agentCapabilities, skills, apiKey.takeIf { it.isNotBlank() }, clearKey)
            }) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
