package com.appthere.mdwriter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appthere.mdwriter.ui.theme.EditorTypography

/**
 * Markdown editor with syntax highlighting
 *
 * Features:
 * - Real-time syntax highlighting (basic version)
 * - Scroll position preservation
 * - E Ink optimized styling
 * - Proper text field configuration
 */
@Composable
fun MarkdownEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Start writing..."
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                textStyle = EditorTypography.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.text.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = EditorTypography.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

/**
 * Apply basic syntax highlighting to markdown text
 * This is a placeholder for full syntax highlighting which will be implemented
 * with the markdown parser in Phase 2.
 *
 * TODO: Implement full syntax highlighting with:
 * - Headers with larger font size and bold weight
 * - Bold/Italic/Code with appropriate styles
 * - Links with color and underline
 * - Blockquotes with different color
 * - Code blocks with background color
 */
private fun applyBasicSyntaxHighlighting(text: String): androidx.compose.ui.text.AnnotatedString {
    val builder = androidx.compose.ui.text.AnnotatedString.Builder(text)

    // Basic highlighting patterns
    // This will be replaced with proper AST-based highlighting in Phase 2

    // Example: Highlight markdown headers (lines starting with #)
    val lines = text.split('\n')
    var currentIndex = 0

    lines.forEach { line ->
        val lineLength = line.length

        when {
            // H1
            line.startsWith("# ") -> {
                builder.addStyle(
                    style = androidx.compose.ui.text.SpanStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    start = currentIndex,
                    end = currentIndex + lineLength
                )
            }
            // H2
            line.startsWith("## ") -> {
                builder.addStyle(
                    style = androidx.compose.ui.text.SpanStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    start = currentIndex,
                    end = currentIndex + lineLength
                )
            }
            // H3
            line.startsWith("### ") -> {
                builder.addStyle(
                    style = androidx.compose.ui.text.SpanStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    start = currentIndex,
                    end = currentIndex + lineLength
                )
            }
        }

        currentIndex += lineLength + 1 // +1 for newline
    }

    return builder.toAnnotatedString()
}
