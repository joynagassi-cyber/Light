package com.example.data.document

import java.io.OutputStream

internal fun isAgentMarkdownDocument(text: String): Boolean =
    text.length > 4_000 || text.lineSequence().take(25).count() == 25

/** Keep every code unit in order without cutting a UTF-16 surrogate pair. Raw source, not HTML. */
internal fun pageAgentMarkdown(text: String, pageSize: Int = 6_000): List<String> {
    require(pageSize >= 2)
    if (text.isEmpty()) return listOf("")
    return buildList {
        var start = 0
        while (start < text.length) {
            var end = start + minOf(pageSize, text.length - start)
            if (end < text.length) {
                val newline = text.lastIndexOf('\n', end - 1)
                if (newline >= start + pageSize / 2) end = newline + 1
                if (text[end - 1].isHighSurrogate() && text[end].isLowSurrogate()) end--
            }
            add(text.substring(start, end))
            start = end
        }
    }
}

internal fun writeAgentMarkdown(output: OutputStream, markdown: String) {
    output.bufferedWriter(Charsets.UTF_8).use { it.write(markdown) }
}
