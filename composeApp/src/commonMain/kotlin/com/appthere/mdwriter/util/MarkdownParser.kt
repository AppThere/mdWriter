package com.appthere.mdwriter.util

import com.appthere.mdwriter.domain.model.MarkdownNode
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser as IntellijMarkdownParser

/**
 * Parser for Markdown content with support for:
 * - CommonMark with GFM (GitHub Flavored Markdown) extensions
 * - Hugo-style YAML frontmatter
 * - CSS class annotations {.class-name}
 *
 * Uses org.jetbrains:markdown library as foundation.
 */
class MarkdownParser {

    private val flavour = GFMFlavourDescriptor()
    private val parser = IntellijMarkdownParser(flavour)
    private val frontmatterParser = FrontmatterParser()

    /**
     * CSS class annotation pattern: {.class-name} or {.class1 .class2}
     */
    private val cssClassPattern = Regex("""\{\.([a-zA-Z0-9_\-]+(?:\s+\.[a-zA-Z0-9_\-]+)*)\}""")

    /**
     * Parse Markdown content to AST
     *
     * @param content Markdown content (may include frontmatter)
     * @return ParseResult containing AST, frontmatter, and content without frontmatter
     */
    fun parse(content: String): ParseResult {
        // Extract frontmatter first
        val (frontmatter, contentWithoutFrontmatter) = frontmatterParser.extractFrontmatter(content)

        // Parse markdown to AST
        val tree = parser.buildMarkdownTreeFromString(contentWithoutFrontmatter)
        val rootNode = convertToMarkdownNode(tree, contentWithoutFrontmatter)

        return ParseResult(
            ast = rootNode,
            frontmatter = frontmatter,
            content = contentWithoutFrontmatter
        )
    }

    /**
     * Convert intellij-markdown AST to our MarkdownNode AST
     */
    private fun convertToMarkdownNode(node: ASTNode, sourceText: String): MarkdownNode {
        val text = getNodeText(node, sourceText)
        val startOffset = node.startOffset
        val endOffset = node.endOffset

        // Extract CSS classes from the node's text if it's a block element
        val (cleanText, cssClasses) = extractCssClasses(text, node.type)

        return when (node.type) {
            MarkdownElementTypes.MARKDOWN_FILE -> {
                val children = node.children.map { convertToMarkdownNode(it, sourceText) }
                MarkdownNode.Document(children, startOffset, endOffset)
            }

            MarkdownElementTypes.ATX_1 -> parseHeading(node, sourceText, 1, cssClasses)
            MarkdownElementTypes.ATX_2 -> parseHeading(node, sourceText, 2, cssClasses)
            MarkdownElementTypes.ATX_3 -> parseHeading(node, sourceText, 3, cssClasses)
            MarkdownElementTypes.ATX_4 -> parseHeading(node, sourceText, 4, cssClasses)
            MarkdownElementTypes.ATX_5 -> parseHeading(node, sourceText, 5, cssClasses)
            MarkdownElementTypes.ATX_6 -> parseHeading(node, sourceText, 6, cssClasses)

            MarkdownElementTypes.PARAGRAPH -> {
                val children = node.children.map { convertToMarkdownNode(it, sourceText) }
                MarkdownNode.Paragraph(children, startOffset, endOffset, cssClasses)
            }

            MarkdownElementTypes.UNORDERED_LIST -> {
                val items = node.children
                    .filter { it.type == MarkdownElementTypes.LIST_ITEM }
                    .map { convertToMarkdownNode(it, sourceText) as MarkdownNode.ListItem }
                MarkdownNode.BulletList(items, startOffset, endOffset, cssClasses)
            }

            MarkdownElementTypes.ORDERED_LIST -> {
                val items = node.children
                    .filter { it.type == MarkdownElementTypes.LIST_ITEM }
                    .map { convertToMarkdownNode(it, sourceText) as MarkdownNode.ListItem }
                MarkdownNode.OrderedList(1, items, startOffset, endOffset, cssClasses)
            }

            MarkdownElementTypes.LIST_ITEM -> {
                val children = node.children
                    .filter { it.type != MarkdownTokenTypes.LIST_BULLET && it.type != MarkdownTokenTypes.LIST_NUMBER }
                    .map { convertToMarkdownNode(it, sourceText) }
                MarkdownNode.ListItem(children, startOffset, endOffset, cssClasses)
            }

            MarkdownElementTypes.BLOCK_QUOTE -> {
                val children = node.children
                    .filter { it.type != MarkdownTokenTypes.BLOCK_QUOTE }
                    .map { convertToMarkdownNode(it, sourceText) }
                MarkdownNode.Blockquote(children, startOffset, endOffset, cssClasses)
            }

            MarkdownElementTypes.CODE_FENCE -> {
                val language = findLanguage(node, sourceText)
                val codeContent = extractCodeFenceContent(node, sourceText)
                MarkdownNode.CodeBlock(codeContent, language, startOffset, endOffset, cssClasses)
            }

            MarkdownElementTypes.CODE_BLOCK -> {
                MarkdownNode.CodeBlock(cleanText, null, startOffset, endOffset, cssClasses)
            }

            MarkdownElementTypes.CODE_SPAN -> {
                val codeContent = extractCodeSpanContent(node, sourceText)
                MarkdownNode.Code(codeContent, startOffset, endOffset)
            }

            MarkdownElementTypes.INLINE_LINK -> {
                parseLink(node, sourceText)
            }

            MarkdownElementTypes.IMAGE -> {
                parseImage(node, sourceText)
            }

            MarkdownElementTypes.STRONG -> {
                val children = node.children
                    .filter { it.type != MarkdownTokenTypes.EMPH }
                    .map { convertToMarkdownNode(it, sourceText) }
                MarkdownNode.Strong(children, startOffset, endOffset)
            }

            MarkdownElementTypes.EMPH -> {
                val children = node.children
                    .filter { it.type != MarkdownTokenTypes.EMPH }
                    .map { convertToMarkdownNode(it, sourceText) }
                MarkdownNode.Emphasis(children, startOffset, endOffset)
            }

            MarkdownTokenTypes.TEXT -> {
                MarkdownNode.Text(text, startOffset, endOffset)
            }

            MarkdownTokenTypes.EOL -> {
                MarkdownNode.LineBreak(startOffset, endOffset)
            }

            MarkdownTokenTypes.HORIZONTAL_RULE -> {
                MarkdownNode.HorizontalRule(startOffset, endOffset, cssClasses)
            }

            // GFM extensions
            org.intellij.markdown.flavours.gfm.GFMElementTypes.STRIKETHROUGH -> {
                val children = node.children
                    .filter { it.type != org.intellij.markdown.flavours.gfm.GFMTokenTypes.TILDE }
                    .map { convertToMarkdownNode(it, sourceText) }
                MarkdownNode.Strikethrough(children, startOffset, endOffset)
            }

            org.intellij.markdown.flavours.gfm.GFMElementTypes.TABLE -> {
                parseTable(node, sourceText)
            }

            else -> {
                // For unhandled types, try to parse children or return text
                if (node.children.isNotEmpty()) {
                    val children = node.children.map { convertToMarkdownNode(it, sourceText) }
                    // Return a paragraph as a generic container
                    MarkdownNode.Paragraph(children, startOffset, endOffset)
                } else {
                    MarkdownNode.Text(text, startOffset, endOffset)
                }
            }
        }
    }

    /**
     * Extract CSS class annotations from text
     *
     * @return Pair of (text without CSS classes, list of CSS classes)
     */
    private fun extractCssClasses(text: String, nodeType: IElementType): Pair<String, List<String>> {
        // Only extract CSS classes from block elements
        val isBlockElement = when (nodeType) {
            MarkdownElementTypes.ATX_1, MarkdownElementTypes.ATX_2, MarkdownElementTypes.ATX_3,
            MarkdownElementTypes.ATX_4, MarkdownElementTypes.ATX_5, MarkdownElementTypes.ATX_6,
            MarkdownElementTypes.PARAGRAPH, MarkdownElementTypes.BLOCK_QUOTE,
            MarkdownElementTypes.CODE_FENCE, MarkdownElementTypes.CODE_BLOCK -> true
            //MarkdownElementTypes.HORIZONTAL_RULE -> true
            else -> false
        }

        if (!isBlockElement) {
            return text to emptyList()
        }

        val matches = cssClassPattern.findAll(text)
        val classes = matches.flatMap { match ->
            val classString = match.groupValues[1]
            classString.split(Regex("\\s+\\.")).map { it.trim() }
        }.toList()

        val cleanText = cssClassPattern.replace(text, "").trim()

        return cleanText to classes
    }

    private fun parseHeading(node: ASTNode, sourceText: String, level: Int, cssClasses: List<String>): MarkdownNode.Heading {
        val children = node.children
            .filter { it.type != MarkdownTokenTypes.ATX_HEADER && it.type != MarkdownTokenTypes.ATX_CONTENT }
            .map { convertToMarkdownNode(it, sourceText) }

        // Extract text content
        val textContent = node.children
            .firstOrNull { it.type == MarkdownTokenTypes.ATX_CONTENT }
            ?.let { getNodeText(it, sourceText) }?.trim() ?: ""

        // Remove CSS classes from text
        val (cleanText, _) = extractCssClasses(textContent, node.type)

        return MarkdownNode.Heading(
            level = level,
            text = cleanText,
            children = children,
            startOffset = node.startOffset,
            endOffset = node.endOffset,
            cssClasses = cssClasses
        )
    }

    private fun parseLink(node: ASTNode, sourceText: String): MarkdownNode.Link {
        val linkText = node.children
            .firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT }
            ?.children
            ?.map { convertToMarkdownNode(it, sourceText) } ?: emptyList()

        val destination = node.children
            .firstOrNull { it.type == MarkdownElementTypes.LINK_DESTINATION }
            ?.let { getNodeText(it, sourceText) } ?: ""

        val title = node.children
            .firstOrNull { it.type == MarkdownElementTypes.LINK_TITLE }
            ?.let { getNodeText(it, sourceText) }

        return MarkdownNode.Link(
            destination = destination,
            title = title,
            children = linkText,
            startOffset = node.startOffset,
            endOffset = node.endOffset
        )
    }

    private fun parseImage(node: ASTNode, sourceText: String): MarkdownNode.Image {
        val altText = node.children
            .firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT }
            ?.let { getNodeText(it, sourceText) } ?: ""

        val destination = node.children
            .firstOrNull { it.type == MarkdownElementTypes.LINK_DESTINATION }
            ?.let { getNodeText(it, sourceText) } ?: ""

        val title = node.children
            .firstOrNull { it.type == MarkdownElementTypes.LINK_TITLE }
            ?.let { getNodeText(it, sourceText) }

        return MarkdownNode.Image(
            destination = destination,
            title = title,
            altText = altText,
            startOffset = node.startOffset,
            endOffset = node.endOffset
        )
    }

    private fun parseTable(node: ASTNode, sourceText: String): MarkdownNode.Table {
        val rows = node.children
            .filter { it.type == org.intellij.markdown.flavours.gfm.GFMElementTypes.ROW }

        val header = rows.firstOrNull()?.let { parseTableRow(it, sourceText, isHeader = true) }
        val bodyRows = rows.drop(1).map { parseTableRow(it, sourceText, isHeader = false) }

        return MarkdownNode.Table(
            header = header,
            rows = bodyRows,
            startOffset = node.startOffset,
            endOffset = node.endOffset
        )
    }

    private fun parseTableRow(node: ASTNode, sourceText: String, isHeader: Boolean): MarkdownNode.TableRow {
        val cells = node.children
            .filter { it.type == org.intellij.markdown.flavours.gfm.GFMTokenTypes.CELL }
            .map { parseTableCell(it, sourceText) }

        return MarkdownNode.TableRow(
            cells = cells,
            startOffset = node.startOffset,
            endOffset = node.endOffset
        )
    }

    private fun parseTableCell(node: ASTNode, sourceText: String): MarkdownNode.TableCell {
        val children = node.children.map { convertToMarkdownNode(it, sourceText) }

        return MarkdownNode.TableCell(
            children = children,
            alignment = MarkdownNode.CellAlignment.NONE,
            startOffset = node.startOffset,
            endOffset = node.endOffset
        )
    }

    private fun findLanguage(node: ASTNode, sourceText: String): String? {
        // Find fence language in children - it's usually plain text after the fence marker
        val codeBlock = getNodeText(node, sourceText)
        val firstLine = codeBlock.lines().firstOrNull() ?: return null
        val lang = firstLine.removePrefix("```").trim()
        return if (lang.isNotEmpty()) lang else null
    }

    private fun extractCodeFenceContent(node: ASTNode, sourceText: String): String {
        // Extract content between fence markers
        val fullText = getNodeText(node, sourceText)
        val lines = fullText.lines()
        if (lines.size < 2) return ""

        // Remove first line (```lang) and last line (```)
        val contentLines = lines.drop(1).dropLast(1)
        return contentLines.joinToString("\n")
    }

    private fun extractCodeSpanContent(node: ASTNode, sourceText: String): String {
        return node.children
            .filter { it.type != MarkdownTokenTypes.BACKTICK }
            .joinToString("") { getNodeText(it, sourceText) }
    }

    /**
     * Helper to extract text from an AST node
     */
    private fun getNodeText(node: ASTNode, sourceText: String): String {
        return sourceText.substring(node.startOffset, node.endOffset)
    }
}

/**
 * Result of parsing Markdown content
 */
data class ParseResult(
    /**
     * Root AST node
     */
    val ast: MarkdownNode,

    /**
     * Parsed YAML frontmatter as map
     */
    val frontmatter: Map<String, Any>,

    /**
     * Content without frontmatter
     */
    val content: String
)
