package com.example.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import java.net.HttpURLConnection
import java.net.URL

data class SearchResult(
    val title: String,
    val url: String,
    val snippet: String,
    val source: String
)

class SearchRepository {

    /**
     * Recherche explicite, bornée, sans navigation arbitraire.
     * Pré-requis: opt-in activé + clé locale présente. Les clés ne sont jamais journalisées.
     * Les résultats incluent toujours l'URL source pour attribution visible.
     */
    suspend fun search(
        query: String,
        providerId: String,
        apiKey: String?,
        maxResults: Int = 5
    ): List<SearchResult> {
        val q = query.trim().take(300)
        require(q.length >= 2) { "Requête trop courte." }
        require(!apiKey.isNullOrBlank()) { "Clé API manquante pour $providerId. Ajoutez-la dans Réglages → Recherche." }
        require(maxResults in 1..10) { "maxResults invalide" }

        return when (providerId) {
            "tavily" -> searchTavily(q, apiKey, maxResults)
            "exa" -> searchExa(q, apiKey, maxResults)
            else -> throw IllegalArgumentException("Fournisseur de recherche inconnu: $providerId")
        }
    }

    private suspend fun searchTavily(query: String, apiKey: String, maxResults: Int): List<SearchResult> =
        withContext(Dispatchers.IO) {
            val payload = buildJsonObject {
                put("api_key", apiKey.trim())
                put("query", query)
                put("max_results", maxResults)
                put("search_depth", "basic")
                put("include_answer", false)
            }
            val response = postJson("https://api.tavily.com/search", payload.toString(), null)
            val results = response["results"]?.jsonArray ?: JsonArray(emptyList())
            results.mapNotNull { el ->
                val obj = el.jsonObject
                val title = obj["title"]?.jsonPrimitive?.contentOrNull?.take(200)?.ifBlank { null } ?: return@mapNotNull null
                val url = obj["url"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                if (!url.startsWith("https://")) return@mapNotNull null
                SearchResult(
                    title = title,
                    url = url,
                    snippet = obj["content"]?.jsonPrimitive?.contentOrNull?.take(400).orEmpty(),
                    source = "Tavily"
                )
            }
        }

    private suspend fun searchExa(query: String, apiKey: String, maxResults: Int): List<SearchResult> =
        withContext(Dispatchers.IO) {
            val payload = buildJsonObject {
                put("query", query)
                put("numResults", maxResults)
            }
            val response = postJson("https://api.exa.ai/search", payload.toString(), apiKey)
            val results = response["results"]?.jsonArray ?: JsonArray(emptyList())
            results.mapNotNull { el ->
                val obj = el.jsonObject
                val title = obj["title"]?.jsonPrimitive?.contentOrNull?.take(200)?.ifBlank { null } ?: return@mapNotNull null
                val url = obj["url"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                if (!url.startsWith("https://")) return@mapNotNull null
                SearchResult(
                    title = title,
                    url = url,
                    snippet = obj["text"]?.jsonPrimitive?.contentOrNull?.take(400).orEmpty(),
                    source = "Exa"
                )
            }
        }

    private fun postJson(endpoint: String, body: String, bearer: String?): JsonObject {
        val conn = URL(endpoint).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 15_000
            conn.readTimeout = 20_000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.setRequestProperty("Accept", "application/json")
            if (bearer != null) conn.setRequestProperty("x-api-key", bearer.trim())
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            if (code !in 200..299) {
                // Ne pas inclure le corps d'erreur (peut contenir des fragments de clé)
                conn.errorStream?.close()
                throw IllegalStateException("Recherche indisponible (HTTP $code). Vérifiez la clé et le quota.")
            }
            val text = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            return Json.parseToJsonElement(text).jsonObject
        } finally {
            conn.disconnect()
        }
    }
}
