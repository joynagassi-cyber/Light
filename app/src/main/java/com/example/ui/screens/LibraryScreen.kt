package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DriveFolderUpload
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Html
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.TextSnippet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.LibraryFolder
import com.example.data.model.LibraryFolderKind
import com.example.data.model.LibraryItem
import com.example.data.model.LibraryItemCategory
import com.example.data.model.LibraryItemSource
import com.example.data.model.UiState
import com.example.ui.components.AgentMarkdown
import com.example.ui.components.AppleCard
import com.example.ui.theme.LocalLightGradient

@Composable
fun LibraryScreen(
    foldersState: UiState<List<LibraryFolder>>,
    itemsState: UiState<List<LibraryItem>>,
    onCreateFolder: (String, String) -> Unit,
    onRenameFolder: (String, String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onCreateItem: (String, String, String, String, String, String, String) -> Unit,
    onDeleteItem: (String) -> Unit,
    onMoveItem: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    var selectedFolderId by remember { mutableStateOf<String?>(null) }
    var showCreateFolder by remember { mutableStateOf(false) }
    var showCreateItem by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<LibraryItem?>(null) }
    var folderNameInput by remember { mutableStateOf("") }
    var folderKindInput by remember { mutableStateOf(LibraryFolderKind.MANUAL.key) }

    // Item creation form
    var itemTitle by remember { mutableStateOf("") }
    var itemCategory by remember { mutableStateOf(LibraryItemCategory.OTHER.key) }
    var itemSource by remember { mutableStateOf(LibraryItemSource.MANUAL.key) }
    var itemMime by remember { mutableStateOf("markdown") }
    var itemFolder by remember { mutableStateOf("") }
    var itemContent by remember { mutableStateOf("") }
    var itemFileName by remember { mutableStateOf("") }
    var folderRenameTarget by remember { mutableStateOf<LibraryFolder?>(null) }
    var renameText by remember { mutableStateOf("") }

    val folders = (foldersState as? UiState.Success)?.data.orEmpty()
    val items = (itemsState as? UiState.Success)?.data.orEmpty()
    val filteredItems = if (selectedFolderId == null) items else items.filter { it.folderId == selectedFolderId }

    val isLoading = foldersState is UiState.Loading || itemsState is UiState.Loading
    val errorMsg = (foldersState as? UiState.Error)?.message ?: (itemsState as? UiState.Error)?.message

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Bibliothèque",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("library_title")
        )
        Text(
            text = "Stockage isolé par compte. Chaque dossier et document n’est visible que par son propriétaire ; les écritures hors ligne sont mises en cache et synchronisées au retour réseau.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = gradient.accentColor, modifier = Modifier.testTag("library_loading"))
            }
        }
        if (errorMsg != null) {
            AppleCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
                Text(text = errorMsg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("library_error"))
            }
        }

        // Folder filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFolderId == null,
                    onClick = { selectedFolderId = null },
                    label = { Text("Tous (${items.size})") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = gradient.softContainer, selectedLabelColor = gradient.accentColor),
                    modifier = Modifier.testTag("library_filter_all")
                )
            }
            items(folders) { folder ->
                FilterChip(
                    selected = selectedFolderId == folder.id,
                    onClick = { selectedFolderId = folder.id },
                    label = { Text("${folder.name} (${items.count { it.folderId == folder.id }})") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (folder.kind == LibraryFolderKind.AGENT.key) Icons.Rounded.AutoAwesome else Icons.Rounded.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = gradient.softContainer, selectedLabelColor = gradient.accentColor),
                    modifier = Modifier.testTag("library_filter_${folder.id}")
                )
            }
        }

        // Folders management row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { showCreateFolder = true },
                modifier = Modifier.testTag("library_create_folder_button")
            ) {
                Icon(Icons.Rounded.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(6.dp))
                Text("Nouveau dossier")
            }
            if (folders.isNotEmpty()) {
                Text(
                    text = "${folders.size} dossiers",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (folders.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(folders) { folder ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.testTag("library_folder_card_${folder.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (folder.kind == LibraryFolderKind.AGENT.key) Icons.Rounded.AutoAwesome else Icons.Rounded.Folder,
                                contentDescription = null,
                                tint = gradient.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(text = folder.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Text(text = LibraryFolderKind.fromKey(folder.kind).label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { folderRenameTarget = folder; renameText = folder.name }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.Folder, contentDescription = "Renommer", modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onDeleteFolder(folder.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Supprimer dossier", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Items list + add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedFolderId == null) "Documents (${filteredItems.size})" else "Dans ce dossier (${filteredItems.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(
                onClick = { itemFolder = selectedFolderId ?: ""; showCreateItem = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = gradient.accentColor, contentColor = Color.White),
                modifier = Modifier.testTag("library_create_item_button")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(6.dp))
                Text("Ajouter")
            }
        }

        if (filteredItems.isEmpty()) {
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Aucun document dans cette vue. Créez un dossier puis ajoutez un document Markdown, PDF ou HTML. Le contenu est stocké comme texte borné (max 800 000 caractères) et affiché sans exécuter de code.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(filteredItems) { item ->
                    LibraryItemRow(
                        item = item,
                        folderName = folders.firstOrNull { it.id == item.folderId }?.name,
                        folders = folders,
                        onOpen = { selectedItem = item },
                        onDelete = { onDeleteItem(item.id) },
                        onMove = { targetId -> onMoveItem(item.id, targetId) }
                    )
                }
            }
        }
    }

    if (showCreateFolder) {
        AlertDialog(
            onDismissRequest = { showCreateFolder = false },
            title = { Text("Nouveau dossier") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = folderNameInput,
                        onValueChange = { folderNameInput = it },
                        label = { Text("Nom du dossier") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("library_folder_name_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = folderKindInput == LibraryFolderKind.MANUAL.key,
                            onClick = { folderKindInput = LibraryFolderKind.MANUAL.key },
                            label = { Text("Manuel") }
                        )
                        FilterChip(
                            selected = folderKindInput == LibraryFolderKind.AGENT.key,
                            onClick = { folderKindInput = LibraryFolderKind.AGENT.key },
                            label = { Text("Agent") }
                        )
                    }
                    Text(
                        text = "Les dossiers sont privés au compte. Le type « Agent » regroupe les artefacts générés par l’assistant.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (folderNameInput.isNotBlank()) {
                            onCreateFolder(folderNameInput.trim(), folderKindInput)
                            folderNameInput = ""
                            showCreateFolder = false
                        }
                    },
                    modifier = Modifier.testTag("library_folder_confirm")
                ) { Text("Créer") }
            },
            dismissButton = { TextButton(onClick = { showCreateFolder = false }) { Text("Annuler") } }
        )
    }

    if (folderRenameTarget != null) {
        AlertDialog(
            onDismissRequest = { folderRenameTarget = null },
            title = { Text("Renommer le dossier") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Nouveau nom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("library_folder_rename_input")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        folderRenameTarget?.let { onRenameFolder(it.id, renameText) }
                        folderRenameTarget = null
                    },
                    modifier = Modifier.testTag("library_folder_rename_confirm")
                ) { Text("Renommer") }
            },
            dismissButton = { TextButton(onClick = { folderRenameTarget = null }) { Text("Annuler") } }
        )
    }

    if (showCreateItem) {
        LibraryCreateItemDialog(
            title = itemTitle,
            onTitleChange = { itemTitle = it },
            category = itemCategory,
            onCategoryChange = { itemCategory = it },
            source = itemSource,
            onSourceChange = { itemSource = it },
            mimeType = itemMime,
            onMimeChange = { itemMime = it },
            folderId = itemFolder,
            onFolderChange = { itemFolder = it },
            textContent = itemContent,
            onContentChange = { itemContent = it },
            fileName = itemFileName,
            onFileNameChange = { itemFileName = it },
            folders = folders,
            onDismiss = { showCreateItem = false },
            onConfirm = {
                onCreateItem(itemFolder, itemTitle, itemCategory, itemSource, itemMime, itemContent, itemFileName)
                itemTitle = ""; itemContent = ""; itemFileName = ""
                showCreateItem = false
            }
        )
    }

    selectedItem?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedItem = null },
            title = { Text(item.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = {},
                            label = { Text(LibraryItemCategory.fromKey(item.category).label) },
                            leadingIcon = { Icon(Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(LibraryItemSource.fromKey(item.source).label) },
                            leadingIcon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(item.mimeType.uppercase()) },
                            leadingIcon = { Icon(mimeIcon(item.mimeType), contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                    if (item.fileName.isNotBlank()) {
                        Text(text = "Fichier d’origine : ${item.fileName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val folderLabel = folders.firstOrNull { it.id == item.folderId }?.name ?: "Sans dossier"
                    Text(text = "Dossier : $folderLabel", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LibraryBoundedReader(mimeType = item.mimeType, content = item.textContent)
                }
            },
            confirmButton = { TextButton(onClick = { selectedItem = null }, modifier = Modifier.testTag("library_reader_close")) { Text("Fermer") } },
            modifier = Modifier.widthIn(max = 640.dp)
        )
    }
}

@Composable
private fun LibraryItemRow(
    item: LibraryItem,
    folderName: String?,
    folders: List<LibraryFolder>,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onMove: (String) -> Unit
) {
    var showMove by remember { mutableStateOf(false) }
    AppleCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .testTag("library_item_${item.id}"),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(mimeIcon(item.mimeType), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        text = "${LibraryItemCategory.fromKey(item.category).label} · ${LibraryItemSource.fromKey(item.source).label} · ${item.mimeType} · ${folderName ?: "Sans dossier"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.fileName.isNotBlank()) {
                        Text(text = item.fileName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = { showMove = !showMove }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.Folder, contentDescription = "Déplacer", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
        if (showMove) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Déplacer vers :", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        AssistChip(onClick = { onMove(""); showMove = false }, label = { Text("Sans dossier") })
                    }
                    items(folders) { folder ->
                        AssistChip(
                            onClick = { onMove(folder.id); showMove = false },
                            label = { Text(folder.name) },
                            modifier = Modifier.testTag("library_move_${folder.id}")
                        )
                    }
                }
            }
        }
    }
}

private fun mimeIcon(mime: String): ImageVector = when (mime.lowercase()) {
    "pdf" -> Icons.Rounded.Description
    "html" -> Icons.Rounded.Code
    "markdown" -> Icons.Rounded.Description
    else -> Icons.Rounded.Description
}

@Composable
private fun LibraryBoundedReader(mimeType: String, content: String) {
    when (mimeType.lowercase()) {
        "markdown" -> AgentMarkdown(text = content)
        "html" -> {
            // Bounded: show escaped text, never load or execute
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Aperçu HTML borné — le code actif n’est pas exécuté. Copiez le texte si besoin.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        "pdf" -> {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Texte extrait du PDF — les images et scripts ne sont pas rendus.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        else -> Text(text = content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryCreateItemDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    source: String,
    onSourceChange: (String) -> Unit,
    mimeType: String,
    onMimeChange: (String) -> Unit,
    folderId: String,
    onFolderChange: (String) -> Unit,
    textContent: String,
    onContentChange: (String) -> Unit,
    fileName: String,
    onFileNameChange: (String) -> Unit,
    folders: List<LibraryFolder>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedMime by remember { mutableStateOf(false) }
    var expandedFolder by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un document") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Titre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("library_item_title_input")
                )
                OutlinedTextField(
                    value = fileName,
                    onValueChange = onFileNameChange,
                    label = { Text("Nom du fichier d’origine (optionnel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(expanded = expandedCategory, onExpandedChange = { expandedCategory = !expandedCategory }) {
                    OutlinedTextField(
                        value = LibraryItemCategory.fromKey(category).label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Catégorie") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }) {
                        LibraryItemCategory.entries.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.label) }, onClick = { onCategoryChange(cat.key); expandedCategory = false })
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = source == LibraryItemSource.MANUAL.key, onClick = { onSourceChange(LibraryItemSource.MANUAL.key) }, label = { Text("Manuel") })
                    FilterChip(selected = source == LibraryItemSource.AGENT.key, onClick = { onSourceChange(LibraryItemSource.AGENT.key) }, label = { Text("Agent") })
                }
                ExposedDropdownMenuBox(expanded = expandedMime, onExpandedChange = { expandedMime = !expandedMime }) {
                    OutlinedTextField(
                        value = mimeType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMime) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(expanded = expandedMime, onDismissRequest = { expandedMime = false }) {
                        listOf("markdown" to "Markdown", "pdf" to "PDF (texte extrait)", "html" to "HTML (borné)", "text" to "Texte").forEach { (key, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { onMimeChange(key); expandedMime = false })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = expandedFolder, onExpandedChange = { expandedFolder = !expandedFolder }) {
                    OutlinedTextField(
                        value = folders.firstOrNull { it.id == folderId }?.name ?: "Sans dossier",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dossier") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFolder) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(expanded = expandedFolder, onDismissRequest = { expandedFolder = false }) {
                        DropdownMenuItem(text = { Text("Sans dossier") }, onClick = { onFolderChange(""); expandedFolder = false })
                        folders.forEach { folder ->
                            DropdownMenuItem(text = { Text(folder.name) }, onClick = { onFolderChange(folder.id); expandedFolder = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = textContent,
                    onValueChange = onContentChange,
                    label = { Text("Contenu textuel (extrait, max 800 000 car.)") },
                    minLines = 4,
                    maxLines = 12,
                    modifier = Modifier.fillMaxWidth().height(140.dp).testTag("library_item_content_input")
                )
                Text(
                    text = "Le contenu est stocké tel quel dans Firestore, sans exécution. Pour un PDF/HTML, collez le texte extrait — le lecteur n’interprète jamais de script.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = title.isNotBlank() && textContent.isNotBlank(),
                modifier = Modifier.testTag("library_item_confirm")
            ) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
