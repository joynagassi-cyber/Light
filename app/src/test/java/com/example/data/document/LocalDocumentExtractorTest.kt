package com.example.data.document

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocalDocumentExtractorTest {
    private val extractor by lazy {
        LocalDocumentExtractor(ApplicationProvider.getApplicationContext<Context>())
    }

    @Test
    fun `positioned PDF text keeps table columns and rows distinct`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        PDFBoxResourceLoader.init(context)
        PDDocument().use { document ->
            val page = PDPage()
            document.addPage(page)
            PDPageContentStream(document, page).use { stream ->
                stream.beginText()
                stream.setFont(PDType1Font.HELVETICA, 12f)
                stream.newLineAtOffset(50f, 750f)
                stream.showText("Course")
                stream.newLineAtOffset(120f, 0f)
                stream.showText("Hours")
                stream.newLineAtOffset(-120f, -20f)
                stream.showText("Biology")
                stream.newLineAtOffset(120f, 0f)
                stream.showText("12")
                stream.endText()
            }

            val stripper = PositionedTableTextStripper()
            stripper.getText(document)
            val tableText = stripper.asPageRows().single()

            assertTrue(tableText.contains("Course  |  Hours"))
            assertTrue(tableText.contains("Biology  |  12"))
            assertTrue(tableText.lines().size >= 2)
        }
    }

    @Test
    fun `Word tables retain row and cell boundaries`() {
        val xml = """
            <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
              <w:body>
                <w:p><w:r><w:t>Study outline</w:t></w:r></w:p>
                <w:tbl>
                  <w:tr>
                    <w:tc><w:p><w:r><w:t>Course</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:t>Hours</w:t></w:r></w:p></w:tc>
                  </w:tr>
                  <w:tr>
                    <w:tc><w:p><w:r><w:t>Biology</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:t>12</w:t></w:r></w:p></w:tc>
                  </w:tr>
                </w:tbl>
              </w:body>
            </w:document>
        """.trimIndent()

        val text = extractor.xmlDocxText(xml)

        assertTrue(text.contains("Study outline"))
        assertTrue(text.contains("Course | Hours"))
        assertTrue(text.contains("Biology | 12"))
    }

    @Test
    fun `Excel shared strings cell references formulas and dates are retained`() {
        val sharedXml = """
            <sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
              <si><t>Subject</t></si><si><t>Hours</t></si>
            </sst>
        """.trimIndent()
        val sheetXml = """
            <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
              <sheetData>
                <row r="1"><c r="A1" t="s"><v>0</v></c><c r="B1" t="s"><v>1</v></c></row>
                <row r="2"><c r="A2" t="s"><v>0</v></c><c r="B2"><f>SUM(3,4)</f><v>7</v></c><c r="C2" s="1"><v>45292</v></c></row>
              </sheetData>
            </worksheet>
        """.trimIndent()
        val shared = extractor.parseSharedStrings(sharedXml)

        val text = extractor.parseWorksheet(sheetXml, shared, setOf(1), uses1904DateSystem = false)

        assertEquals(listOf("Subject", "Hours"), shared)
        assertTrue(text.contains("A1: Subject"))
        assertTrue(text.contains("B1: Hours"))
        assertTrue(text.contains("B2: 7 [formule: =SUM(3,4)]"))
        assertTrue(text.contains("C2: 45292 [2024-01-01]"))
    }

    @Test
    fun `Excel date system and leap year compatibility are explicit`() {
        assertEquals("2024-01-01", extractor.excelDateLabel("45292", uses1904DateSystem = false))
        assertEquals("1900-02-29 (date Excel historique)", extractor.excelDateLabel("60", uses1904DateSystem = false))
        assertEquals("1904-01-01", extractor.excelDateLabel("0", uses1904DateSystem = true))
    }
}
