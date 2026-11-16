package com.example.ui.screens

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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.AgentChatMessage
import com.example.ui.theme.AppleCardSurface
import com.example.ui.theme.AppleDivider
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleRose
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.LocalElanGradient

@Composable
fun AgentChatScreen(
    messages: List<AgentChatMessage>,
    isBusy: Boolean,
    onSendMessage: (String) -> Unit,
    onClearConversation: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalElanGradient.current
    var inputText by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickPrompts = listOf(
        Icons.Rounded.Search to "Quelles sont les tâches à accomplir",
        Icons.Rounded.Add to "Créer une tâche : réviser Architecture Logicielle demain à 14h",
        Icons.Rounded.Description to "En résumé, aujourd'hui...",
        Icons.Rounded.Schedule to "Planification des activités pour aujourd'hui"
    )

    LaunchedEffect(messages.size, isBusy) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
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
                        .background(gradient.linearBrush()),
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
                        text = "Élan Agent IA",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AppleTextPrimary
                    )
                    Text(
                        text = "Discutez & opérez directement votre planning",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    onClick = onClearConversation,
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(0.8.dp, AppleDivider),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("agent_clear_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Réinitialiser la conversation",
                            tint = AppleTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Surface(
                    onClick = onOpenSettings,
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(0.8.dp, AppleDivider),
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
                        if (!isBusy) onSendMessage(prompt)
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
                                onClick = { if (!isBusy) onSendMessage(prompt) },
                                shape = CircleShape,
                                color = AppleCardSurface.copy(alpha = 0.92f),
                                border = BorderStroke(0.8.dp, Color.White),
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
                                        color = AppleTextPrimary
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
                        color = AppleCardSurface.copy(alpha = 0.92f),
                        border = BorderStroke(0.8.dp, AppleDivider),
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
                                text = "Élan Agent réfléchit et exécute...",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppleTextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Floating iOS-style composer
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = AppleCardSurface,
            border = BorderStroke(0.9.dp, Color.White),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Demandez une action à Élan Agent...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppleTextSecondary
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
                        .weight(1f)
                        .testTag("agent_chat_input")
                )

                Spacer(modifier = Modifier.width(6.dp))

                val canSend = inputText.isNotBlank() && !isBusy
                Surface(
                    onClick = {
                        if (canSend) {
                            val toSend = inputText.trim()
                            inputText = ""
                            onSendMessage(toSend)
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
}

@Composable
private fun AgentMessageBubble(message: AgentChatMessage) {
    val gradient = LocalElanGradient.current
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
                else -> AppleCardSurface.copy(alpha = 0.94f)
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
                            Surface(
                                shape = CircleShape,
                                color = AppleEmerald.copy(alpha = 0.14f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = AppleEmerald,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = action.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppleEmerald
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
                        else -> AppleTextPrimary
                    }
                )
            }
        }
    }
}
