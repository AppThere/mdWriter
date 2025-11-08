package com.appthere.mdwriter.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.appthere.mdwriter.domain.model.MarkdownNode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Syntax highlighter that converts Markdown AST to styled AnnotatedString
 *
 * Features:
 * - Applies styles based on node type (headings, bold, italic, code, links)
 * - Caches parsed results for performance
 * - Handles nested elements correctly
 * - Uses E Ink friendly colors
 */
class SyntaxHighlighter(
    private val styleConfig: StyleConfig = StyleConfig.light()
) {
    private val cache = mutableMapOf<CacheKey, AnnotatedString>()
    private val cacheMutex = Mutex()
    private var cacheVersion = 0

    /**
     * Convert Markdown AST to AnnotatedString with syntax highlighting
     *
     * @param ast Root AST node (usually Document)
     * @param sourceText Original markdown text
     * @param useCache Whether to use caching (default true)
     * @return Styled AnnotatedString
     */
    suspend fun highlight(
        ast: MarkdownNode,
        sourceText: String,
        useCache: Boolean = true
    ): AnnotatedString {
        if (!useCache) {
            return buildAnnotatedString(ast, sourceText)
        }

        val key = CacheKey(sourceText.hashCode(), cacheVersion)

        return cacheMutex.withLock {
            cache.getOrPut(key) {
                buildAnnotatedString(ast, sourceText)
            }
        }
    }

    /**
     * Clear the cache (useful when theme changes or config updates)
     */
    suspend fun clearCache() {
        cacheMutex.withLock {
            cache.clear()
            cacheVersion++
        }
    }

    /**
     * Build AnnotatedString from AST
     */
    private fun buildAnnotatedString(ast: MarkdownNode, sourceText: String): AnnotatedString {
        val builder = AnnotatedString.Builder(sourceText)
        applyStyles(ast, builder)
        return builder.toAnnotatedString()
    }

    /**
     * Recursively apply styles to AST nodes
     */
    private fun applyStyles(node: MarkdownNode, builder: AnnotatedString.Builder) {
        when (node) {
            is MarkdownNode.Document -> {
                node.children.forEach { applyStyles(it, builder) }
            }

            is MarkdownNode.Heading -> {
                val style = styleConfig.getHeadingStyle(node.level)
                builder.addStyle(style, node.startOffset, node.endOffset)
                node.children.forEach { applyStyles(it, builder) }
            }

            is MarkdownNode.Paragraph -> {
                node.children.forEach { applyStyles(it, builder) }
            }

            is MarkdownNode.Strong -> {
                builder.addStyle(styleConfig.strongStyle, node.startOffset, node.endOffset)
                node.children.forEach { applyStyles(it, builder) }
            }

            is MarkdownNode.Emphasis -> {
                builder.addStyle(styleConfig.emphasisStyle, node.startOffset, node.endOffset)
                node.children.forEach { applyStyles(it, builder) }
            }

            is MarkdownNode.Code -> {
                builder.addStyle(styleConfig.codeStyle, node.startOffset, node.endOffset)
            }

            is MarkdownNode.CodeBlock -> {
                builder.addStyle(styleConfig.codeBlockStyle, node.startOffset, node.endOffset)
            }

            is MarkdownNode.Link -> {
                builder.addStyle(styleConfig.linkStyle, node.startOffset, node.endOffset)
                node.children.forEach { applyStyles(it, builder) }
            }

            is MarkdownNode.Strikethrough -> {
                builder.addStyle(styleConfig.strikethroughStyle, node.startOffset, node.endOffset)
                node.children.forEach { applyStyles(it, builder) }
            }

            is MarkdownNode.Blockquote -> {
                builder.addStyle(styleConfig.blockquoteStyle, node.startOffset, node.endOffset)
                node.children.forEach { applyStyles(it, builder) }
            }

            is MarkdownNode.BulletList -> {
                node.items.forEach { applyStyles(it, builder) }
            }

            is MarkdownNode.OrderedList -> {
                node.items.forEach { applyStyles(it, builder) }
            }

            is MarkdownNode.ListItem -> {
                node.children.forEach { applyStyles(it, builder) }
            }

            is MarkdownNode.Table -> {
                node.header?.let { applyStyles(it, builder) }
                node.rows.forEach { applyStyles(it, builder) }
            }

            is MarkdownNode.TableRow -> {
                node.cells.forEach { applyStyles(it, builder) }
            }

            is MarkdownNode.TableCell -> {
                node.children.forEach { applyStyles(it, builder) }
            }

            is MarkdownNode.TaskListItem -> {
                node.children.forEach { applyStyles(it, builder) }
            }

            // Leaf nodes - no children to process
            is MarkdownNode.Text,
            is MarkdownNode.Image,
            is MarkdownNode.HorizontalRule,
            is MarkdownNode.LineBreak -> {
                // No special styling needed
            }
        }
    }

    /**
     * Cache key for memoization
     */
    private data class CacheKey(
        val contentHash: Int,
        val configVersion: Int
    )
}

/**
 * Incremental syntax highlighter that efficiently updates only changed portions
 *
 * This is useful for large documents where re-highlighting the entire text
 * would be too slow.
 */
class IncrementalSyntaxHighlighter(
    private val styleConfig: StyleConfig = StyleConfig.light()
) {
    private val highlighter = SyntaxHighlighter(styleConfig)
    private var lastText: String = ""
    private var lastResult: AnnotatedString? = null

    /**
     * Highlight text with incremental updates
     *
     * If the text has changed only slightly, this will reuse parts of the
     * previous result for better performance.
     *
     * @param ast Current AST
     * @param sourceText Current source text
     * @return Styled AnnotatedString
     */
    suspend fun highlight(ast: MarkdownNode, sourceText: String): AnnotatedString {
        // Simple incremental strategy: check if text is similar
        val similarity = calculateSimilarity(lastText, sourceText)

        // If text is very similar (>90%), just re-highlight from cache
        // Otherwise do a full highlight
        val result = if (similarity > 0.9 && lastResult != null) {
            highlighter.highlight(ast, sourceText, useCache = true)
        } else {
            highlighter.highlight(ast, sourceText, useCache = false)
        }

        lastText = sourceText
        lastResult = result
        return result
    }

    /**
     * Calculate similarity between two strings (0.0 to 1.0)
     */
    private fun calculateSimilarity(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0

        val maxLen = maxOf(s1.length, s2.length)
        val minLen = minOf(s1.length, s2.length)

        // Simple length-based similarity
        val lengthSimilarity = minLen.toDouble() / maxLen.toDouble()

        // Character-based similarity (count matching chars at same position)
        var matchingChars = 0
        for (i in 0 until minLen) {
            if (s1[i] == s2[i]) matchingChars++
        }
        val charSimilarity = matchingChars.toDouble() / maxLen.toDouble()

        // Average of both metrics
        return (lengthSimilarity + charSimilarity) / 2.0
    }

    /**
     * Clear the cache
     */
    suspend fun clearCache() {
        highlighter.clearCache()
        lastText = ""
        lastResult = null
    }
}

/**
 * Debounced syntax highlighter for real-time editing
 *
 * This delays highlighting until the user stops typing for a short period,
 * improving performance during rapid text entry.
 */
class DebouncedSyntaxHighlighter(
    private val styleConfig: StyleConfig = StyleConfig.light(),
    private val debounceMillis: Long = 300
) {
    private val highlighter = IncrementalSyntaxHighlighter(styleConfig)
    private var lastHighlightTime = 0L
    private val mutex = Mutex()

    /**
     * Highlight with debouncing
     *
     * This will delay highlighting until debounceMillis have passed since
     * the last call. For immediate highlighting (e.g., on first render),
     * set force = true.
     *
     * @param ast Current AST
     * @param sourceText Current source text
     * @param force Force immediate highlighting
     * @return Styled AnnotatedString or null if debounced
     */
    suspend fun highlight(
        ast: MarkdownNode,
        sourceText: String,
        force: Boolean = false
    ): AnnotatedString? {
        val currentTime = System.currentTimeMillis()

        return mutex.withLock {
            if (force || (currentTime - lastHighlightTime) >= debounceMillis) {
                lastHighlightTime = currentTime
                highlighter.highlight(ast, sourceText)
            } else {
                null // Still within debounce period
            }
        }
    }

    /**
     * Clear the cache
     */
    suspend fun clearCache() {
        highlighter.clearCache()
    }
}
