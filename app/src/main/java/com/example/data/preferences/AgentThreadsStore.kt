package com.example.data.preferences

import com.example.data.repository.AgentActionLog
import com.example.data.repository.AgentChatMessage
import com.example.data.repository.AgentPlanPreview
import com.example.data.repository.AgentPlanPreviewItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.io.File
import java.util.UUID

data class AgentConversationThread(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Nouvelle discussion",
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val messages: List<AgentChatMessage> = emptyList()
)

class AgentThreadsStore(
    private val directory: File,
    userId: String
) {
    constructor(context: android.content.Context, userId: String) : this(context.filesDir, userId)

    private val lock = Any()
    private val safeUserId = userId.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "default" }
    private val storeFile = File(directory, "agent_threads_${safeUserId}.json")

    suspend fun loadThreads(): List<AgentConversationThread> = withContext(Dispatchers.IO) {
        synchronized(lock) {
            if (!storeFile.exists()) return@synchronized emptyList()
            val raw = runCatching { storeFile.readText(Charsets.UTF_8) }.getOrNull().orEmpty()
            if (raw.isBlank()) return@synchronized emptyList()
            decodeThreads(raw)
        }
    }

    suspend fun saveThreads(threads: List<AgentConversationThread>) = withContext(Dispatchers.IO) {
        synchronized(lock) {
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val trimmed = threads
                .sortedByDescending { it.updatedAtMillis }
                .take(40)
                .map { thread ->
                    thread.copy(messages = thread.messages.takeLast(120))
                }
            val encoded = encodeThreads(trimmed)
            val tmpFile = File(directory, "${storeFile.name}.tmp")
            tmpFile.writeText(encoded, Charsets.UTF_8)
            if (!tmpFile.renameTo(storeFile)) {
                storeFile.writeText(encoded, Charsets.UTF_8)
                tmpFile.delete()
            }
        }
    }

    private fun encodeThreads(threads: List<AgentConversationThread>): String = buildJsonArray {
        threads.forEach { thread ->
            add(
                buildJsonObject {
                    put("id", thread.id)
                    put("title", thread.title)
                    put("updatedAtMillis", thread.updatedAtMillis)
                    put("messages", buildJsonArray {
                        thread.messages.forEach { msg ->
                            add(
                                buildJsonObject {
                                    put("id", msg.id)
                                    put("isUser", msg.isUser)
                                    put("text", msg.text)
                                    put("isError", msg.isError)
                                    if (msg.actions.isNotEmpty()) {
                                        put("actions", buildJsonArray {
                                            msg.actions.forEach { action ->
                                                add(
                                                    buildJsonObject {
                                                        put("toolName", action.toolName)
                                                        put("label", action.label)
                                                        put("succeeded", action.succeeded)
                                                    }
                                                )
                                            }
                                        })
                                    }
                                    msg.planPreview?.let { plan ->
                                        put("planPreview", buildJsonObject {
                                            put("title", plan.title)
                                            put("summary", plan.summary)
                                            put("items", buildJsonArray {
                                                plan.items.forEach { item ->
                                                    add(
                                                        buildJsonObject {
                                                            put("title", item.title)
                                                            put("detail", item.detail)
                                                            put("dateIso", item.dateIso)
                                                            put("estimatedMinutes", item.estimatedMinutes)
                                                            put("priority", item.priority)
                                                        }
                                                    )
                                                }
                                            })
                                        })
                                    }
                                }
                            )
                        }
                    })
                }
            )
        }
    }.toString()

    private fun decodeThreads(raw: String): List<AgentConversationThread> = runCatching {
        Json.parseToJsonElement(raw).jsonArray.mapNotNull { threadEl ->
            val obj = threadEl.jsonObject
            val id = obj["id"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val title = obj["title"]?.jsonPrimitive?.contentOrNull?.ifBlank { "Discussion" } ?: "Discussion"
            val updatedAt = obj["updatedAtMillis"]?.jsonPrimitive?.longOrNull ?: 0L
            val messages = (obj["messages"] as? JsonArray)?.mapNotNull { msgEl ->
                decodeMessage(msgEl.jsonObject)
            }.orEmpty()
            AgentConversationThread(
                id = id,
                title = title,
                updatedAtMillis = updatedAt,
                messages = messages
            )
        }
    }.getOrDefault(emptyList())

    private fun decodeMessage(obj: JsonObject): AgentChatMessage? {
        val id = obj["id"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        val isUser = obj["isUser"]?.jsonPrimitive?.booleanOrNull ?: false
        val text = obj["text"]?.jsonPrimitive?.contentOrNull ?: return null
        val isError = obj["isError"]?.jsonPrimitive?.booleanOrNull ?: false
        val actions = (obj["actions"] as? JsonArray)?.mapNotNull { actEl ->
            val act = actEl.jsonObject
            val label = act["label"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            AgentActionLog(
                toolName = act["toolName"]?.jsonPrimitive?.contentOrNull ?: "",
                label = label,
                succeeded = act["succeeded"]?.jsonPrimitive?.booleanOrNull ?: true
            )
        }.orEmpty()
        val plan = (obj["planPreview"] as? JsonObject)?.let { planObj ->
            val pTitle = planObj["title"]?.jsonPrimitive?.contentOrNull ?: ""
            val pSummary = planObj["summary"]?.jsonPrimitive?.contentOrNull ?: ""
            val pItems = (planObj["items"] as? JsonArray)?.mapNotNull { itemEl ->
                val item = itemEl.jsonObject
                AgentPlanPreviewItem(
                    title = item["title"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                    detail = item["detail"]?.jsonPrimitive?.contentOrNull ?: "",
                    dateIso = item["dateIso"]?.jsonPrimitive?.contentOrNull ?: "",
                    estimatedMinutes = item["estimatedMinutes"]?.jsonPrimitive?.intOrNull ?: 0,
                    priority = item["priority"]?.jsonPrimitive?.contentOrNull ?: "medium"
                )
            }.orEmpty()
            if (pTitle.isBlank() && pItems.isEmpty()) null else AgentPlanPreview(pTitle, pSummary, pItems)
        }
        return AgentChatMessage(
            id = id,
            isUser = isUser,
            text = text,
            actions = actions,
            isError = isError,
            planPreview = plan
        )
    }
}
