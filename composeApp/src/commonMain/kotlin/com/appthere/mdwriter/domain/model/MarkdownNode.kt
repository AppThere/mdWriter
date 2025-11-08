package com.appthere.mdwriter.domain.model

/**
 * AST (Abstract Syntax Tree) node representing parsed Markdown content
 *
 * This sealed class hierarchy represents the structure of a Markdown document
 * after parsing, including support for CSS class annotations.
 */
sealed class MarkdownNode {
    /**
     * Source position information for this node
     */
    abstract val startOffset: Int
    abstract val endOffset: Int

    /**
     * CSS classes attached to this node via {.class-name} annotations
     */
    abstract val cssClasses: List<String>

    /**
     * Document root node containing all top-level blocks
     */
    data class Document(
        val children: List<MarkdownNode>,
        override val startOffset: Int = 0,
        override val endOffset: Int = 0,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Heading node (H1-H6)
     */
    data class Heading(
        val level: Int, // 1-6
        val text: String,
        val children: List<MarkdownNode> = emptyList(),
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode() {
        init {
            require(level in 1..6) { "Heading level must be between 1 and 6" }
        }
    }

    /**
     * Paragraph node
     */
    data class Paragraph(
        val children: List<MarkdownNode>,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Text node (leaf node containing actual text)
     */
    data class Text(
        val content: String,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Strong emphasis (bold) node
     */
    data class Strong(
        val children: List<MarkdownNode>,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Emphasis (italic) node
     */
    data class Emphasis(
        val children: List<MarkdownNode>,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Inline code node
     */
    data class Code(
        val content: String,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Code block node
     */
    data class CodeBlock(
        val content: String,
        val language: String? = null,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Link node
     */
    data class Link(
        val destination: String,
        val title: String? = null,
        val children: List<MarkdownNode>,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Image node
     */
    data class Image(
        val destination: String,
        val title: String? = null,
        val altText: String,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Unordered list node
     */
    data class BulletList(
        val items: List<ListItem>,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Ordered list node
     */
    data class OrderedList(
        val startNumber: Int = 1,
        val items: List<ListItem>,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * List item node
     */
    data class ListItem(
        val children: List<MarkdownNode>,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Blockquote node
     */
    data class Blockquote(
        val children: List<MarkdownNode>,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Horizontal rule node
     */
    data class HorizontalRule(
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Line break node
     */
    data class LineBreak(
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Strikethrough node
     */
    data class Strikethrough(
        val children: List<MarkdownNode>,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Table node
     */
    data class Table(
        val header: TableRow?,
        val rows: List<TableRow>,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Table row node
     */
    data class TableRow(
        val cells: List<TableCell>,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Table cell node
     */
    data class TableCell(
        val children: List<MarkdownNode>,
        val alignment: CellAlignment = CellAlignment.NONE,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()

    /**
     * Cell alignment in tables
     */
    enum class CellAlignment {
        NONE, LEFT, CENTER, RIGHT
    }

    /**
     * Task list item (GitHub-style)
     */
    data class TaskListItem(
        val checked: Boolean,
        val children: List<MarkdownNode>,
        override val startOffset: Int,
        override val endOffset: Int,
        override val cssClasses: List<String> = emptyList()
    ) : MarkdownNode()
}

/**
 * Helper functions for working with MarkdownNode trees
 */
object MarkdownNodeUtil {
    /**
     * Extract all text content from a node tree
     */
    fun extractText(node: MarkdownNode): String {
        return when (node) {
            is MarkdownNode.Text -> node.content
            is MarkdownNode.Code -> node.content
            is MarkdownNode.CodeBlock -> node.content
            is MarkdownNode.Heading -> node.children.joinToString("") { extractText(it) }
            is MarkdownNode.Paragraph -> node.children.joinToString("") { extractText(it) }
            is MarkdownNode.Strong -> node.children.joinToString("") { extractText(it) }
            is MarkdownNode.Emphasis -> node.children.joinToString("") { extractText(it) }
            is MarkdownNode.Link -> node.children.joinToString("") { extractText(it) }
            is MarkdownNode.Strikethrough -> node.children.joinToString("") { extractText(it) }
            is MarkdownNode.ListItem -> node.children.joinToString("") { extractText(it) }
            is MarkdownNode.Blockquote -> node.children.joinToString("") { extractText(it) }
            is MarkdownNode.TableCell -> node.children.joinToString("") { extractText(it) }
            is MarkdownNode.TaskListItem -> node.children.joinToString("") { extractText(it) }
            is MarkdownNode.Document -> node.children.joinToString("\n") { extractText(it) }
            is MarkdownNode.BulletList -> node.items.joinToString("\n") { extractText(it) }
            is MarkdownNode.OrderedList -> node.items.joinToString("\n") { extractText(it) }
            is MarkdownNode.Table -> {
                val headerText = node.header?.let { extractText(it) + "\n" } ?: ""
                headerText + node.rows.joinToString("\n") { extractText(it) }
            }
            is MarkdownNode.TableRow -> node.cells.joinToString(" | ") { extractText(it) }
            is MarkdownNode.Image -> node.altText
            is MarkdownNode.HorizontalRule, is MarkdownNode.LineBreak -> ""
        }
    }

    /**
     * Find all nodes of a specific type in a tree
     */
    inline fun <reified T : MarkdownNode> findNodes(node: MarkdownNode): List<T> {
        val results = mutableListOf<T>()
        if (node is T) {
            results.add(node)
        }

        when (node) {
            is MarkdownNode.Document -> node.children.forEach { results.addAll(findNodes(it)) }
            is MarkdownNode.Heading -> node.children.forEach { results.addAll(findNodes(it)) }
            is MarkdownNode.Paragraph -> node.children.forEach { results.addAll(findNodes(it)) }
            is MarkdownNode.Strong -> node.children.forEach { results.addAll(findNodes(it)) }
            is MarkdownNode.Emphasis -> node.children.forEach { results.addAll(findNodes(it)) }
            is MarkdownNode.Link -> node.children.forEach { results.addAll(findNodes(it)) }
            is MarkdownNode.Strikethrough -> node.children.forEach { results.addAll(findNodes(it)) }
            is MarkdownNode.ListItem -> node.children.forEach { results.addAll(findNodes(it)) }
            is MarkdownNode.Blockquote -> node.children.forEach { results.addAll(findNodes(it)) }
            is MarkdownNode.BulletList -> node.items.forEach { results.addAll(findNodes(it)) }
            is MarkdownNode.OrderedList -> node.items.forEach { results.addAll(findNodes(it)) }
            is MarkdownNode.Table -> {
                node.header?.let { results.addAll(findNodes(it)) }
                node.rows.forEach { results.addAll(findNodes(it)) }
            }
            is MarkdownNode.TableRow -> node.cells.forEach { results.addAll(findNodes(it)) }
            is MarkdownNode.TableCell -> node.children.forEach { results.addAll(findNodes(it)) }
            is MarkdownNode.TaskListItem -> node.children.forEach { results.addAll(findNodes(it)) }
            else -> { /* Leaf nodes - no children to traverse */ }
        }

        return results
    }
}
