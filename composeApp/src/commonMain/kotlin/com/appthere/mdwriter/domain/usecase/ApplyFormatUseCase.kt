package com.appthere.mdwriter.domain.usecase

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.appthere.mdwriter.domain.model.MarkdownFormat

/**
 * Use case for applying Markdown formatting to text
 *
 * Behavior:
 * - No selection: Insert marks and place cursor between them
 * - With selection: Wrap selection and keep it selected
 * - Headings: Insert # at beginning of line(s) containing text
 */
class ApplyFormatUseCase {
    operator fun invoke(
        currentText: TextFieldValue,
        format: MarkdownFormat
    ): TextFieldValue {
        val selection = currentText.selection
        val text = currentText.text

        // Get selected text or use empty string
        val hasSelection = selection.start < selection.end
        val selectedText = if (hasSelection) {
            text.substring(selection.start, selection.end)
        } else {
            ""
        }

        val (newText, newSelection) = when {
            // Heading formats - special handling for line prefixes
            format is MarkdownFormat.Heading1 ||
            format is MarkdownFormat.Heading2 ||
            format is MarkdownFormat.Heading3 ||
            format is MarkdownFormat.Heading4 ||
            format is MarkdownFormat.Heading5 ||
            format is MarkdownFormat.Heading6 -> {
                applyHeadingFormat(text, selection, format)
            }
            // Other block formats (blockquote, lists, code blocks, etc.)
            format.blockPrefix.isNotEmpty() || format.blockSuffix.isNotEmpty() -> {
                applyBlockFormat(text, selection, selectedText, format, hasSelection)
            }
            // Inline formats (bold, italic, strikethrough, etc.)
            else -> {
                applyInlineFormat(text, selection, selectedText, format, hasSelection)
            }
        }

        return currentText.copy(
            text = newText,
            selection = newSelection
        )
    }

    /**
     * Apply heading format to line(s)
     * Inserts # at the beginning of lines containing text
     */
    private fun applyHeadingFormat(
        text: String,
        selection: TextRange,
        format: MarkdownFormat
    ): Pair<String, TextRange> {
        val headingPrefix = format.blockPrefix // e.g., "# ", "## ", etc.

        // Find the start of the line containing the cursor/selection start
        val lineStart = text.lastIndexOf('\n', selection.start - 1).let {
            if (it == -1) 0 else it + 1
        }

        // Find the end of the line containing the cursor/selection end
        val lineEnd = text.indexOf('\n', selection.end).let {
            if (it == -1) text.length else it
        }

        // Get all lines in the selection range
        val linesText = text.substring(lineStart, lineEnd)
        val lines = linesText.split('\n')

        // Apply heading to each non-empty line
        val newLines = lines.map { line ->
            if (line.trim().isNotEmpty()) {
                // Check if already has heading markers
                val trimmedLine = line.trimStart()
                if (trimmedLine.startsWith("#")) {
                    // Already a heading, don't add more
                    line
                } else {
                    // Add heading prefix
                    val leadingSpaces = line.takeWhile { it.isWhitespace() }
                    leadingSpaces + headingPrefix + trimmedLine
                }
            } else {
                line // Keep empty lines as-is
            }
        }

        val newLinesText = newLines.joinToString("\n")
        val newText = text.substring(0, lineStart) + newLinesText + text.substring(lineEnd)

        // Calculate new selection
        val addedChars = newLinesText.length - linesText.length
        val newSelectionStart = selection.start + if (selection.start > lineStart) {
            // Count how many characters were added before selection start
            val beforeSelection = text.substring(lineStart, selection.start)
            val newBeforeSelection = newLinesText.substring(0, selection.start - lineStart + addedChars.coerceAtMost(headingPrefix.length))
            newBeforeSelection.length - beforeSelection.length
        } else {
            0
        }

        val newSelectionEnd = if (selection.start < selection.end) {
            // Keep selection, adjusted for added characters
            selection.end + addedChars
        } else {
            // No selection, place cursor after heading prefix
            lineStart + headingPrefix.length
        }

        return newText to TextRange(newSelectionStart.coerceAtLeast(lineStart), newSelectionEnd)
    }

    /**
     * Apply block format (blockquote, lists, code blocks, horizontal rule)
     */
    private fun applyBlockFormat(
        text: String,
        selection: TextRange,
        selectedText: String,
        format: MarkdownFormat,
        hasSelection: Boolean
    ): Pair<String, TextRange> {
        val isStartOfLine = selection.start == 0 || text.getOrNull(selection.start - 1) == '\n'
        val prefix = if (isStartOfLine || selectedText.isEmpty()) "" else "\n"
        val formattedText = "$prefix${format.blockPrefix}$selectedText${format.blockSuffix}"

        val newText = text.replaceRange(selection.start, selection.end, formattedText)

        val newSelection = if (hasSelection) {
            // Keep text selected, but now it's between the marks
            val newStart = selection.start + prefix.length + format.blockPrefix.length
            val newEnd = newStart + selectedText.length
            TextRange(newStart, newEnd)
        } else {
            // No selection, place cursor between prefix and suffix
            val cursorPos = selection.start + prefix.length + format.blockPrefix.length
            TextRange(cursorPos)
        }

        return newText to newSelection
    }

    /**
     * Apply inline format (bold, italic, code, strikethrough, etc.)
     */
    private fun applyInlineFormat(
        text: String,
        selection: TextRange,
        selectedText: String,
        format: MarkdownFormat,
        hasSelection: Boolean
    ): Pair<String, TextRange> {
        val formattedText = "${format.prefix}$selectedText${format.suffix}"
        val newText = text.replaceRange(selection.start, selection.end, formattedText)

        val newSelection = if (hasSelection) {
            // Keep text selected, but now it's between the marks
            val newStart = selection.start + format.prefix.length
            val newEnd = newStart + selectedText.length
            TextRange(newStart, newEnd)
        } else {
            // No selection, place cursor between opening and closing marks
            val cursorPos = selection.start + format.prefix.length
            TextRange(cursorPos)
        }

        return newText to newSelection
    }
}
