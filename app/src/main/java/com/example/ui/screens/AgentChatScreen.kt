package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.document.ExtractedAgentDocument
import com.example.data.document.LocalDocumentExtractor
import com.example.data.model.AgentModelOption
import com.example.data.model.AgentProvider
import com.example.data.model.AgentReflection
import com.example.data.model.AgentSkill
import com.example.data.model.AgentSkillCatalog
import com.example.data.model.AgentUsageRecord
import com.example.data.model.UiState
import com.example.data.preferences.AgentConversationThread
import com.example.data.preferences.StudyAppSettings
import com.example.data.repository.AgentActionApproval
import com.example.data.repository.AgentTurnProgress
import com.example.data.repository.AgentChatMessage
import com.example.ui.components.AgentMessageDocumentReader
import com.example.ui.components.AgentSkillPicker
import com.example.ui.components.AgentThreadsDrawerSheet
import com.example.ui.components.LIGHT_INSTALL_ID
import com.example.ui.components.LIGHT_USER_ID
import com.example.ui.components.LightAgentComponentFactory
import com.example.ui.components.LightAgentSnapshot
import com.example.ui.components.LightAgentTransport
import io.getstream.chat.android.ai.compose.conversation.rememberAIConversationState
import io.getstream.chat.android.ai.compose.ui.component.AIConversation
import io.getstream.chat.android.ai.compose.ui.component.AIToolApprover
import io.getstream.chat.android.ai.compose.ui.component.LocalChatAiComponentFactory
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.ui.theme.AppleRose
import com.example.ui.theme.LocalLightGradient
import com.example.ui.theme.appBrush
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
    threads: List<AgentConversationThread> = emptyList(),
    activeThreadId: String = "",
    isBusy: Boolean,
    pendingAction: AgentActionApproval?,
    onResolveAgentAction: (actionId: String, approved: Boolean) -> Unit,
    onSendMessage: (agentPrompt: String, visibleMessage: String, skillKey: String?) -> Unit,
    onClearConversation: () -> Unit,
    onSelectThread: (String) -> Unit = {},
    onNewThread: () -> Unit = onClearConversation,
    onDeleteThread: (String) -> Unit = {},
    onOpenSettings: () -> Unit,
    onStopAgentTurn: () -> Unit = {},
    settings: StudyAppSettings,
    apiKeyConfigured: Boolean,
    agentModels: List<AgentModelOption>,
    agentModelsLoading: Boolean,
    agentModelsError: String?,
    agentUsage: List<AgentUsageRecord>,
    onClearAgentUsage: () -> Unit,
    onLoadAgentModels: (String, String, String?) -> Unit,
    onSaveAgentSettings: (String, String, String, String, Set<String>, List<AgentSkill>, String?, Boolean) -> Unit,
    showEmbeddedNavigationChrome: Boolean = true,
    externalConfigTrigger: Int = 0,
    liveProgress: AgentTurnProgress? = null,
    modifier: Modifier = Modifier,
    libraryFoldersState: UiState<List<com.example.data.model.LibraryFolder>> = UiState.Success(emptyList()),
    libraryItemsState: UiState<List<com.example.data.model.LibraryItem>> = UiState.Success(emptyList()),
    onSaveToLibrary: ((folderId: String, title: String, category: String, source: String, mimeType: String, textContent: String, fileName: String) -> Unit)? = null,
    searchResults: List<com.example.data.repository.SearchResult> = emptyList(),
    searchLoading: Boolean = false,
    searchError: String? = null,
    onSearch: (String) -> Unit = {},
    onClearSearch: () -> Unit = {},
    imageGenerating: Boolean = false,
    imageError: String? = null,
    onGenerateImage: (String, String) -> Unit = { _, _ -> },
    onClearImageError: () -> Unit = {}
) {
    val gradient = LocalLightGradient.current
    var inputText by rememberSaveable(activeThreadId) { mutableStateOf("") }
    var showAgentSettings by rememberSaveable { mutableStateOf(false) }
    var showUsageHistory by rememberSaveable { mutableStateOf(false) }
    var showAttachmentTray by remember(activeThreadId) { mutableStateOf(false) }
    LaunchedEffect(activeThreadId) { showAttachmentTray = false }
    var openDocumentId by rememberSaveable(activeThreadId) { mutableStateOf<String?>(null) }
    var showSkills by rememberSaveable(activeThreadId) { mutableStateOf(false) }
    var selectedSkillKey by rememberSaveable(activeThreadId) { mutableStateOf<String?>(null) }
    val skills = remember(settings.agentSkills) { AgentSkillCatalog.available(settings.agentSkills) }
    val selectedSkill = skills.firstOrNull { it.key == selectedSkillKey }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    LaunchedEffect(externalConfigTrigger) {
        if (externalConfigTrigger > 0) showAgentSettings = true
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val documentExtractor = remember(context) { LocalDocumentExtractor(context.applicationContext) }
    val attachedDocuments = remember(activeThreadId) { mutableStateListOf<ExtractedAgentDocument>() }
    var isExtractingDocuments by remember(activeThreadId) { mutableStateOf(false) }
    var extractionError by remember(activeThreadId) { mutableStateOf<String?>(null) }
    var showDocumentConsent by remember(activeThreadId) { mutableStateOf(false) }
    var showLibraryPicker by remember(activeThreadId) { mutableStateOf(false) }
    var showSearchSheet by remember(activeThreadId) { mutableStateOf(false) }
    var showImageSheet by remember(activeThreadId) { mutableStateOf(false) }
    var searchQuery by remember(activeThreadId) { mutableStateOf("") }
    var imagePrompt by remember(activeThreadId) { mutableStateOf("") }
    var imageFolderId by remember { mutableStateOf("") }
    var saveLibraryTarget by remember { mutableStateOf<com.example.ui.components.LightAgentMessage?>(null) }
    val libraryFolders = (libraryFoldersState as? UiState.Success)?.data.orEmpty()
    val draftToken = remember(activeThreadId) { Any() }
    var documentPickerOwner by remember { mutableStateOf<Any?>(null) }
    val documentPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        val owner = documentPickerOwner
        documentPickerOwner = null
        if (owner !== draftToken) return@rememberLauncherForActivityResult
        showAttachmentTray = false
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

    val agentSnapshots = remember { MutableStateFlow(LightAgentSnapshot(emptyList(), null, null)) }
    LaunchedEffect(messages, pendingAction, liveProgress) {
        agentSnapshots.value = LightAgentSnapshot(messages, pendingAction, liveProgress)
    }
    val busyRef = rememberUpdatedState(isBusy)
    val sendRef = rememberUpdatedState(onSendMessage)
    val stopRef = rememberUpdatedState(onStopAgentTurn)
    val skillRef = rememberUpdatedState(selectedSkillKey)
    val transport = remember {
        LightAgentTransport(
            snapshots = agentSnapshots,
            sendFn = { prompt, visible, skill -> sendRef.value(prompt, visible, skill) },
            busyProvider = { busyRef.value },
            stopFn = { stopRef.value() },
            skillProvider = { skillRef.value },
        )
    }
    val approver = remember(onResolveAgentAction, scope) {
        AIToolApprover(LIGHT_USER_ID, LIGHT_INSTALL_ID, scope) { call, allowed ->
            onResolveAgentAction(call.id, allowed)
        }
    }
    val conversationState = rememberAIConversationState(transport, approver)
    val saveToLibraryEnabled = onSaveToLibrary != null
    val lightFactory = remember(saveToLibraryEnabled) {
        LightAgentComponentFactory(
            onOpenDocument = { openDocumentId = it },
            onSaveToLibrary = if (saveToLibraryEnabled) { message -> saveLibraryTarget = message } else null,
        )
    }

    val quickPrompts = listOf(
        Icons.Rounded.Search to "Quelles sont les tâches à accomplir",
        Icons.Rounded.Add to "Créer une tâche : réviser Architecture Logicielle demain à 14h",
        Icons.Rounded.Description to "En résumé, aujourd'hui...",
        Icons.Rounded.Schedule to "Planification des activités pour aujourd'hui"
    )

    val chatContent: @Composable () -> Unit = {
        CompositionLocalProvider(LocalChatAiComponentFactory provides lightFactory) {
        Column(
            modifier = (if (showEmbeddedNavigationChrome) Modifier else modifier)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = if (showEmbeddedNavigationChrome) 10.dp else 4.dp, bottom = 14.dp)
        ) {
            if (showEmbeddedNavigationChrome) {
                // Header with top-left drawer button for standalone usage/previews
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
                        Surface(
                            onClick = { scope.launch { drawerState.open() } },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("agent_threads_drawer_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Menu,
                                    contentDescription = "Fils de discussion",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(gradient.appBrush()),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Light Agent IA",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Discutez & opérez votre planning",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                                .size(44.dp)
                                .testTag("agent_clear_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Réinitialiser la conversation",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                        Surface(
                            onClick = { showAgentSettings = true },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.size(44.dp).testTag("agent_configuration_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Tune, contentDescription = "Configurer l’agent", tint = gradient.accentColor, modifier = Modifier.size(19.dp))
                            }
                        }
                        Surface(
                            onClick = onOpenSettings,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("agent_settings_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = "Paramètres",
                                    tint = gradient.accentColor,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

        Row(modifier = Modifier.fillMaxWidth()) {
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

        // Messages (Stream AI Compose)
        AIConversation(
                state = conversationState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                footer = {
                    val error by conversationState.error.collectAsState()
                    error?.let { failure ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                failure.message ?: "La réponse a échoué. Vérifiez votre connexion ou la configuration du fournisseur.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            TextButton(
                                onClick = { conversationState.retry() },
                                modifier = Modifier.testTag("agent_retry_button")
                            ) {
                                Text("Réessayer", color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                },
                emptyContent = {
                    Text(
                        "Light Agent vous attend…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

        if (messages.size <= 1 && !isBusy) {
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
                        onClick = { if (!isBusy) inputText = prompt },
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

        selectedSkill?.let { skill ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Compétence : ${skill.title}", style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f).testTag("agent_selected_skill"), maxLines = 2, overflow = TextOverflow.Ellipsis)
                IconButton(onClick = { selectedSkillKey = null }, modifier = Modifier.testTag("agent_remove_skill")) {
                    Icon(Icons.Rounded.Close, contentDescription = "Retirer la compétence")
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

        if (showAttachmentTray) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = gradient.softContainer,
                border = BorderStroke(0.8.dp, gradient.accentColor.copy(alpha = 0.28f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .testTag("agent_upload_tray")
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AttachFile,
                            contentDescription = null,
                            tint = gradient.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Joindre un document",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "MD, PDF, DOCX, XLSX ou PNG · lecture locale",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = {
                                documentPickerOwner = draftToken
                                documentPicker.launch(SUPPORTED_DOCUMENT_MIME_TYPES)
                            },
                            enabled = !isBusy && !isExtractingDocuments,
                            modifier = Modifier.testTag("agent_pick_files_button")
                        ) {
                            Text("Parcourir", color = gradient.accentColor, fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(
                        onClick = { showAttachmentTray = false; showSkills = true },
                        enabled = !isBusy,
                        modifier = Modifier.padding(horizontal = 10.dp).testTag("agent_pick_skill_button"),
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Choisir une compétence")
                    }
                    TextButton(
                        onClick = { showAttachmentTray = false; showLibraryPicker = true },
                        enabled = !isBusy,
                        modifier = Modifier.padding(horizontal = 10.dp).testTag("agent_pick_library_button"),
                    ) {
                        Icon(Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Choisir dans la bibliothèque")
                    }
                    TextButton(
                        onClick = { showAttachmentTray = false; showSearchSheet = true },
                        enabled = !isBusy,
                        modifier = Modifier.padding(horizontal = 10.dp).testTag("agent_search_button"),
                    ) {
                        Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Rechercher sur le web")
                    }
                    TextButton(
                        onClick = { showAttachmentTray = false; showImageSheet = true },
                        enabled = !isBusy,
                        modifier = Modifier.padding(horizontal = 10.dp).testTag("agent_image_button"),
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Générer une image")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

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
                    onClick = { showAttachmentTray = !showAttachmentTray },
                    enabled = !isBusy && !isExtractingDocuments,
                    modifier = Modifier.testTag("agent_attach_button")
                ) {
                    Icon(
                        imageVector = if (showAttachmentTray) Icons.Rounded.Close else Icons.Rounded.Add,
                        contentDescription = if (showAttachmentTray) "Fermer le menu du message" else "Options du message",
                        tint = gradient.accentColor
                    )
                }
                Column(Modifier.weight(1f)) {
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
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("agent_chat_input")
                    )
                    TextButton(onClick = { showAgentSettings = true }, modifier = Modifier.testTag("agent_active_model")) {
                        val model = settings.agentModel.ifBlank { "Choisir un modèle" }
                        Text("$model · ${AgentReflection.fromKey(settings.agentReflection).label}", style = MaterialTheme.typography.labelMedium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                val answering = isBusy
                val canSend = (inputText.isNotBlank() || attachedDocuments.isNotEmpty()) && !isBusy && !isExtractingDocuments
                Surface(
                    onClick = {
                        if (answering) {
                            conversationState.stop()
                            return@Surface
                        }
                        if (canSend) {
                            if (attachedDocuments.isNotEmpty()) {
                                showDocumentConsent = true
                            } else {
                                val toSend = inputText.trim()
                                inputText = ""
                                transport.documentPrompt = null
                                conversationState.send(toSend)
                                selectedSkillKey = null
                                showAttachmentTray = false
                            }
                        }
                    },
                    shape = CircleShape,
                    color = if (canSend || answering) gradient.accentColor else gradient.softContainer,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("agent_send_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (answering) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.Send,
                            contentDescription = if (answering) "Arrêter la réponse" else "Envoyer à l'agent",
                            tint = if (canSend || answering) Color.White else gradient.accentColor.copy(alpha = 0.55f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        }
        }
    }

    if (showEmbeddedNavigationChrome) {
        BackHandler(enabled = drawerState.isOpen) {
            scope.launch { drawerState.close() }
        }
        ModalNavigationDrawer(
            drawerState = drawerState,
            modifier = modifier.fillMaxSize(),
            drawerContent = {
                AgentThreadsDrawerSheet(
                    threads = threads,
                    activeThreadId = activeThreadId,
                    onSelectThread = onSelectThread,
                    onNewThread = onNewThread,
                    onDeleteThread = onDeleteThread,
                    onCloseDrawer = { scope.launch { drawerState.close() } },
                        drawerOpen = drawerState.isOpen
                )
            }
        ) {
            chatContent()
        }
    } else {
        chatContent()
    }

    messages.firstOrNull { it.id == openDocumentId }?.let { message ->
        key(activeThreadId, message.id) {
            AgentMessageDocumentReader(message, onClose = { openDocumentId = null })
        }
    }

    if (showSkills) {
        AgentSkillPicker(skills, selectedSkill?.key, onSelect = { key ->
            selectedSkillKey = key
            showSkills = false
        }, onDismiss = { showSkills = false })
    }

    if (showDocumentConsent) {
        val providerName = AgentProvider.fromId(settings.agentProvider).displayName
        val visibleSummary = inputText.trim().ifBlank { "Analyse les documents joints" } + " · " + attachedDocuments.joinToString { it.displayName }
        AlertDialog(
            onDismissRequest = { showDocumentConsent = false },
            title = { Text("Envoyer le texte extrait ?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Les fichiers sont lus localement sur cet appareil. Pour répondre, leur texte extrait, votre demande et la méthode éventuellement sélectionnée seront transmis à $providerName (${settings.agentModel}) via son API.")
                    Text("${attachedDocuments.size} fichier(s) · au plus ${MAX_DOCUMENT_PROMPT_CHARS} caractères transmis. Ne joignez pas d'informations sensibles que vous ne souhaitez pas partager.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    attachedDocuments.forEach { Text("• ${it.displayName} (${it.formatLabel})", style = MaterialTheme.typography.labelMedium) }
                }
            },
            confirmButton = {
                TextButton(enabled = !isBusy && attachedDocuments.isNotEmpty(), onClick = {
                    val agentPrompt = buildDocumentPrompt(inputText.trim(), attachedDocuments.toList())
                    transport.documentPrompt = agentPrompt to visibleSummary
                    conversationState.send(visibleSummary)
                    selectedSkillKey = null
                    showAttachmentTray = false
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

    if (showLibraryPicker) {
        com.example.ui.components.LibraryPickerSheet(
            foldersState = libraryFoldersState,
            itemsState = libraryItemsState,
            onPick = { item ->
                // Insère le texte borné de la bibliothèque comme contexte supplémentaire
                val header = "\n\n[Contexte bibliothèque: ${item.title} (${item.mimeType})]\n"
                val excerpt = item.textContent.take(6000)
                inputText = (inputText + header + excerpt).take(12000)
                showLibraryPicker = false
                showAttachmentTray = false
            },
            onDismiss = { showLibraryPicker = false }
        )
    }

    saveLibraryTarget?.let { target ->
        if (onSaveToLibrary != null) {
            com.example.ui.components.SaveToLibrarySheet(
                folders = libraryFolders,
                initialTitle = target.text.lineSequence().firstOrNull()?.take(60) ?: "Artefact agent",
                initialContent = target.text,
                onSave = { folderId, title, mime ->
                    onSaveToLibrary(folderId, title, "other", "agent", mime, target.text.take(800000), "")
                    saveLibraryTarget = null
                },
                onDismiss = { saveLibraryTarget = null }
            )
        }
    }

    if (showSearchSheet) {
        com.example.ui.components.SearchAgentSheet(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            results = searchResults,
            loading = searchLoading,
            error = searchError,
            enabled = settings.searchEnabled && settings.searchProvider != "none",
            providerName = settings.searchProvider,
            onSearch = { onSearch(searchQuery) },
            onPick = { result ->
                val header = "\n\n[Source: ${result.title} — ${result.url}]\n"
                inputText = (inputText + header + result.snippet.take(2000)).take(12000)
                showSearchSheet = false
            },
            onClear = onClearSearch,
            onDismiss = { showSearchSheet = false }
        )
    }

    if (showImageSheet) {
        com.example.ui.components.ImageAgentSheet(
            prompt = imagePrompt,
            onPromptChange = { imagePrompt = it },
            folderId = imageFolderId,
            onFolderChange = { imageFolderId = it },
            folders = libraryFolders,
            generating = imageGenerating,
            error = imageError,
            enabled = settings.imageEnabled && settings.imageProvider != "none",
            providerName = settings.imageProvider,
            onGenerate = { onGenerateImage(imagePrompt, imageFolderId) },
            onClearError = onClearImageError,
            onDismiss = { showImageSheet = false }
        )
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
