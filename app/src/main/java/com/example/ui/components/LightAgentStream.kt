package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.document.isAgentMarkdownDocument
import com.example.data.repository.AgentActionApproval
import com.example.data.repository.AgentActionLog
import com.example.data.repository.AgentChatMessage
import com.example.data.repository.AgentPlanPreview
import com.example.data.repository.AgentTurnPhase
import com.example.data.repository.AgentTurnProgress
import io.getstream.chat.android.ai.compose.conversation.AIAgentActivity
import io.getstream.chat.android.ai.compose.conversation.AIConversationFeed
import io.getstream.chat.android.ai.compose.conversation.AIConversationRequest
import io.getstream.chat.android.ai.compose.conversation.AIConversationTransport
import io.getstream.chat.android.ai.compose.conversation.AIMessage
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.parts.AIMessagePart
import io.getstream.chat.android.ai.compose.ui.component.AIConversationAssistantMessageParams
import io.getstream.chat.android.ai.compose.ui.component.AIMessageAttachmentItem
import io.getstream.chat.android.ai.compose.ui.component.AIMessageParts
import io.getstream.chat.android.ai.compose.ui.component.AIMessagePartItem
import io.getstream.chat.android.ai.compose.ui.component.ChatAiComponentFactory
import io.getstream.chat.android.ai.compose.ui.component.StreamingReasoning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/** User id and install id used for tool approvals rendered inside the conversation. */
internal const val LIGHT_USER_ID = "light-local"
internal const val LIGHT_INSTALL_ID = "light-install"

/**
 * Light's message as an AIMessage for Stream AI Compose. Light keeps the source of truth in
 * its own [AgentChatMessage] list; this is a read-only projection for the conversation UI.
 */
internal class LightAgentMessage(
    override val id: String,
    override val role: AIMessageRole,
    override val text: String,
    override val isGenerating: Boolean,
    override val parts: List<AIMessagePart>,
    val isError: Boolean,
    val planPreview: AgentPlanPreview?,
    val elapsedMillis: Long?,
) : AIMessage

private fun logPart(messageId: String, index: Int, log: AgentActionLog): AIMessagePart =
    AIMessagePart.parts(
        listOf(
            "ai_tool_call" to mapOf(
                "id" to "$messageId-tool-$index",
                "v" to 1,
                "name" to log.toolName,
                "display_title" to log.label,
                "status" to if (log.succeeded) "completed" else "failed",
                "executor" to "client",
                "target_user_id" to LIGHT_USER_ID,
                "duration_ms" to log.elapsedMillis?.toInt(),
            )
        )
    ).single()

private fun approvalPart(approval: AgentActionApproval): AIMessagePart =
    AIMessagePart.parts(
        listOf(
            "ai_tool_call" to mapOf(
                "id" to approval.id,
                "v" to 1,
                "name" to approval.toolName,
                "display_title" to approval.title,
                "status" to "awaiting_approval",
                "executor" to "client",
                "target_user_id" to LIGHT_USER_ID,
                "arguments" to approval.details.toMap(),
                "approval" to mapOf(
                    "title" to approval.title,
                    "message" to approval.details.joinToString("\n") { "${it.first} : ${it.second}" },
                    "allow_title" to "Autoriser",
                    "decline_title" to "Refuser",
                ),
            )
        )
    ).single()

private fun mapActivity(progress: AgentTurnProgress?): AIAgentActivity? {
    if (progress == null) return null
    return when (progress.activity.phase) {
        AgentTurnPhase.REQUESTING -> AIAgentActivity(AIAgentActivity.State.Thinking)
        AgentTurnPhase.STREAMING -> AIAgentActivity(AIAgentActivity.State.Generating)
        AgentTurnPhase.CHECKING_TOOL,
        AgentTurnPhase.RUNNING_TOOL -> AIAgentActivity(
            AIAgentActivity.State.CheckingExternalSources,
            text = progress.activity.toolName ?: "outil",
        )
        AgentTurnPhase.RESUMING -> AIAgentActivity(AIAgentActivity.State.Thinking)
        AgentTurnPhase.AWAITING_APPROVAL -> AIAgentActivity(
            AIAgentActivity.State.CheckingExternalSources,
            text = "En attente de votre accord",
        )
    }
}

private fun mapMessages(
    messages: List<AgentChatMessage>,
    approval: AgentActionApproval?,
    progress: AgentTurnProgress?,
): List<LightAgentMessage> {
    val liveId = progress?.messageId
    val lastAssistantId = messages.lastOrNull { !it.isUser }?.id
    // The pending confirmation is attached to the live reply, or, if the turn is between tool
    // rounds, to the latest assistant message.
    val approvalTarget = liveId ?: lastAssistantId
    return messages.map { message ->
        val isLive = progress != null && progress.messageId == message.id && !message.isUser
        val parts = buildList {
            if (message.isUser) return@buildList
            message.actions.forEachIndexed { index, log -> add(logPart(message.id, index, log)) }
            if (isLive && progress != null) {
                val already = message.actions.size
                progress.tools.drop(already).forEachIndexed { index, log ->
                    add(logPart(message.id, already + index, log))
                }
            }
            if (approval != null && message.id == approvalTarget) add(approvalPart(approval))
        }
        LightAgentMessage(
            id = message.id,
            role = if (message.isUser) AIMessageRole.User else AIMessageRole.Assistant,
            text = message.text.takeUnless { it == "…" }.orEmpty(),
            isGenerating = isLive,
            parts = parts,
            isError = message.isError,
            planPreview = message.planPreview,
            elapsedMillis = message.elapsedMillis,
        )
    }
}

/** Immutable snapshot of everything Light Agent exposes to the conversation UI. */
internal data class LightAgentSnapshot(
    val messages: List<AgentChatMessage>,
    val approval: AgentActionApproval?,
    val progress: AgentTurnProgress?,
)

/**
 * Bridges Stream AI Compose's conversation state to Light's agent. The screen pushes snapshots
 * of Light's message list (streaming tokens, tool calls, pending approval) into [snapshots];
 * the transport forwards them to the conversation feed.
 */
internal class LightAgentTransport(
    private val snapshots: MutableStateFlow<LightAgentSnapshot>,
    private val sendFn: (prompt: String, visible: String, skillKey: String?) -> Unit,
    private val busyProvider: () -> Boolean,
    private val stopFn: () -> Unit,
    private val skillProvider: () -> String?,
) : AIConversationTransport<LightAgentMessage> {

    /**
     * Set by the screen just before `AIConversationState.send` when the visible message differs
     * from the prompt sent to the model (attached documents).
     */
    @Volatile
    var documentPrompt: Pair<String, String>? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var connectJob: Job? = null

    override fun connect(feed: AIConversationFeed<LightAgentMessage>) {
        connectJob?.cancel()
        connectJob = scope.launch {
            snapshots.collect { snapshot ->
                if (!feed.isConnected) {
                    connectJob?.cancel()
                    return@collect
                }
                feed.update(messages = mapMessages(snapshot.messages, snapshot.approval, snapshot.progress))
                feed.update(activity = mapActivity(snapshot.progress))
            }
        }
    }

    override suspend fun send(request: AIConversationRequest<LightAgentMessage>) {
        if (busyProvider()) {
            throw IllegalStateException("Light Agent termine déjà une réponse.")
        }
        val pending = documentPrompt
        documentPrompt = null
        val prompt = pending?.first ?: request.text
        val visible = pending?.second ?: request.text
        sendFn(prompt, visible, skillProvider())
    }

    override suspend fun stop() {
        stopFn()
    }
}

/** Assistant reply row: steps first (tool calls, approvals), then Light's markdown answer. */
internal class LightAgentComponentFactory(
    private val onOpenDocument: (messageId: String) -> Unit,
    private val onSaveToLibrary: ((LightAgentMessage) -> Unit)?,
) : ChatAiComponentFactory {

    @Composable
    override fun AIConversationAssistantMessage(params: AIConversationAssistantMessageParams) {
        val light = params.message as? LightAgentMessage
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (params.parts.isNotEmpty()) {
                AIMessageParts(parts = params.parts) { part ->
                    val reasoning = part.reasoning
                    if (reasoning != null) {
                        StreamingReasoning(part = reasoning, text = params.reasoningText(reasoning))
                    } else {
                        AIMessagePartItem(part = part, approver = params.approver)
                    }
                }
            }
            if (light?.isError == true) {
                Text(
                    text = params.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("agent_message_content"),
                )
            } else if (params.text.isNotEmpty()) {
                if (isAgentMarkdownDocument(params.text)) {
                    AgentMessageDocumentCard(
                        text = params.text,
                        streaming = params.isGenerating,
                        onOpen = { light?.let { onOpenDocument(it.id) } },
                    )
                } else {
                    AgentMarkdown(params.text, Modifier.testTag("agent_message_content"))
                }
            }
            light?.planPreview?.let { plan -> AgentPlanPreviewCard(plan) }
            light?.elapsedMillis?.takeIf { !params.isGenerating && it > 0 }?.let { millis ->
                Text(
                    text = "Répondu en ${millis / 1000}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (onSaveToLibrary != null && light != null && params.text.length > 20 && !params.isGenerating) {
                TextButton(
                    onClick = { onSaveToLibrary.invoke(light) },
                    modifier = Modifier.testTag("save_to_library_${light.id}"),
                ) {
                    Icon(Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                    androidx.compose.foundation.layout.Spacer(Modifier.size(6.dp))
                    Text("Enregistrer dans la bibliothèque", style = MaterialTheme.typography.labelSmall)
                }
            }
            if (params.attachments.isNotEmpty()) {
                params.attachments.forEach { attachment ->
                    AIMessageAttachmentItem(attachment = attachment)
                }
            }
        }
    }
}

@Composable
internal fun AgentPlanPreviewCard(plan: AgentPlanPreview) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("agent_plan_preview"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(plan.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (plan.summary.isNotBlank()) {
                Text(
                    plan.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            plan.items.forEachIndexed { index, item ->
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            "${index + 1}. ${item.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (item.detail.isNotBlank()) {
                            Text(
                                item.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
