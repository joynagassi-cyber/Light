package com.example.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream

/**
 * Lecture bornée d'une page web publique en HTTPS.
 *
 * Garde-fous : https uniquement, hôtes privés/locaux bloqués (y compris après redirection),
 * taille plafonnée, contenu converti en texte brut avant d'être donné au modèle.
 * Aucun cookie, aucune authentification, aucun POST.
 */
object WebPageFetcher {

    private const val MAX_BYTES = 512 * 1024
    private const val DEFAULT_MAX_CHARS = 3500
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 15_000

    suspend fun fetchAsJson(url: String, maxChars: Int = DEFAULT_MAX_CHARS): JsonObject =
        withContext(Dispatchers.IO) {
            try {
                val page = fetch(url, maxChars)
                buildJsonObject {
                    put("success", JsonPrimitive(true))
                    put("url", JsonPrimitive(page.finalUrl))
                    put("title", JsonPrimitive(page.title))
                    put("text", JsonPrimitive(page.text))
                    put("truncated", JsonPrimitive(page.truncated))
                    put("source", JsonPrimitive("web"))
                }
            } catch (e: Exception) {
                buildJsonObject {
                    put("success", JsonPrimitive(false))
                    put("error", JsonPrimitive(e.message ?: "Page inaccessible."))
                }
            }
        }

    internal fun fetch(url: String, maxChars: Int = DEFAULT_MAX_CHARS): PageContent {
        val normalized = url.trim()
        require(normalized.startsWith("https://", ignoreCase = true)) { "Seules les URL https:// sont acceptées." }
        val parsed = runCatching { URL(normalized) }.getOrElse { throw IllegalArgumentException("URL invalide.") }
        require(parsed.userInfo == null) { "Les URL avec identifiants sont refusées." }
        require(!isBlockedHost(parsed.host)) { "Adresse locale ou privée refusée." }

        val connection = (parsed.openConnection() as HttpURLConnection).apply {
            instanceFollowRedirects = true
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setRequestProperty("User-Agent", "LightStudyAssistant/1.0")
            setRequestProperty("Accept", "text/html,application/xhtml+xml,text/plain;q=0.9,*/*;q=0.1")
            setRequestProperty("Accept-Encoding", "gzip")
        }
        try {
            val code = connection.responseCode
            require(code in 200..299) { "La page a répondu HTTP $code." }
            val finalUrl = connection.url?.toString() ?: normalized
            require(finalUrl.startsWith("https://", ignoreCase = true)) { "Redirection non https refusée." }
            val finalHost = runCatching { URL(finalUrl).host }.getOrNull()
            require(finalHost != null && !isBlockedHost(finalHost)) { "Redirection vers une adresse privée refusée." }
            val contentType = connection.contentType.orEmpty().lowercase()
            require(
                contentType.isBlank() || contentType.contains("text/html") || contentType.contains("application/xhtml+xml") ||
                    contentType.contains("text/plain")
            ) { "Type de contenu non lisible (${contentType.take(60)})." }

            val raw = readBounded(connection)
            val html = decodeBody(raw, connection.getHeaderField("Content-Encoding"), contentType)
            val title = Regex("<title[^>]*>(.*?)</title>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                .find(html)?.groupValues?.get(1)?.let { collapseWhitespace(decodeEntities(stripTags(it))).take(200) }
                .orEmpty()
            val text = collapseWhitespace(decodeEntities(stripTags(html)))
            val truncated = text.length > maxChars
            val body = if (truncated) text.take(maxChars) else text
            require(body.isNotBlank()) { "Page vide ou sans texte exploitable." }
            return PageContent(finalUrl, title, body, truncated)
        } finally {
            connection.disconnect()
        }
    }

    private fun readBounded(connection: HttpURLConnection): ByteArray {
        val stream = connection.inputStream ?: throw IllegalStateException("Flux de lecture indisponible.")
        val output = ByteArrayOutputStream(MAX_BYTES)
        val buffer = ByteArray(16 * 1024)
        var total = 0
        stream.use { input ->
            while (total < MAX_BYTES) {
                val read = input.read(buffer)
                if (read <= 0) break
                output.write(buffer, 0, read)
                total += read
            }
        }
        return output.toByteArray()
    }

    private fun decodeBody(bytes: ByteArray, contentEncoding: String?, contentType: String): String {
        val payload = if (contentEncoding.equals("gzip", ignoreCase = true)) {
            runCatching { GZIPInputStream(bytes.inputStream()).readBytes() }.getOrElse { bytes }
        } else bytes
        val charsetName = Regex("charset=([\\w.-]+)", RegexOption.IGNORE_CASE).find(contentType)
            ?.groupValues?.get(1) ?: "UTF-8"
        val charset = runCatching { Charset.forName(charsetName) }.getOrDefault(Charsets.UTF_8)
        return runCatching { String(payload, charset) }
            .getOrElse { String(payload, Charsets.UTF_8) }
    }

    internal fun isBlockedHost(host: String?): Boolean {
        val h = host?.trim()?.lowercase()?.trimEnd('.').orEmpty()
        if (h.isEmpty()) return true
        if (h == "localhost" || h == "0.0.0.0" || h == "::1" || h.endsWith(".local") || h.endsWith(".internal") || h.endsWith(".lan")) return true
        if (h.startsWith("127.") || h.startsWith("10.") || h.startsWith("192.168.") ||
            h.startsWith("169.254.") || h.startsWith("0.") || h.startsWith("100.64.") || h.startsWith("192.0.0.")
        ) return true
        if (Regex("^172\\.(1[6-9]|2[0-9]|3[01])\\.").matches(h)) return true
        if (h.contains(":")) return true // IPv6 littérales : on bloque par prudence (loopback, ULA, link-local).
        return false
    }

    internal fun stripTags(html: String): String {
        var text = html
        // Retire les blocs non textuels avant de considérer le reste.
        Regex("<(script|style|noscript|template|svg|head)[^>]*>.*?</\\1>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            .replace(text, " ").also { text = it }
        Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL).replace(text, " ").also { text = it }
        return Regex("<[^>]+>").replace(text, " ")
    }

    internal fun decodeEntities(text: String): String = buildString(text.length) {
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '&') {
                val end = text.indexOf(';', i + 1)
                if (end in (i + 1)..(i + 10)) {
                    val entity = text.substring(i + 1, end)
                    val decoded = when (entity.lowercase()) {
                        "amp" -> "&"; "lt" -> "<"; "gt" -> ">"; "quot" -> "\""; "apos" -> "'"; "nbsp" -> " "
                        else -> if (entity.startsWith("#")) {
                            entity.drop(1).let { num ->
                                num.removePrefix("x").removePrefix("X")
                                    .let { runCatching { if (entity.contains("x", ignoreCase = true)) it.toInt(16) else it.toInt() }.getOrNull() }
                                    ?.toChar()?.toString()
                            }
                        } else null
                    }
                    if (decoded != null) {
                        append(decoded)
                        i = end + 1
                        continue
                    }
                }
            }
            append(c)
            i++
        }
    }

    internal fun collapseWhitespace(text: String): String =
        text.replace(Regex("[\\s\\u00a0]+"), " ").trim()

    data class PageContent(val finalUrl: String, val title: String, val text: String, val truncated: Boolean)
}
