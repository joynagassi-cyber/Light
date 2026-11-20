package com.example.data.repository

import com.example.data.model.AgentProvider
import com.example.data.preferences.StudyAppSettings
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import java.net.URI

internal data class PlannerProviderRequest(val endpoint: String, val payload: JsonObject, val providerId: String)

internal fun buildPlannerProviderRequest(prompt: String, settings: StudyAppSettings): PlannerProviderRequest {
    val model = settings.agentModel.trim()
    require(model.isNotEmpty()) { "Sélectionnez d’abord un modèle dans les réglages de Light Agent." }
    val provider = AgentProvider.fromId(settings.agentProvider)
    require(provider != AgentProvider.FIREBASE) { "Ce fournisseur utilise la configuration Firebase." }
    val anthropic = provider == AgentProvider.ANTHROPIC
    val root = settings.agentBaseUrl.ifBlank { provider.baseUrl }.trim().trimEnd('/')
    val base = runCatching { URI(root) }.getOrNull()
    require(base != null && base.scheme.equals("https", ignoreCase = true) && !base.host.isNullOrBlank() &&
        base.userInfo == null && base.rawQuery == null && base.rawFragment == null) {
        "Indiquez une URL de fournisseur HTTPS sans identifiants, paramètres ni fragment."
    }
    val route = if (anthropic) "/messages" else "/chat/completions"
    val endpoint = when {
        root.endsWith(route, ignoreCase = true) -> root
        root.endsWith("/v1", ignoreCase = true) -> "$root$route"
        anthropic && root.endsWith("/chat/completions", ignoreCase = true) -> root.dropLast("/chat/completions".length) + route
        else -> "$root/v1$route"
    }
    val body = buildJsonObject {
        put("model", model)
        if (anthropic) put("max_tokens", 4096)
        put("messages", JsonArray(listOf(buildJsonObject {
            put("role", "user")
            put("content", prompt)
        })))
    }
    return PlannerProviderRequest(endpoint, body, provider.id)
}

internal fun plannerProviderText(response: JsonObject, providerId: String): String {
    fun text(value: JsonPrimitive?): String? = value?.takeIf { it.isString }?.contentOrNull
    val result = if (providerId == AgentProvider.ANTHROPIC.id) {
        require(text(response["stop_reason"] as? JsonPrimitive) != "max_tokens") {
            "Le modèle a interrompu son plan avant la fin. Réessayez avec un objectif plus court."
        }
        (response["content"] as? JsonArray).orEmpty().mapNotNull { element ->
            val block = element as? JsonObject ?: return@mapNotNull null
            if (text(block["type"] as? JsonPrimitive) == "text") text(block["text"] as? JsonPrimitive) else null
        }.joinToString("\n")
    } else {
        val choice = (response["choices"] as? JsonArray)?.firstOrNull() as? JsonObject
        val message = choice?.get("message") as? JsonObject
        text(message?.get("content") as? JsonPrimitive).orEmpty()
    }
    return result.takeIf(String::isNotBlank) ?: error("Le modèle actif n’a pas renvoyé de plan exploitable.")
}
