package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.preferences.AgentConversationThread
import com.example.ui.theme.LocalLightGradient
import com.example.ui.theme.appBrush

@Composable
fun AgentThreadsDrawerSheet(
    threads: List<AgentConversationThread>,
    activeThreadId: String,
    onSelectThread: (String) -> Unit,
    onNewThread: () -> Unit,
    onDeleteThread: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .width(304.dp)
            .testTag("agent_threads_drawer_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(gradient.appBrush()),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Discussions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Fermer le panneau")
                }
            }

            Button(
                onClick = {
                    onNewThread()
                    onCloseDrawer()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = gradient.accentColor,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("agent_new_thread_button")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nouvelle discussion", fontWeight = FontWeight.SemiBold)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (threads.isEmpty()) {
                Text(
                    text = "Aucune discussion enregistrée.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(threads, key = { it.id }) { thread ->
                        val selected = thread.id == activeThreadId
                        val messageCount = thread.messages.count { it.isUser }
                        val preview = thread.messages
                            .lastOrNull { it.text.isNotBlank() && it.text != "…" }
                            ?.text
                            .orEmpty()
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) gradient.softContainer else Color.Transparent,
                            border = if (selected) BorderStroke(1.dp, gradient.accentColor.copy(alpha = 0.35f)) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    onSelectThread(thread.id)
                                    onCloseDrawer()
                                }
                                .testTag("agent_thread_item_${thread.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = if (selected) gradient.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = thread.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) gradient.accentColor else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (preview.isNotBlank()) {
                                        Text(
                                            text = preview.replace('\n', ' '),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = if (messageCount > 0) "$messageCount message(s)" else "Nouveau fil",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteThread(thread.id) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("agent_delete_thread_${thread.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Supprimer ce fil",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
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
fun AgentChatTopBar(
    activeThreadTitle: String,
    onOpenDrawer: () -> Unit,
    onClearConversation: () -> Unit,
    onOpenAgentConfig: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    TopAppBar(
        modifier = modifier.testTag("agent_top_app_bar"),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        navigationIcon = {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("agent_threads_drawer_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = "Fils de discussion",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(gradient.appBrush()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "Light Agent IA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = activeThreadTitle.ifBlank { "Nouvelle discussion" },
                        style = MaterialTheme.typography.labelSmall,
                        color = gradient.accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("agent_top_bar_thread_title")
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = onClearConversation,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("agent_clear_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Réinitialiser la conversation",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onOpenAgentConfig,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("agent_configuration_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = "Configurer l'agent",
                    tint = gradient.accentColor
                )
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("agent_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Paramètres",
                    tint = gradient.accentColor
                )
            }
        }
    )
}
