package com.example.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Recherche de publications sur l'API ouverte arXiv (aucun compte ni clé requis).
 * https://info.arxiv.org/help/api/user-manual.html — une requête à la fois, résultats bornés.
 * Le parsing est isolé dans [parseArxivAtom] pour être couvert par des tests unitaires.
 */
class AcademicSearchRepository {

    data class AcademicPaper(
        val title: String,
        val url: String,
        val authors: List<String>,
        val published: String,
        val summary: String,
    )

    suspend fun search(query: String, maxResults: Int = 5): List<AcademicPaper> = withContext(Dispatchers.IO) {
        val q = query.trim().take(300)
        require(q.length >= 2) { "Requête trop courte." }
        val limit = maxResults.coerceIn(1, 10)
        val encoded = URLEncoder.encode(q, "UTF-8").replace("+", "%20")
        val endpoint = "https://export.arxiv.org/api/query?search_query=all:$encoded&sortBy=relevance&sortOrder=descending&max_results=$limit"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setRequestProperty("User-Agent", "LightStudyAssistant/1.0")
            setRequestProperty("Accept", "application/atom+xml,application/xml,text/xml")
        }
        try {
            val code = connection.responseCode
            require(code in 200..299) { "arXiv a répondu HTTP $code." }
            val bytes = connection.inputStream.use { it.readNBytes(MAX_BYTES + 1) }
            require(bytes.size <= MAX_BYTES) { "Réponse arXiv trop volumineuse." }
            parseArxivAtom(String(bytes, Charsets.UTF_8)).take(limit)
        } finally {
            connection.disconnect()
        }
    }

    suspend fun searchAsJson(query: String, maxResults: Int = 5): JsonObject =
        try {
            val papers = search(query, maxResults)
            buildJsonObject {
                put("success", JsonPrimitive(true))
                put("source", JsonPrimitive("arXiv"))
                put("count", JsonPrimitive(papers.size))
                put("papers", JsonArray(papers.map { paper ->
                    buildJsonObject {
                        put("title", JsonPrimitive(paper.title))
                        put("url", JsonPrimitive(paper.url))
                        put("authors", JsonPrimitive(paper.authors.take(4).joinToString(", ")))
                        put("published", JsonPrimitive(paper.published.take(10)))
                        put("summary", JsonPrimitive(paper.summary.take(300)))
                    }
                }))
            }
        } catch (e: Exception) {
            buildJsonObject {
                put("success", JsonPrimitive(false))
                put("error", JsonPrimitive(e.message ?: "Recherche arXiv impossible."))
            }
        }

    internal fun parseArxivAtom(xml: String): List<AcademicPaper> {
        val factory = DocumentBuilderFactory.newInstance().apply {
            // Anti-XXE : DTD et entités externes désactivées (best effort selon les plateformes).
            runCatching { setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
            runCatching { setFeature("http://xml.org/sax/features/external-general-entities", false) }
            runCatching { setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
            isXIncludeAware = false
            isExpandEntityReferences = false
            isNamespaceAware = true
        }
        val document = factory.newDocumentBuilder().parse(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)))
        val entries = document.getElementsByTagNameNS("*", "entry")
        val papers = mutableListOf<AcademicPaper>()
        for (i in 0 until entries.length) {
            val entry = entries.item(i) as? Element ?: continue
            val title = collapse(entry.textOf("title") ?: continue)
            val id = collapse(entry.textOf("id"))?.takeIf { it.startsWith("http") } ?: continue
            val published = collapse(entry.textOf("published")).orEmpty()
            val summary = collapse(entry.textOf("summary")).orEmpty()
            val authors = buildList {
                val nodes = entry.getElementsByTagNameNS("*", "author")
                for (j in 0 until nodes.length) {
                    val author = nodes.item(j) as? Element ?: continue
                    author.textOf("name")?.let { collapse(it) }?.takeIf { it.isNotBlank() }?.let { add(it) }
                }
            }
            papers += AcademicPaper(title = title, url = id, authors = authors, published = published, summary = summary)
        }
        return papers
    }

    private fun Element.textOf(tag: String): String? =
        getElementsByTagNameNS("*", tag).item(0)?.textContent

    private fun collapse(text: String?): String {
        val cleaned = text?.replace(Regex("[\\s\\u00a0]+"), " ")?.trim().orEmpty()
        return cleaned
    }

    private companion object {
        const val MAX_BYTES = 512 * 1024
        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 20_000
    }
}
