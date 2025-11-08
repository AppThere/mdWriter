package com.appthere.mdwriter.domain.usecase

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.appthere.mdwriter.domain.model.MarkdownFormat

/**
 * Use case for applying Markdown formatting to text
 */
class ApplyFormatUseCase {
    operator fun invoke(
        currentText: TextFieldValue,
        format: MarkdownFormat
    ): TextFieldValue {
        val selection = currentText.selection
        val text = currentText.text

        // Get selected text or use empty string
        val selectedText = if (selection.start < selection.end) {
            text.substring(selection.start, selection.end)
        } else {
            ""
        }

        // Determine if we're at the start of a line for block formats
        val isStartOfLine = selection.start == 0 ||
                           text.getOrNull(selection.start - 1) == '\n'

        val (newText, newCursorPosition) = when {
            // Block format (heading, blockquote, list, etc.)
            format.blockPrefix.isNotEmpty() || format.blockSuffix.isNotEmpty() -> {
                applyBlockFormat(text, selection, selectedText, format, isStartOfLine)
            }
            // Inline format (bold, italic, etc.)
            else -> {
                applyInlineFormat(text, selection, selectedText, format)
            }
        }

        return currentText.copy(
            text = newText,
            selection = TextRange(newCursorPosition)
        )
    }

    private fun applyBlockFormat(
        text: String,
        selection: TextRange,
        selectedText: String,
        format: MarkdownFormat,
        isStartOfLine: Boolean
    ): Pair<String, Int> {
        val prefix = if (isStartOfLine || selectedText.isEmpty()) "" else "\n"
        val formattedText = "$prefix${format.blockPrefix}$selectedText${format.blockSuffix}"

        val newText = text.replaceRange(selection.start, selection.end, formattedText)
        val cursorPosition = selection.start + prefix.length + format.blockPrefix.length + selectedText.length

        return newText to cursorPosition
    }

    private fun applyInlineFormat(
        text: String,
        selection: TextRange,
        selectedText: String,
        format: MarkdownFormat
    ): Pair<String, Int> {
        val formattedText = "${format.prefix}$selectedText${format.suffix}"

        val newText = text.replaceRange(selection.start, selection.end, formattedText)
        val cursorPosition = selection.start + format.prefix.length + selectedText.length

        return newText to cursorPosition
    }
}
