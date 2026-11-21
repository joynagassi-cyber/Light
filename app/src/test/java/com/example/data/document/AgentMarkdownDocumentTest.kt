package com.example.data.document

import java.io.ByteArrayOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentMarkdownDocumentTest {
    @Test fun shortMessagesStayInlineButWideOrTallMessagesBecomeDocuments() {
        assertFalse(isAgentMarkdownDocument("a".repeat(4000)))
        assertTrue(isAgentMarkdownDocument("a".repeat(4001)))
        assertFalse(isAgentMarkdownDocument(List(24) { "a" }.joinToString("\n")))
        assertTrue(isAgentMarkdownDocument(List(25) { "a" }.joinToString("\n")))
    }

    @Test fun pageBreaksPreserveUnicodeAndExportPreservesEveryByte() {
        val input = "# Épreuve\r\n" + "abc🚀".repeat(14_000) + "\n<script>texte seulement</script>\n"
        val pages = pageAgentMarkdown(input)
        assertTrue(pages.size > 10)
        assertTrue(pages.all { it.length <= 6_000 && !it.last().isHighSurrogate() && !it.first().isLowSurrogate() })
        assertEquals(input, pages.joinToString(""))
        val output = ByteArrayOutputStream()
        writeAgentMarkdown(output, input)
        assertArrayEquals(input.toByteArray(Charsets.UTF_8), output.toByteArray())
    }
}
