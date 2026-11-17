package com.example.data.repository

import com.example.data.model.AgentModelOption
import com.example.data.model.AgentProvider
import com.example.data.model.ModelCostKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull
import java.net.HttpURLConnection
import java.net.URL

class AgentModelCatalogRepository {
    suspend fun loadModels(providerId: String, baseUrl: String, apiKey: String?): List<AgentModelOption> {
        val provider = AgentProvider.fromId(providerId)
        if (provider == AgentProvider.FIREBASE) {
            return listOf(
                AgentModelOption("gemini-2.5-flash", "Gemini 2.5 Flash", ModelCostKind.ACCOUNT_DEPENDENT, "Facturation selon le projet Firebase", contextTokens = 1_048_576),
                AgentModelOption("gemini-2.5-pro", "Gemini 2.5 Pro", ModelCostKind.ACCOUNT_DEPENDENT, "Facturation selon le projet Firebase", contextTokens = 1_048_576)
            )
        }
        require(!apiKey.isNullOrBlank()) { "Saisissez la clé API du fournisseur avant de charger les modèles." }
        val base = (baseUrl.ifBlank { provider.baseUrl }).trim().trimEnd('/')
        require(base.startsWith("https://", true)) { "L'URL du fournisseur doit utiliser HTTPS." }
        val url = when (provider) {
            AgentProvider.OPENROUTER -> "$base/models?output_modalities=text&endpoints=%2Fapi%2Fv1%2Fchat%2Fcompletions"
            else -> "$base/models${if (provider == AgentProvider.ANTHROPIC) "?limit=1000" else ""}"
        }
        return withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                connection.instanceFollowRedirects = false
                connection.requestMethod = "GET"
                connection.connectTimeout = 15_000
                connection.readTimeout = 30_000
                connection.setRequestProperty("Accept", "application/json")
                if (provider == AgentProvider.ANTHROPIC) {
                    connection.setRequestProperty("x-api-key", apiKey.trim())
                    connection.setRequestProperty("anthropic-version", "2026-10-21")
                } else {
                    connection.setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
                }
                val code = connection.responseCode
                val responseBody = (if (code in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                if (code !in 200..299) {
                    val message = runCatching {
                        Json.parseToJsonElement(responseBody).jsonObject["error"]?.let(::extractMessage)
                    }.getOrNull()
                    throw IllegalStateException(message ?: "Le fournisseur a répondu HTTP $code lors du chargement des modèles.")
                }
                val array = Json.parseToJsonElement(responseBody).jsonObject["data"]?.jsonArray
                    ?: throw IllegalStateException("Le catalogue du fournisseur ne contient pas de liste de modèles.")
                array.mapNotNull { element ->
                    val item = element.jsonObject
                    val id = item["id"]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank) ?: return@mapNotNull null
                    if (provider == AgentProvider.AGNES && id.contains("image", true)) return@mapNotNull null
                    val name = item["name"]?.jsonPrimitive?.contentOrNull
                        ?: item["display_name"]?.jsonPrimitive?.contentOrNull
                        ?: id
                    val price = item["pricing"]?.jsonObject
                    val promptRate = price?.get("prompt")?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()
                    val outputRate = price?.get("completion")?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()
                    val cost = when (provider) {
                        AgentProvider.OPENROUTER -> when {
                            id.endsWith(":free") || ((promptRate ?: 1.0) == 0.0 && (outputRate ?: 1.0) == 0.0) -> ModelCostKind.FREE_MODEL
                            else -> ModelCostKind.PAID
                        }
                        AgentProvider.AGNES, AgentProvider.GROQ, AgentProvider.OPENAI, AgentProvider.ANTHROPIC -> ModelCostKind.ACCOUNT_DEPENDENT
                        else -> ModelCostKind.UNKNOWN
                    }
                    val costText = when (cost) {
                        ModelCostKind.FREE_MODEL -> "Gratuit · offre OpenRouter du moment"
                        ModelCostKind.FREE_QUOTA -> "Quota gratuit limité · forfaits payants possibles"
                        ModelCostKind.PAID -> "Payant · facturation à l'usage"
                        ModelCostKind.ACCOUNT_DEPENDENT -> "Prix/quotas selon le compte · vérifier le tarif officiel"
                        ModelCostKind.UNKNOWN -> "Tarif non publié par l'API des modèles"
                    }
                    AgentModelOption(
                        id = id,
                        displayName = name,
                        costKind = cost,
                        costDescription = costText,
                        promptUsdPerMillion = promptRate?.times(1_000_000.0),
                        outputUsdPerMillion = outputRate?.times(1_000_000.0),
                        contextTokens = item["context_length"]?.jsonPrimitive?.longOrNull
                            ?: item["max_input_tokens"]?.jsonPrimitive?.longOrNull
                            ?: item["context_window"]?.jsonPrimitive?.longOrNull
                    )
                }.distinctBy { it.id }.sortedWith(compareBy<AgentModelOption>({ it.costKind.ordinal }, { it.displayName.lowercase() }))
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun extractMessage(element: kotlinx.serialization.json.JsonElement): String? = runCatching {
        when (element) {
            is JsonObject -> element["message"]?.jsonPrimitive?.contentOrNull
            else -> element.jsonPrimitive.contentOrNull
        }
    }.getOrNull()
}
