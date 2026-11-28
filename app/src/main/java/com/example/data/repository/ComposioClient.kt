package com.example.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Client minimal pour l'API Composio v3.1 (https://backend.composio.dev/api/v3.1).
 *
 * Implémenté en direct (sans SDK) : surface réduite aux trois besoins de Light —
 * lister les comptes connectés (statut ACTIVE), découvrir les outils (méta-outil
 * COMPOSIO_SEARCH_TOOLS, exécuté via le même point d'entrée) et exécuter un outil.
 *
 * NON VÉRIFIÉ avec un compte réel : les formes exactes des réponses (enveloppe
 * {data,error,successful} pour l'exécution ; {items:[...]} pour la liste des comptes)
 * sont celles documentées publiquement au 2026-11-28, mais peuvent varier — le parsing
 * est défensif et conserve les charges brutes pour diagnostic. La clé API n'est
 * jamais journalisée.
 */
class ComposioClient {

    data class ConnectedAccount(
        val id: String,
        val toolkit: String,
        val status: String,
    )

    private val json = Json { ignoreUnknownKeys = true }

    /** GET /connected_accounts?user_ids=…&statuses=ACTIVE — comptes prêts à l'emploi. */
    suspend fun listConnectedAccounts(apiKey: String, userId: String): List<ConnectedAccount> = withContext(Dispatchers.IO) {
        val query = mapOf("user_ids" to userId, "statuses" to "ACTIVE")
        val (code, body) = request("GET", "/connected_accounts", apiKey, null, query)
        require(code in 200..299) { "Composio a répondu HTTP $code pour le listage des comptes." }
        val root = runCatching { json.parseToJsonElement(body) }.getOrNull() as? JsonObject
            ?: throw IllegalStateException("Réponse Composio illisible (liste des comptes).")
        val items = extractItems(root)
        items.mapNotNull { item ->
            val obj = item as? JsonObject ?: return@mapNotNull null
            val id = obj["id"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val toolkit = ((obj["toolkit"] as? JsonObject)?.get("slug")?.jsonPrimitive?.contentOrNull)
                ?: obj["toolkit_slug"]?.jsonPrimitive?.contentOrNull
                ?: obj["appName"]?.jsonPrimitive?.contentOrNull
                ?: ""
            val status = obj["status"]?.jsonPrimitive?.contentOrNull ?: "UNKNOWN"
            ConnectedAccount(id = id, toolkit = toolkit.lowercase(), status = status.uppercase())
        }
    }

    /** POST /tools/execute/{TOOL_SLUG} — exécute un outil Composio pour le compte utilisateur. */
    suspend fun executeTool(
        apiKey: String,
        userId: String,
        toolSlug: String,
        arguments: JsonObject,
        connectedAccountId: String?,
    ): JsonObject = withContext(Dispatchers.IO) {
        val slug = toolSlug.trim().uppercase()
        require(TOOL_SLUG_REGEX.matches(slug)) { "Slug d'outil invalide : $toolSlug" }
        val payload = buildJsonObject {
            put("user_id", JsonPrimitive(userId))
            put("version", JsonPrimitive("latest"))
            connectedAccountId?.takeIf { it.isNotBlank() }?.let { put("connected_account_id", JsonPrimitive(it)) }
            put("arguments", arguments)
        }
        val (code, body) = request("POST", "/tools/execute/$slug", apiKey, payload.toString(), emptyMap())
        val parsed = runCatching { json.parseToJsonElement(body) }.getOrNull() as? JsonObject
        if (code !in 200..299) {
            return@withContext buildJsonObject {
                put("success", JsonPrimitive(false))
                put("httpStatus", JsonPrimitive(code))
                put("error", JsonPrimitive(extractError(parsed) ?: "HTTP $code. ${body.take(400)}"))
            }
        }
        val error = extractError(parsed)
        val successful = parsed?.get("successful")?.jsonPrimitive?.booleanOrNull ?: (error == null)
        buildJsonObject {
            put("success", JsonPrimitive(successful && error == null))
            put("tool", JsonPrimitive(slug))
            if (error != null) put("error", JsonPrimitive(error.take(500)))
            // Réponse brute bornée : le modèle lit la structure, on borne pour protéger le contexte.
            put("result", JsonPrimitive(body.take(MAX_RESULT_CHARS)))
        }
    }

    /** Découverte d'outils via le méta-outil COMPOSIO_SEARCH_TOOLS (même endpoint d'exécution). */
    suspend fun searchTools(apiKey: String, userId: String, query: String): JsonObject =
        executeTool(
            apiKey = apiKey,
            userId = userId,
            toolSlug = "COMPOSIO_SEARCH_TOOLS",
            arguments = buildJsonObject { put("query", JsonPrimitive(query.trim().take(300))) },
            connectedAccountId = null,
        )

    private fun extractItems(root: JsonObject): List<kotlinx.serialization.json.JsonElement> {
        (root["items"] as? JsonArray)?.let { return it.toList() }
        (root["connected_accounts"] as? JsonArray)?.let { return it.toList() }
        (root["data"] as? JsonArray)?.let { return it.toList() }
        ((root["data"] as? JsonObject)?.get("items") as? JsonArray)?.let { return it.toList() }
        return emptyList()
    }

    private fun extractError(parsed: JsonObject?): String? {
        if (parsed == null) return null
        val error = parsed["error"] ?: return null
        return when (error) {
            JsonNull -> null
            is JsonPrimitive -> error.contentOrNull?.takeIf { it.isNotBlank() }
            else -> error.toString().take(500)
        }
    }

    private fun request(
        method: String,
        path: String,
        apiKey: String,
        body: String?,
        query: Map<String, String>,
    ): Pair<Int, String> {
        val encodedQuery = if (query.isEmpty()) "" else query.entries.joinToString("&", prefix = "?") { (k, v) ->
            "$k=${URLEncoder.encode(v, "UTF-8")}"
        }
        val connection = (URL("$BASE_URL$path$encodedQuery").openConnection() as HttpURLConnection).apply {
            this.requestMethod = method
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setRequestProperty("x-api-key", apiKey.trim())
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            doOutput = body != null
        }
        try {
            if (body != null) {
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val output = ByteArrayOutputStream()
            stream?.use { input ->
                val buffer = ByteArray(16 * 1024)
                var total = 0
                while (total < MAX_BODY_BYTES) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    output.write(buffer, 0, read)
                    total += read
                }
            }
            return code to String(output.toByteArray(), Charsets.UTF_8)
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val BASE_URL = "https://backend.composio.dev/api/v3.1"
        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 30_000
        const val MAX_BODY_BYTES = 1024 * 1024
        const val MAX_RESULT_CHARS = 3000
        val TOOL_SLUG_REGEX = Regex("^[A-Z0-9_]{3,80}$")
    }
}
