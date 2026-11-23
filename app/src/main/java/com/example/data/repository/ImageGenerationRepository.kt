package com.example.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.HttpURLConnection
import java.net.URL

data class GeneratedImage(
    val url: String,
    val revisedPrompt: String? = null
)

class ImageGenerationRepository {

    /**
     * Génération d'images bornée. La clé est lue depuis le coffre local chiffré; jamais journalisée.
     * OpenAI et OpenRouter sont les seuls fournisseurs actifs; Agnes suspendu (identité non établie).
     * L'appelant choisit le dossier de destination en bibliothèque; l'image est ensuite enregistrée comme LibraryItem.
     */
    suspend fun generate(
        prompt: String,
        providerId: String,
        apiKey: String?,
        size: String = "1024x1024",
        n: Int = 1
    ): List<GeneratedImage> {
        val p = prompt.trim().take(1000)
        require(p.length >= 3) { "Prompt trop court." }
        require(!apiKey.isNullOrBlank()) { "Clé API manquante pour $providerId. Ajoutez-la dans Réglages → Images." }
        require(n in 1..4) { "n invalide" }
        val safeSize = when (size) {
            "1024x1024", "1024x1792", "1792x1024", "512x512", "1024x1536" -> size
            else -> "1024x1024"
        }
        return when (providerId) {
            "openai" -> generateOpenAi(p, apiKey, safeSize, n)
            "openrouter" -> generateOpenRouter(p, apiKey, safeSize, n)
            else -> throw IllegalArgumentException("Fournisseur d'images inconnu: $providerId (Agnes non intégré).")
        }
    }

    private suspend fun generateOpenAi(prompt: String, apiKey: String, size: String, n: Int): List<GeneratedImage> =
        withContext(Dispatchers.IO) {
            val payload = buildJsonObject {
                put("model", "dall-e-3")
                put("prompt", prompt)
                put("n", n)
                put("size", size)
            }
            val resp = postJson("https://api.openai.com/v1/images/generations", payload.toString(), "Bearer ${apiKey.trim()}")
            parseImages(resp)
        }

    private suspend fun generateOpenRouter(prompt: String, apiKey: String, size: String, n: Int): List<GeneratedImage> =
        withContext(Dispatchers.IO) {
            // OpenRouter proxy — modèle à préciser par l'utilisateur dans la doc; on tente un modèle générique
            val payload = buildJsonObject {
                put("model", "openai/dall-e-3")
                put("prompt", prompt)
                put("n", n)
                put("size", size)
            }
            val resp = postJson("https://openrouter.ai/api/v1/images/generations", payload.toString(), "Bearer ${apiKey.trim()}")
            parseImages(resp)
        }

    private fun parseImages(obj: kotlinx.serialization.json.JsonObject): List<GeneratedImage> {
        val data = obj["data"]?.jsonArray ?: JsonArray(emptyList())
        return data.mapNotNull { el ->
            val o = el.jsonObject
            val url = o["url"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            if (!url.startsWith("https://")) return@mapNotNull null
            GeneratedImage(url = url, revisedPrompt = o["revised_prompt"]?.jsonPrimitive?.contentOrNull)
        }
    }

    private fun postJson(endpoint: String, body: String, auth: String): kotlinx.serialization.json.JsonObject {
        val conn = URL(endpoint).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 20_000
            conn.readTimeout = 60_000
            conn.doOutput = true
            conn.setRequestProperty("Authorization", auth)
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            if (code !in 200..299) {
                conn.errorStream?.close()
                Log.w("ImageGen", "Image provider HTTP $code")
                throw IllegalStateException("Génération indisponible (HTTP $code). Vérifiez la clé et le quota.")
            }
            val text = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            return Json.parseToJsonElement(text).jsonObject
        } finally {
            conn.disconnect()
        }
    }
}
