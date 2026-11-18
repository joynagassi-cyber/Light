package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.document.ExtractedAgentDocument
import com.example.data.document.LocalDocumentExtractor
import com.example.data.model.AgentModelOption
import com.example.data.model.AgentProvider
import com.example.data.model.AgentUsageRecord
import com.example.data.model.AgentSkill
import com.example.data.preferences.StudyAppSettings
import com.example.data.repository.AgentActionApproval
import com.example.data.repository.AgentPlanPreview
import com.example.data.repository.AgentChatMessage
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleRose
import com.example.ui.theme.LocalLightGradient
import com.example.ui.theme.appBrush
import com.example.ui.theme.appVerticalBrush
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val MAX_ATTACHED_DOCUMENTS = 4
private const val MAX_DOCUMENT_PROMPT_CHARS = 60_000
private val SUPPORTED_DOCUMENT_MIME_TYPES = arrayOf(
    "text/markdown",
    "text/plain",
    "application/pdf",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "image/png"
)

private fun buildDocumentPrompt(prompt: String, documents: List<ExtractedAgentDocument>): String = buildString {
    append(prompt.ifBlank { "Analyse ces documents et propose un plan de travail ou des sessions d'étude adaptées." })
    append("\n\nConsignes de lecture des fichiers : conserve strictement les en-têtes, lignes, colonnes, nombres, unités et références de cellules. Dans les PDF, le séparateur | indique un changement probable de colonne; dans Excel, les références de cellules sont indiquées. Ne fusionne pas les valeurs de cellules différentes et signale toute structure ambiguë avant de planifier.\nTexte extrait localement des fichiers joints :")
    var remaining = (MAX_DOCUMENT_PROMPT_CHARS - prompt.length).coerceAtLeast(0)
    for (document in documents) {
        if (remaining <= 0) break
        val header = "\n\n--- ${document.displayName} (${document.formatLabel}) ---\n"
        append(header)
        remaining -= header.length
        val excerpt = document.text.take(remaining.coerceAtLeast(0))
        append(excerpt)
        remaining -= excerpt.length
        if (excerpt.length < document.text.length) append("\n[Texte tronqué pour limiter la taille envoyée.]")
    }
}.take(MAX_DOCUMENT_PROMPT_CHARS + 1000)

@Composable
fun AgentChatScreen(
    messages: List<AgentChatMessage>,
    isBusy: Boolean,
    pendingAction: AgentActionApproval?,
    onResolveAgentAction: (actionId: String, approved: Boolean) -> Unit,
    onSendMessage: (agentPrompt: String, visibleMessage: String) -> Unit,
    onClearConversation: () -> Unit,
    onOpenSettings: () -> Unit,
    settings: StudyAppSettings,
    apiKeyConfigured: Boolean,
    agentModels: List<AgentModelOption>,
    agentModelsLoading: Boolean,
    agentModelsError: String?,
    agentUsage: List<AgentUsageRecord>,
    onClearAgentUsage: () -> Unit,
    onLoadAgentModels: (String, String, String?) -> Unit,
    onSaveAgentSettings: (String, String, String, String, Set<String>, List<AgentSkill>, String?, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    var inputText by rememberSaveable { mutableStateOf("") }
    var showAgentSettings by rememberSaveable { mutableStateOf(false) }
    var showUsageHistory by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val documentExtractor = remember(context) { LocalDocumentExtractor(context.applicationContext) }
    val attachedDocuments = remember { mutableStateListOf<ExtractedAgentDocument>() }
    var isExtractingDocuments by remember { mutableStateOf(false) }
    var extractionError by remember { mutableStateOf<String?>(null) }
    var showDocumentConsent by remember { mutableStateOf(false) }
    val documentPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        val available = (MAX_ATTACHED_DOCUMENTS - attachedDocuments.size).coerceAtLeast(0)
        val overLimit = uris.size > available
        val selected = uris.take(available)
        if (selected.isEmpty() && overLimit) extractionError = "Vous pouvez joindre au maximum $MAX_ATTACHED_DOCUMENTS fichiers par message."
        if (selected.isNotEmpty()) scope.launch {
            isExtractingDocuments = true
            extractionError = if (overLimit) "Vous pouvez joindre au maximum $MAX_ATTACHED_DOCUMENTS fichiers par message; les fichiers excédentaires ont été ignorés." else null
            try {
                selected.forEach { uri ->
                    try {
                        attachedDocuments += documentExtractor.extract(uri)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        extractionError = e.localizedMessage ?: "Impossible de lire un fichier sélectionné."
                    }
                }
            } finally {
                isExtractingDocuments = false
            }
        }
    }

    val quickPrompts = listOf(
        Icons.Rounded.Search to "Quelles sont les tâches à accomplir",
        Icons.Rounded.Add to "Créer une tâche : réviser Architecture Logicielle demain à 14h",
        Icons.Rounded.Description to "En résumé, aujourd'hui...",
        Icons.Rounded.Schedule to "Planification des activités pour aujourd'hui"
    )

    LaunchedEffect(messages.size, isBusy, pendingAction?.id) {
        val lastVisibleIndex = if (pendingAction != null) messages.size else messages.lastIndex
        if (lastVisibleIndex >= 0) listState.animateScrollToItem(lastVisibleIndex)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp, bottom = 14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(gradient.appBrush()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Light Agent IA",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Discutez & opérez directement votre planning",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    onClick = onClearConversation,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("agent_clear_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Réinitialiser la conversation",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Surface(
                    onClick = { showAgentSettings = true },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.size(48.dp).testTag("agent_configuration_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Tune, contentDescription = "Configurer l’agent", tint = gradient.accentColor)
                    }
                }
                Surface(
                    onClick = onOpenSettings,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("agent_settings_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Paramètres",
                            tint = gradient.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick action prompt pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEachIndexed { idx, (icon, prompt) ->
                val shortChipLabel = when (idx) {
                    1 -> "Créer une tâche."
                    else -> prompt
                }
                Surface(
                    onClick = {
                        if (!isBusy) onSendMessage(prompt, prompt)
                    },
                    shape = CircleShape,
                    color = gradient.softContainer,
                    border = BorderStroke(0.8.dp, gradient.accentColor.copy(alpha = 0.25f)),
                    modifier = Modifier.testTag("agent_quick_prompt_$idx")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = gradient.accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = shortChipLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = gradient.accentColor
                        )
                    }
                }
            }
            Surface(
                onClick = { showUsageHistory = true },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.Description, contentDescription = null, tint = gradient.accentColor, modifier = Modifier.size(15.dp))
                    Text("Historique / coûts", style = MaterialTheme.typography.labelMedium, color = gradient.accentColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                AgentMessageBubble(message = msg)
            }

            pendingAction?.let { action ->
                item(key = "agent_approval_${action.id}") {
                    AgentActionApprovalCard(
                        action = action,
                        onApprove = { onResolveAgentAction(action.id, true) },
                        onReject = { onResolveAgentAction(action.id, false) }
                    )
                }
            }

            if (messages.size <= 1 && !isBusy) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quickPrompts.forEachIndexed { idx, (icon, prompt) ->
                            val cardLabel = when (idx) {
                                1 -> "Créer une tâche."
                                else -> prompt
                            }
                            Surface(
                                onClick = { if (!isBusy) onSendMessage(prompt, prompt) },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                                shadowElevation = 1.dp,
                                modifier = Modifier.testTag("agent_suggestion_card_$idx")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = gradient.accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = cardLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isBusy) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = gradient.accentColor,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = if (pendingAction != null) "En attente de votre validation avant l'action..." else "Light Agent réfléchit...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (attachedDocuments.isNotEmpty() || isExtractingDocuments || extractionError != null) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                attachedDocuments.forEachIndexed { index, document ->
                    Surface(shape = RoundedCornerShape(16.dp), color = gradient.softContainer) {
                        Row(
                            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${document.displayName} · local", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                            IconButton(onClick = { attachedDocuments.removeAt(index) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Rounded.Close, contentDescription = "Retirer ${document.displayName}", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                if (isExtractingDocuments) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            }
            extractionError?.let { Text(it, color = AppleRose, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp)) }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Floating iOS-style composer
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.9.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { documentPicker.launch(SUPPORTED_DOCUMENT_MIME_TYPES) },
                    enabled = !isBusy && !isExtractingDocuments,
                    modifier = Modifier.testTag("agent_attach_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Joindre des fichiers", tint = gradient.accentColor)
                }
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Demandez une action à Light Agent...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    supportingText = { Text("Joindre MD, PDF, DOCX, XLSX ou PNG · lecture locale") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("agent_chat_input")
                )

                Spacer(modifier = Modifier.width(6.dp))

                val canSend = (inputText.isNotBlank() || attachedDocuments.isNotEmpty()) && !isBusy && !isExtractingDocuments
                Surface(
                    onClick = {
                        if (canSend) {
                            if (attachedDocuments.isNotEmpty()) {
                                showDocumentConsent = true
                            } else {
                                val toSend = inputText.trim()
                                inputText = ""
                                onSendMessage(toSend, toSend)
                            }
                        }
                    },
                    shape = CircleShape,
                    color = if (canSend) gradient.accentColor else gradient.softContainer,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("agent_send_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Envoyer à l'agent",
                            tint = if (canSend) Color.White else gradient.accentColor.copy(alpha = 0.55f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDocumentConsent) {
        val providerName = AgentProvider.fromId(settings.agentProvider).displayName
        val visibleSummary = inputText.trim().ifBlank { "Analyse les documents joints" } + " · " + attachedDocuments.joinToString { it.displayName }
        AlertDialog(
            onDismissRequest = { showDocumentConsent = false },
            title = { Text("Envoyer le texte extrait ?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Les fichiers sont lus localement sur cet appareil. Pour répondre, leur texte extrait et votre demande seront transmis à $providerName (${settings.agentModel}) via son API.")
                    Text("${attachedDocuments.size} fichier(s) · au plus ${MAX_DOCUMENT_PROMPT_CHARS} caractères transmis. Ne joignez pas d'informations sensibles que vous ne souhaitez pas partager.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    attachedDocuments.forEach { Text("• ${it.displayName} (${it.formatLabel})", style = MaterialTheme.typography.labelMedium) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val agentPrompt = buildDocumentPrompt(inputText.trim(), attachedDocuments.toList())
                    onSendMessage(agentPrompt, visibleSummary)
                    inputText = ""
                    attachedDocuments.clear()
                    extractionError = null
                    showDocumentConsent = false
                }) { Text("Envoyer") }
            },
            dismissButton = { TextButton(onClick = { showDocumentConsent = false }) { Text("Annuler") } }
        )
    }

    if (showUsageHistory) {
        AgentUsageHistoryDialog(records = agentUsage, onClear = onClearAgentUsage, onDismiss = { showUsageHistory = false })
    }

    if (showAgentSettings) {
        AgentSettingsDialog(
            settings = settings,
            apiKeyConfigured = apiKeyConfigured,
            models = agentModels,
            modelsLoading = agentModelsLoading,
            modelsError = agentModelsError,
            onLoadModels = onLoadAgentModels,
            onDismiss = { showAgentSettings = false },
            onSave = { provider, baseUrl, model, reflection, capabilities, skills, apiKey, clearApiKey ->
                onSaveAgentSettings(provider, baseUrl, model, reflection, capabilities, skills, apiKey, clearApiKey)
                showAgentSettings = false
            }
        )
    }
}

@Composable
private fun AgentPlanPreviewCard(plan: AgentPlanPreview) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("agent_plan_preview"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(plan.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (plan.summary.isNotBlank()) {
                Text(plan.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            plan.items.forEachIndexed { index, item ->
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text("${index + 1}. ${item.title}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        if (item.detail.isNotBlank()) Text(item.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val metadata = buildList {
                            if (item.dateIso.isNotBlank()) add(item.dateIso)
                            if (item.estimatedMinutes > 0) add("${item.estimatedMinutes} min")
                            add("Priorité ${item.priority}")
                        }.joinToString(" · ")
                        Text(metadata, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Text(
                "Aperçu uniquement : aucune tâche n'est créée tant que vous ne l'autorisez pas dans une carte d'action.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AgentActionApprovalCard(
    action: AgentActionApproval,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val container = if (action.destructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
    val foreground = if (action.destructive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("agent_action_approval_${action.id}"),
        shape = RoundedCornerShape(20.dp),
        color = container,
        border = BorderStroke(1.dp, foreground.copy(alpha = 0.2f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("À valider", style = MaterialTheme.typography.labelMedium, color = foreground, fontWeight = FontWeight.SemiBold)
            Text(action.title, style = MaterialTheme.typography.titleMedium, color = foreground, fontWeight = FontWeight.Bold)
            action.details.forEach { (label, value) ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("$label :", style = MaterialTheme.typography.labelMedium, color = foreground, fontWeight = FontWeight.SemiBold)
                    Text(value, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = foreground)
                }
            }
            Text(
                if (action.destructive) "Cette action est définitive. Rien ne sera supprimé sans votre accord." else "Light attend votre accord; aucune modification n'est encore effectuée.",
                style = MaterialTheme.typography.bodySmall,
                color = foreground
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) { Text("Refuser") }
                Button(onClick = onApprove, modifier = Modifier.weight(1f)) { Text("Autoriser") }
            }
        }
    }
}

@Composable
private fun AgentMessageBubble(message: AgentChatMessage) {
    val gradient = LocalLightGradient.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (message.isUser) 20.dp else 6.dp,
                bottomEnd = if (message.isUser) 6.dp else 20.dp
            ),
            color = when {
                message.isError -> AppleRose.copy(alpha = 0.12f)
                message.isUser -> gradient.accentColor
                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
            },
            border = if (message.isUser) null else BorderStroke(
                0.8.dp,
                if (message.isError) AppleRose.copy(alpha = 0.4f) else Color.White
            ),
            shadowElevation = if (message.isUser) 2.dp else 1.dp,
            modifier = Modifier.fillMaxWidth(0.86f)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (message.actions.isNotEmpty()) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        message.actions.forEach { action ->
                            val actionColor = if (action.succeeded) AppleEmerald else AppleRose
                            Surface(
                                shape = CircleShape,
                                color = actionColor.copy(alpha = 0.14f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (action.succeeded) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
                                        contentDescription = null,
                                        tint = actionColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = action.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = actionColor
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        message.isError -> AppleRose
                        message.isUser -> Color.White
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                message.planPreview?.let { plan -> AgentPlanPreviewCard(plan) }
            }
        }
    }
}
