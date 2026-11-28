package com.example.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResearchToolsTest {

    private val arxivFeed = """<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
  <entry>
    <id>http://arxiv.org/abs/2401.12345v1</id>
    <published>2024-01-31T10:00:00Z</published>
    <title>Spaced  repetition
      and long-term retention</title>
    <summary>We review the evidence for distributed practice
      across domains.</summary>
    <author><name>Alice Martin</name></author>
    <author><name>Bob Koné</name></author>
  </entry>
  <entry>
    <id>http://arxiv.org/abs/2402.00001v2</id>
    <published>2024-02-02T08:30:00Z</published>
    <title>Testing effect: a meta-analysis</title>
    <summary>Retrieval practice improves delayed recall.</summary>
    <author><name>Carla Ruiz</name></author>
  </entry>
</feed>"""

    @Test fun parseArxivAtomExtractsTitlesAuthorsAndLinks() {
        val papers = AcademicSearchRepository().parseArxivAtom(arxivFeed)
        assertEquals(2, papers.size)
        assertEquals("Spaced repetition and long-term retention", papers[0].title)
        assertEquals("http://arxiv.org/abs/2401.12345v1", papers[0].url)
        assertEquals(listOf("Alice Martin", "Bob Koné"), papers[0].authors)
        assertEquals("2024-01-31T10:00:00Z", papers[0].published)
        assertTrue(papers[0].summary.startsWith("We review"))
        assertEquals("Testing effect: a meta-analysis", papers[1].title)
    }

    @Test fun parseArxivAtomToleratesEmptyOrBrokenFeeds() {
        val empty = AcademicSearchRepository().parseArxivAtom("""<?xml version="1.0"?><feed xmlns="http://www.w3.org/2005/Atom"></feed>""")
        assertEquals(emptyList<AcademicSearchRepository.AcademicPaper>(), empty)
    }

    @Test fun webPageFetcherBlocksPrivateAndLocalHosts() {
        listOf("localhost", "127.0.0.1", "10.0.0.4", "192.168.1.10", "172.16.0.8", "172.31.255.1",
            "169.254.1.1", "0.0.0.0", "foo.local", "printer.internal", "100.64.0.1").forEach { host ->
            assertTrue(host, WebPageFetcher.isBlockedHost(host))
        }
        listOf("example.org", "www.arxiv.org", "172.15.0.1", "172.32.0.1").forEach { host ->
            assertFalse(host, WebPageFetcher.isBlockedHost(host))
        }
    }

    @Test fun webPageFetcherStripsMarkupAndKeepsText() {
        val html = """<html><head><title>Ignoré</title><style>body{}</style><script>var x=1;</script></head>
            <body><h1>Titre &amp; sauce</h1><p>Texte   avec&nbsp;espaces
            multiples &lt;tag&gt; fin.</p><!-- note --></body></html>"""
        val stripped = WebPageFetcher.stripTags(html)
        val text = WebPageFetcher.collapseWhitespace(WebPageFetcher.decodeEntities(stripped))
        assertFalse(text.contains("var x=1"))
        assertFalse(text.contains("body{}"))
        assertFalse(text.contains("note"))
        assertTrue(text.contains("Titre & sauce"))
        assertTrue(text.contains("Texte avec espaces multiples <tag> fin."))
    }
}
