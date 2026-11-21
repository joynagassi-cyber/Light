package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.example.ui.theme.LightSpacing
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text as MarkdownText
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser

private const val MAX_RENDERED_MARKDOWN = 32_768
private const val MAX_MARKDOWN_DEPTH = 16
private val markdownParser = Parser.builder().build()

internal sealed interface AgentMarkdownBlock {
    data class Text(val text: AnnotatedString, val heading: Int? = null, val indent: Int = 0) : AgentMarkdownBlock
    data class Code(val text: String, val language: String?) : AgentMarkdownBlock
    data object Rule : AgentMarkdownBlock
}

private fun Node.children(): Sequence<Node> = generateSequence(firstChild) { it.next }

private fun AnnotatedString.Builder.appendInline(node: Node, depth: Int) {
    if (depth >= MAX_MARKDOWN_DEPTH) return
    when (node) {
        is MarkdownText -> append(node.literal)
        is Code -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append(node.literal) }
        is StrongEmphasis -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { node.children().forEach { appendInline(it, depth + 1) } }
        is Emphasis -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { node.children().forEach { appendInline(it, depth + 1) } }
        // Links and images are labels only: this renderer never opens a URI or fetches a remote image.
        is Link -> withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) { node.children().forEach { appendInline(it, depth + 1) } }
        is Image -> { append("[Image : "); node.children().forEach { appendInline(it, depth + 1) }; append("]") }
        is SoftLineBreak -> append('\n')
        is HardLineBreak -> append('\n')
        is HtmlInline -> append(node.literal)
        else -> node.children().forEach { appendInline(it, depth + 1) }
    }
}

internal fun parseAgentMarkdown(input: String): List<AgentMarkdownBlock> = buildList {
    fun addNode(node: Node, depth: Int, indent: Int = 0, prefix: String = "") {
        if (depth >= MAX_MARKDOWN_DEPTH) return
        when (node) {
            is FencedCodeBlock -> add(AgentMarkdownBlock.Code(node.literal, node.info.orEmpty().substringBefore(' ').take(24).ifBlank { null }))
            is IndentedCodeBlock -> add(AgentMarkdownBlock.Code(node.literal, null))
            is ThematicBreak -> add(AgentMarkdownBlock.Rule)
            is HtmlBlock -> add(AgentMarkdownBlock.Text(AnnotatedString(node.literal.trimEnd()), indent = indent))
            is Heading, is Paragraph -> add(AgentMarkdownBlock.Text(buildAnnotatedString {
                append(prefix); node.children().forEach { appendInline(it, 0) }
            }, heading = (node as? Heading)?.level, indent = indent))
            is BlockQuote -> node.children().forEach { addNode(it, depth + 1, indent + 1, "› ") }
            is BulletList, is OrderedList -> node.children().forEachIndexed { index, item ->
                val marker = if (node is OrderedList) "${(node.markerStartNumber ?: 1).toLong() + index}. " else "• "
                item.children().forEachIndexed { partIndex, child ->
                    addNode(child, depth + 1, indent + 1, if (partIndex == 0) marker else "")
                }
            }
            else -> node.children().forEach { addNode(it, depth + 1, indent, prefix) }
        }
    }
    val plain = input.take(MAX_RENDERED_MARKDOWN)
    addNode(markdownParser.parse(plain), 0)
    if (input.length > MAX_RENDERED_MARKDOWN) add(AgentMarkdownBlock.Text(AnnotatedString("Affichage limité : le message complet est conservé dans l’historique.")))
}

@Composable
internal fun AgentMarkdown(text: String, modifier: Modifier = Modifier) {
    val blocks = remember(text) { parseAgentMarkdown(text) }
    SelectionContainer(modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(LightSpacing.one)) {
            blocks.forEachIndexed { index, block ->
                when (block) {
                    is AgentMarkdownBlock.Text -> {
                        val style = when (block.heading) {
                            1, 2 -> MaterialTheme.typography.titleLarge
                            3, 4, 5, 6 -> MaterialTheme.typography.titleMedium
                            else -> MaterialTheme.typography.bodyMedium
                        }
                        Text(block.text, style = style, color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = LightSpacing.one * block.indent.coerceAtMost(4))
                                .then(if (block.heading != null) Modifier.semantics { heading() } else Modifier))
                    }
                    is AgentMarkdownBlock.Code -> AgentCodeBlock(block, index)
                    AgentMarkdownBlock.Rule -> HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun AgentCodeBlock(block: AgentMarkdownBlock.Code, index: Int) {
    val clipboard = LocalClipboardManager.current
    var copied by remember(block.text) { mutableStateOf(false) }
    LightPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(LightSpacing.one)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(block.language ?: "Code", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                TextButton(onClick = { clipboard.setText(AnnotatedString(block.text)); copied = true },
                    modifier = Modifier.testTag("copy_agent_code_$index")) { Text(if (copied) "Copié" else "Copier") }
            }
            Text(block.text, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()))
        }
    }
}
