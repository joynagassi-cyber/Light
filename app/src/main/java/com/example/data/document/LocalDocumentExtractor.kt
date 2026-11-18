package com.example.data.document

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Xml
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.StringReader
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.zip.ZipInputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/** Extracts supported study files on-device. No file bytes are uploaded by this class. */
data class ExtractedAgentDocument(
    val displayName: String,
    val text: String,
    val formatLabel: String
)

class LocalDocumentExtractor(private val context: Context) {
    suspend fun extract(uri: Uri): ExtractedAgentDocument = withContext(Dispatchers.IO) {
        val name = displayName(uri)
        val extension = name.substringAfterLast('.', "").lowercase(Locale.ROOT)
        val text = when (extension) {
            "md", "markdown", "txt" -> readPlainText(uri)
            "pdf" -> readPdf(uri)
            "docx" -> readDocx(uri)
            "xlsx" -> readXlsx(uri)
            "png" -> readPng(uri)
            "doc", "xls" -> throw IllegalArgumentException("Le format .${extension} ancien n'est pas pris en charge. Enregistrez le fichier en .docx ou .xlsx.")
            else -> throw IllegalArgumentException("Format non pris en charge. Choisissez MD, PDF, DOCX, XLSX ou PNG.")
        }.trim().take(MAX_TEXT_CHARS)
        require(text.isNotBlank()) { "Aucun texte exploitable n'a été trouvé dans « $name »." }
        ExtractedAgentDocument(name, text, extension.uppercase(Locale.ROOT))
    }

    private fun displayName(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use { if (it.moveToFirst()) it.getString(0)?.let { value -> if (value.isNotBlank()) return value.take(180) } }
        return uri.lastPathSegment?.substringAfterLast('/')?.take(180)?.takeIf(String::isNotBlank) ?: "document"
    }

    private fun readPlainText(uri: Uri): String = readBytes(uri).toString(Charsets.UTF_8).take(MAX_TEXT_CHARS)

    private fun readPdf(uri: Uri): String {
        PDFBoxResourceLoader.init(context.applicationContext)
        val bytes = readBytes(uri)
        PDDocument.load(ByteArrayInputStream(bytes)).use { document ->
            require(document.numberOfPages <= MAX_PDF_PAGES) { "Le PDF dépasse la limite de $MAX_PDF_PAGES pages." }
            val positionedText = PositionedTableTextStripper()
            positionedText.getText(document)
            val pageTexts = positionedText.asPageRows()
            if (pageTexts.isNotEmpty() && pageTexts.all(String::isNotBlank)) {
                return pageTexts.mapIndexed { index, text -> "Page ${index + 1}:\n$text" }.joinToString("\n\n").take(MAX_TEXT_CHARS)
            }

            // Pages without a text layer (including scanned pages in mixed PDFs) are OCR'd locally.
            val renderer = PDFRenderer(document)
            return buildString {
                for (pageIndex in 0 until document.numberOfPages) {
                    if (length >= MAX_TEXT_CHARS) break
                    val embeddedPageText = pageTexts.getOrNull(pageIndex).orEmpty()
                    if (embeddedPageText.isNotBlank()) {
                        append("Page ${pageIndex + 1}:\n").append(embeddedPageText).append("\n\n")
                        continue
                    }
                    val page = document.getPage(pageIndex)
                    val points = page.mediaBox.width.toDouble() * page.mediaBox.height.toDouble()
                    val maxSafeScale = if (points > 0 && points.isFinite()) sqrt(TARGET_OCR_PIXELS.toDouble() / points) else 1.5
                    require(maxSafeScale >= 0.1) { "Une page PDF est trop grande pour l'OCR sécurisé sur cet appareil." }
                    val bitmap = renderer.renderImage(pageIndex, minOf(1.5, maxSafeScale).toFloat())
                    try {
                        val pageText = recognizeBitmap(bitmap)
                        if (pageText.isNotBlank()) append("Page ${pageIndex + 1} (OCR):\n").append(pageText).append("\n\n")
                    } finally {
                        bitmap.recycle()
                    }
                }
            }.take(MAX_TEXT_CHARS)
        }
    }

    private fun readDocx(uri: Uri): String {
        val bytes = readBytes(uri)
        val xml = readZipEntry(bytes, "word/document.xml")
            ?: throw IllegalArgumentException("Le fichier Word ne contient pas le document principal attendu.")
        return xmlDocxText(xml).take(MAX_TEXT_CHARS)
    }

    private fun readXlsx(uri: Uri): String {
        val bytes = readBytes(uri)
        val entries = readZipEntries(bytes) { it == "xl/sharedStrings.xml" || it == "xl/styles.xml" || it == "xl/workbook.xml" ||
            (it.startsWith("xl/worksheets/sheet") && it.endsWith(".xml")) }
        val shared = entries["xl/sharedStrings.xml"]?.let(::parseSharedStrings).orEmpty()
        val dateStyles = entries["xl/styles.xml"]?.let(::parseDateStyleIndexes).orEmpty()
        val uses1904DateSystem = entries["xl/workbook.xml"]?.let(::uses1904DateSystem) ?: false
        val sheets = entries.filterKeys { it.startsWith("xl/worksheets/sheet") && it.endsWith(".xml") }
            .toSortedMap()
        require(sheets.isNotEmpty()) { "Aucune feuille Excel lisible n'a été trouvée." }
        return buildString {
            sheets.forEach { (path, xml) ->
                append("Feuille ").append(path.substringAfterLast('/').removeSuffix(".xml")).append('\n')
                append(parseWorksheet(xml, shared, dateStyles, uses1904DateSystem)).append("\n\n")
                if (length >= MAX_TEXT_CHARS) return@buildString
            }
        }.take(MAX_TEXT_CHARS)
    }

    private fun readPng(uri: Uri): String {
        val bytes = readBytes(uri)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0 && bounds.outWidth.toLong() * bounds.outHeight <= MAX_SOURCE_IMAGE_PIXELS) {
            "L'image PNG est invalide ou trop grande pour l'OCR local."
        }
        val options = BitmapFactory.Options()
        while (bounds.outWidth / options.inSampleSize * (bounds.outHeight / options.inSampleSize) > TARGET_OCR_PIXELS) {
            options.inSampleSize *= 2
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: throw IllegalArgumentException("Impossible de décoder l'image PNG.")
        return try {
            recognizeBitmap(bitmap).take(MAX_TEXT_CHARS)
        } finally {
            bitmap.recycle()
        }
    }

    private fun recognizeBitmap(bitmap: Bitmap): String {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            formatOcrLayout(Tasks.await(recognizer.process(InputImage.fromBitmap(bitmap, 0)))).take(MAX_TEXT_CHARS)
        } finally {
            recognizer.close()
        }
    }

    private fun formatOcrLayout(result: Text): String = buildString {
        result.textBlocks.forEach { block ->
            block.lines.forEach { line ->
                val elements = line.elements.sortedBy { it.boundingBox?.left ?: Int.MIN_VALUE }
                if (elements.isEmpty()) {
                    append(line.text)
                } else {
                    var previousRight: Int? = null
                    elements.forEach { element ->
                        val bounds = element.boundingBox
                        val left = bounds?.left
                        val gap = if (left != null && previousRight != null) left - previousRight!! else 0
                        val lineHeight = bounds?.height()?.coerceAtLeast(1) ?: 1
                        if (isNotEmpty() && last() != '\n') {
                            if (gap > max(18f, lineHeight * 1.4f).toInt()) append("  |  ") else append(' ')
                        }
                        append(element.text)
                        previousRight = bounds?.right
                    }
                }
                append('\n')
            }
            append('\n')
        }
    }

    private fun readBytes(uri: Uri): ByteArray {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Impossible d'ouvrir le fichier sélectionné.")
        return input.use { readBounded(it, MAX_FILE_BYTES) }
    }

    private fun readZipEntry(bytes: ByteArray, expected: String): String? {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.name == expected) return readBounded(zip, MAX_XML_ENTRY_BYTES).toString(Charsets.UTF_8)
                zip.closeEntry()
            }
        }
        return null
    }

    private fun readZipEntries(bytes: ByteArray, keep: (String) -> Boolean): Map<String, String> {
        val result = linkedMapOf<String, String>()
        var expandedBytes = 0
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory && keep(entry.name)) {
                    require(result.size < MAX_XLSX_XML_ENTRIES) { "Le classeur contient trop de feuilles ou de données XML." }
                    val xmlBytes = readBounded(zip, MAX_XML_ENTRY_BYTES)
                    expandedBytes += xmlBytes.size
                    require(expandedBytes <= MAX_XLSX_EXPANDED_BYTES) { "Le contenu décompressé du classeur dépasse la limite autorisée." }
                    result[entry.name] = xmlBytes.toString(Charsets.UTF_8)
                }
                zip.closeEntry()
            }
        }
        return result
    }

    internal fun parseDateStyleIndexes(xml: String): Set<Int> {
        val parser = parser(xml)
        val customFormats = mutableSetOf<Int>()
        val dateStyleIndexes = mutableSetOf<Int>()
        var inCellFormats = false
        var styleIndex = 0
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "numFmt" -> {
                        val formatId = parser.getAttributeValue(null, "numFmtId")?.toIntOrNull()
                        val code = parser.getAttributeValue(null, "formatCode").orEmpty()
                            .replace(Regex("\"[^\"]*\""), "")
                            .replace(Regex("\\\\."), "")
                            .replace(Regex("\\[[^]]*]"), "")
                            .lowercase(Locale.ROOT)
                        if (formatId != null && Regex("y|d|h|s|m{3,}").containsMatchIn(code)) customFormats += formatId
                    }
                    "cellXfs" -> inCellFormats = true
                    "xf" -> if (inCellFormats) {
                        val id = parser.getAttributeValue(null, "numFmtId")?.toIntOrNull() ?: -1
                        if (id in BUILT_IN_DATE_FORMAT_IDS || id in customFormats) dateStyleIndexes += styleIndex
                        styleIndex++
                    }
                }
                XmlPullParser.END_TAG -> if (parser.name == "cellXfs") inCellFormats = false
            }
            parser.next()
        }
        return dateStyleIndexes
    }

    private fun uses1904DateSystem(xml: String): Boolean {
        val parser = parser(xml)
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "workbookPr") {
                val date1904 = parser.getAttributeValue(null, "date1904")
                return date1904 == "1" || date1904?.equals("true", ignoreCase = true) == true
            }
            parser.next()
        }
        return false
    }

    internal fun excelDateLabel(raw: String, uses1904DateSystem: Boolean): String? {
        val serial = raw.toDoubleOrNull()?.takeIf { it >= 0 && it <= 2_958_466 } ?: return null
        val wholeDays = kotlin.math.floor(serial).toLong()
        val date = when {
            uses1904DateSystem -> LocalDate.of(1904, 1, 1).plusDays(wholeDays)
            wholeDays == 60L -> return "1900-02-29 (date Excel historique)"
            wholeDays < 60L -> LocalDate.of(1899, 12, 31).plusDays(wholeDays)
            else -> LocalDate.of(1899, 12, 30).plusDays(wholeDays)
        }
        val fraction = serial - wholeDays
        if (fraction <= 0.0000001) return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val nanos = (fraction * NANOS_PER_DAY).toLong().coerceIn(0, NANOS_PER_DAY - 1)
        val time = LocalTime.ofNanoOfDay(nanos).format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT))
        return "${date.format(DateTimeFormatter.ISO_LOCAL_DATE)} $time"
    }

    internal fun parseSharedStrings(xml: String): List<String> {
        val parser = parser(xml)
        val values = mutableListOf<String>()
        var inString = false
        val current = StringBuilder()
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "si" -> { inString = true; current.setLength(0) }
                    "t" -> if (inString) current.append(parser.nextText())
                }
                XmlPullParser.END_TAG -> if (parser.name == "si" && inString) {
                    require(values.size < MAX_SHARED_STRINGS) { "Le classeur contient trop de chaînes pour une extraction locale sécurisée." }
                    values += current.toString()
                    inString = false
                }
            }
            parser.next()
        }
        return values
    }

    internal fun parseWorksheet(xml: String, shared: List<String>, dateStyles: Set<Int>, uses1904DateSystem: Boolean): String {
        val parser = parser(xml)
        val output = StringBuilder()
        var inCell = false
        var cellType = ""
        var cellRef = ""
        var styleIndex = -1
        var formula = ""
        var value = StringBuilder()
        while (parser.eventType != XmlPullParser.END_DOCUMENT && output.length < MAX_TEXT_CHARS) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "row" -> if (output.isNotEmpty()) output.append('\n')
                    "c" -> {
                        inCell = true
                        cellType = parser.getAttributeValue(null, "t").orEmpty()
                        cellRef = parser.getAttributeValue(null, "r").orEmpty()
                        styleIndex = parser.getAttributeValue(null, "s")?.toIntOrNull() ?: -1
                        formula = ""
                        value = StringBuilder()
                    }
                    "v", "t" -> if (inCell) value.append(parser.nextText())
                    "f" -> if (inCell) formula = parser.nextText()
                }
                XmlPullParser.END_TAG -> when (parser.name) {
                    "c" -> if (inCell) {
                        val raw = value.toString()
                        val content = when {
                            cellType == "s" -> raw.toIntOrNull()?.let(shared::getOrNull) ?: raw
                            cellType == "d" -> raw
                            styleIndex in dateStyles -> excelDateLabel(raw, uses1904DateSystem)?.let { "$raw [$it]" } ?: raw
                            else -> raw
                        }
                        if (content.isNotBlank()) {
                            if (output.isNotEmpty() && output.last() != '\n') output.append("  |  ")
                            if (cellRef.isNotBlank()) output.append(cellRef).append(": ")
                            output.append(content)
                            if (formula.isNotBlank()) output.append(" [formule: =").append(formula).append(']')
                        }
                        inCell = false
                    }
                    "row" -> if (output.isNotEmpty() && output.last() != '\n') output.append('\n')
                }
            }
            parser.next()
        }
        return output.toString()
    }

    internal fun xmlDocxText(xml: String): String {
        val parser = parser(xml)
        val output = StringBuilder()
        val bodyParagraph = StringBuilder()
        var tableDepth = 0
        var cellText: StringBuilder? = null
        var rowCells = mutableListOf<String>()
        while (parser.eventType != XmlPullParser.END_DOCUMENT && output.length < MAX_TEXT_CHARS) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "tbl" -> tableDepth++
                    "tr" -> rowCells = mutableListOf()
                    "tc" -> cellText = StringBuilder()
                    "t" -> {
                        val text = parser.nextText()
                        (cellText ?: if (tableDepth == 0) bodyParagraph else null)?.append(text)
                    }
                    "tab" -> (cellText ?: if (tableDepth == 0) bodyParagraph else null)?.append('\t')
                    "br" -> (cellText ?: if (tableDepth == 0) bodyParagraph else null)?.append(' ')
                }
                XmlPullParser.END_TAG -> when (parser.name) {
                    "p" -> if (cellText != null) {
                        if (cellText!!.isNotEmpty() && cellText!!.last() != ' ') cellText!!.append(' ')
                    } else if (tableDepth == 0) {
                        val paragraph = bodyParagraph.toString().trim()
                        if (paragraph.isNotEmpty()) output.append(paragraph).append('\n')
                        bodyParagraph.setLength(0)
                    }
                    "tc" -> {
                        rowCells += cellText?.toString()?.trim().orEmpty()
                        cellText = null
                    }
                    "tr" -> if (rowCells.any(String::isNotBlank)) output.append(rowCells.joinToString(" | ")).append('\n')
                    "tbl" -> {
                        tableDepth = (tableDepth - 1).coerceAtLeast(0)
                        if (tableDepth == 0) output.append('\n')
                    }
                }
            }
            parser.next()
        }
        return output.toString().take(MAX_TEXT_CHARS)
    }

    private fun parser(xml: String): XmlPullParser = Xml.newPullParser().apply {
        setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        runCatching { setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false) }
        setInput(StringReader(xml))
    }

    private fun readBounded(input: InputStream, limit: Int): ByteArray {
        val output = ByteArrayOutputStream(minOf(limit, 32 * 1024))
        val buffer = ByteArray(8192)
        var total = 0
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            require(total <= limit) { "Le fichier dépasse la limite de ${limit / (1024 * 1024)} Mo." }
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }

    companion object {
        const val MAX_FILE_BYTES = 15 * 1024 * 1024
        const val MAX_TEXT_CHARS = 30_000
        private const val MAX_XML_ENTRY_BYTES = 5 * 1024 * 1024
        private const val MAX_XLSX_EXPANDED_BYTES = 16 * 1024 * 1024
        private const val MAX_XLSX_XML_ENTRIES = 64
        private const val MAX_SHARED_STRINGS = 100_000
        private const val MAX_PDF_PAGES = 100
        private const val MAX_SOURCE_IMAGE_PIXELS = 40_000_000L
        private const val TARGET_OCR_PIXELS = 4_000_000
        private const val NANOS_PER_DAY = 86_400_000_000_000L
        private val BUILT_IN_DATE_FORMAT_IDS = (14..22).toSet() + (27..36).toSet() + (45..47).toSet() + (50..58).toSet()
    }
}

internal class PositionedTableTextStripper : PDFTextStripper() {
    private data class PositionedLine(val y: Float, val glyphs: MutableList<TextPosition>)

    private val pages = mutableListOf<MutableList<TextPosition>>()

    init {
        sortByPosition = true
    }

    override fun startPage(page: PDPage) {
        super.startPage(page)
        pages.add(mutableListOf())
    }

    override fun processTextPosition(text: TextPosition) {
        super.processTextPosition(text)
        if (pages.isEmpty()) pages.add(mutableListOf())
        pages.last().add(text)
    }

    fun asPageRows(): List<String> = pages.map { glyphs ->
        if (glyphs.isEmpty()) return@map ""
        val lines = mutableListOf<PositionedLine>()
        glyphs.sortedBy { it.yDirAdj }.forEach { glyph ->
            val lineTolerance = max(1.5f, glyph.heightDir * 0.35f)
            val line = lines.lastOrNull()?.takeIf { abs(it.y - glyph.yDirAdj) <= lineTolerance }
            if (line != null) line.glyphs.add(glyph)
            else lines.add(PositionedLine(glyph.yDirAdj, mutableListOf(glyph)))
        }
        buildString {
            lines.forEach { line ->
                val row = StringBuilder()
                var previous: TextPosition? = null
                line.glyphs.sortedBy { it.xDirAdj }.forEach { glyph ->
                    val value = glyph.unicode.orEmpty()
                    if (value.isEmpty()) return@forEach
                    val before = previous
                    if (before != null && value.none { it.isWhitespace() }) {
                        val gap = glyph.xDirAdj - (before.xDirAdj + before.widthDirAdj)
                        val fontSize = max(glyph.fontSizeInPt, before.fontSizeInPt)
                        when {
                            gap > max(6f, fontSize * 1.15f) -> row.append("  |  ")
                            gap > max(1f, fontSize * 0.18f) && row.isNotEmpty() && row.last() != ' ' -> row.append(' ')
                        }
                    }
                    if (value.all { it.isWhitespace() }) {
                        if (row.isNotEmpty() && row.last() != ' ') row.append(' ')
                    } else {
                        row.append(value)
                    }
                    previous = glyph
                }
                val cleaned = row.toString().trim()
                if (cleaned.isNotEmpty()) append(cleaned).append('\n')
            }
        }
    }
}
