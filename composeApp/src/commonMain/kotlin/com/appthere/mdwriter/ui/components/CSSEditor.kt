package com.appthere.mdwriter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.appthere.mdwriter.ui.theme.*

/**
 * CSS Editor with syntax highlighting, line numbers, and basic autocomplete
 * Optimized for E Ink displays
 */
@Composable
fun CSSEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    placeholder: String = "/* Enter CSS here */"
) {
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(value))
    }

    var showAutocomplete by remember { mutableStateOf(false) }
    var autocompletePosition by remember { mutableStateOf(0) }
    var autocompleteSuggestions by remember { mutableStateOf(emptyList<String>()) }

    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    // Update textFieldValue when external value changes
    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            textFieldValue = TextFieldValue(value)
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with line count
            EditorHeader(
                lineCount = textFieldValue.text.lines().size,
                charCount = textFieldValue.text.length
            )

            HorizontalDivider()

            // Editor area with line numbers
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // Line numbers
                LineNumbers(
                    lineCount = textFieldValue.text.lines().size,
                    scrollState = scrollState,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                )

                VerticalDivider()

                // Text editor
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    val highlightedText = remember(textFieldValue.text) {
                        highlightCSS(textFieldValue.text)
                    }

                    BasicTextField(
                        value = textFieldValue.copy(annotatedString = highlightedText),
                        onValueChange = { newValue ->
                            textFieldValue = newValue
                            onValueChange(newValue.text)

                            // Check for autocomplete triggers
                            val cursorPos = newValue.selection.start
                            val beforeCursor = newValue.text.substring(0, cursorPos)
                            if (beforeCursor.endsWith(":") || beforeCursor.endsWith(" ")) {
                                val suggestions = getAutocompleteSuggestions(beforeCursor)
                                if (suggestions.isNotEmpty()) {
                                    showAutocomplete = true
                                    autocompletePosition = cursorPos
                                    autocompleteSuggestions = suggestions
                                }
                            } else {
                                showAutocomplete = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .verticalScroll(scrollState)
                            .horizontalScroll(horizontalScrollState)
                            .focusRequester(focusRequester)
                            .onKeyEvent { keyEvent ->
                                handleKeyEvent(
                                    keyEvent = keyEvent,
                                    textFieldValue = textFieldValue,
                                    onValueChange = { newValue ->
                                        textFieldValue = newValue
                                        onValueChange(newValue.text)
                                    }
                                )
                            }
                            .semantics {
                                contentDescription = "CSS code editor with syntax highlighting"
                            },
                        textStyle = TextStyle(
                            fontFamily = MonospaceFont,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        readOnly = readOnly,
                        decorationBox = { innerTextField ->
                            if (textFieldValue.text.isEmpty() && !readOnly) {
                                Text(
                                    text = placeholder,
                                    style = TextStyle(
                                        fontFamily = MonospaceFont,
                                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )

                    // Autocomplete popup
                    if (showAutocomplete && autocompleteSuggestions.isNotEmpty()) {
                        AutocompletePopup(
                            suggestions = autocompleteSuggestions,
                            onSuggestionSelected = { suggestion ->
                                val newText = textFieldValue.text.substring(0, autocompletePosition) +
                                        suggestion +
                                        textFieldValue.text.substring(autocompletePosition)
                                textFieldValue = TextFieldValue(
                                    text = newText,
                                    selection = androidx.compose.ui.text.TextRange(
                                        autocompletePosition + suggestion.length
                                    )
                                )
                                onValueChange(newText)
                                showAutocomplete = false
                            },
                            onDismiss = { showAutocomplete = false },
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorHeader(
    lineCount: Int,
    charCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "CSS Editor",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "$lineCount lines",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "$charCount chars",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun LineNumbers(
    lineCount: Int,
    scrollState: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        repeat(lineCount) { index ->
            Text(
                text = (index + 1).toString(),
                style = TextStyle(
                    fontFamily = MonospaceFont,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                ),
                modifier = Modifier.semantics {
                    contentDescription = "Line number ${index + 1}"
                }
            )
        }
    }
}

@Composable
private fun AutocompletePopup(
    suggestions: List<String>,
    onSuggestionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(200.dp)
            .heightIn(max = 200.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            suggestions.forEach { suggestion ->
                TextButton(
                    onClick = { onSuggestionSelected(suggestion) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .semantics {
                            contentDescription = "Autocomplete suggestion: $suggestion"
                        }
                ) {
                    Text(
                        text = suggestion,
                        style = TextStyle(
                            fontFamily = MonospaceFont,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Highlights CSS syntax
 */
private fun highlightCSS(text: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        var currentIndex = 0

        lines.forEachIndexed { lineIndex, line ->
            highlightCSSLine(line, currentIndex)
            currentIndex += line.length

            // Add newline except for last line
            if (lineIndex < lines.size - 1) {
                append("\n")
                currentIndex += 1
            }
        }
    }
}

private fun AnnotatedString.Builder.highlightCSSLine(line: String, startOffset: Int) {
    var i = 0
    while (i < line.length) {
        when {
            // Comments
            line.substring(i).startsWith("/*") -> {
                val endIndex = line.indexOf("*/", i + 2)
                val commentEnd = if (endIndex != -1) endIndex + 2 else line.length
                withStyle(SpanStyle(color = SyntaxComment)) {
                    append(line.substring(i, commentEnd))
                }
                i = commentEnd
            }

            // Selectors (simple heuristic: before {)
            line.substring(i).contains("{") && !line.substring(0, i).contains("{") -> {
                val selectorEnd = line.indexOf("{", i)
                withStyle(SpanStyle(color = SyntaxSelector)) {
                    append(line.substring(i, selectorEnd))
                }
                i = selectorEnd
            }

            // Property (before :)
            line.substring(i).contains(":") && line.substring(i).indexOf(":") < line.substring(i).indexOf(";").coerceAtLeast(line.length) -> {
                val colonIndex = line.indexOf(":", i)
                val propertyText = line.substring(i, colonIndex).trim()
                if (propertyText.isNotEmpty() && !propertyText.contains("{") && !propertyText.contains("}")) {
                    withStyle(SpanStyle(color = SyntaxProperty)) {
                        append(line.substring(i, colonIndex))
                    }
                    i = colonIndex
                } else {
                    append(line[i])
                    i++
                }
            }

            // Values (after : before ;)
            line.substring(0, i).contains(":") && !line.substring(i).contains(":") -> {
                val semicolonIndex = line.indexOf(";", i).takeIf { it != -1 } ?: line.length
                val valueText = line.substring(i, semicolonIndex)
                withStyle(SpanStyle(color = SyntaxValue)) {
                    append(valueText)
                }
                i = semicolonIndex
            }

            else -> {
                append(line[i])
                i++
            }
        }
    }
}

/**
 * Get autocomplete suggestions based on context
 */
private fun getAutocompleteSuggestions(beforeCursor: String): List<String> {
    val lastLine = beforeCursor.lines().lastOrNull() ?: return emptyList()

    return when {
        lastLine.trim().endsWith(":") -> {
            // Property value suggestions
            val property = lastLine.substringBeforeLast(":").trim()
            getValueSuggestions(property)
        }
        lastLine.contains("{") && !lastLine.contains("}") -> {
            // Property name suggestions
            commonCSSProperties
        }
        else -> emptyList()
    }
}

private fun getValueSuggestions(property: String): List<String> {
    return when (property) {
        "display" -> listOf("block", "inline", "flex", "grid", "none", "inline-block")
        "position" -> listOf("static", "relative", "absolute", "fixed", "sticky")
        "text-align" -> listOf("left", "right", "center", "justify")
        "font-weight" -> listOf("normal", "bold", "lighter", "bolder", "100", "400", "700")
        "font-style" -> listOf("normal", "italic", "oblique")
        else -> emptyList()
    }
}

private val commonCSSProperties = listOf(
    "color", "background", "background-color", "border", "border-radius",
    "margin", "padding", "width", "height", "display", "position",
    "font-family", "font-size", "font-weight", "font-style", "text-align",
    "line-height", "letter-spacing", "text-decoration", "flex", "grid",
    "justify-content", "align-items", "gap", "z-index", "opacity",
    "transform", "transition", "animation"
)

/**
 * Handle keyboard events for editor shortcuts
 */
private fun handleKeyEvent(
    keyEvent: KeyEvent,
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
): Boolean {
    if (keyEvent.type != KeyEventType.KeyDown) return false

    return when {
        // Tab key - insert 2 spaces
        keyEvent.key == Key.Tab && !keyEvent.isShiftPressed -> {
            val cursorPos = textFieldValue.selection.start
            val newText = textFieldValue.text.substring(0, cursorPos) +
                    "  " +
                    textFieldValue.text.substring(cursorPos)
            onValueChange(
                TextFieldValue(
                    text = newText,
                    selection = androidx.compose.ui.text.TextRange(cursorPos + 2)
                )
            )
            true
        }

        // Enter key - auto-indent
        keyEvent.key == Key.Enter -> {
            val cursorPos = textFieldValue.selection.start
            val lines = textFieldValue.text.substring(0, cursorPos).lines()
            val currentLine = lines.lastOrNull() ?: ""
            val indent = currentLine.takeWhile { it.isWhitespace() }

            val newText = textFieldValue.text.substring(0, cursorPos) +
                    "\n" + indent +
                    textFieldValue.text.substring(cursorPos)
            onValueChange(
                TextFieldValue(
                    text = newText,
                    selection = androidx.compose.ui.text.TextRange(cursorPos + 1 + indent.length)
                )
            )
            true
        }

        // Ctrl/Cmd + / - Toggle comment
        (keyEvent.isCtrlPressed || keyEvent.isMetaPressed) && keyEvent.key == Key.Slash -> {
            toggleComment(textFieldValue, onValueChange)
            true
        }

        else -> false
    }
}

private fun toggleComment(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    val cursorPos = textFieldValue.selection.start
    val lines = textFieldValue.text.lines()
    val currentLineIndex = textFieldValue.text.substring(0, cursorPos).lines().size - 1
    val currentLine = lines.getOrNull(currentLineIndex) ?: return

    val lineStart = textFieldValue.text.lines().take(currentLineIndex).sumOf { it.length + 1 }
    val lineEnd = lineStart + currentLine.length

    val newLine = if (currentLine.trim().startsWith("/*") && currentLine.trim().endsWith("*/")) {
        // Uncomment
        currentLine.replace("/*", "").replace("*/", "").trim()
    } else {
        // Comment
        "/* $currentLine */"
    }

    val newText = textFieldValue.text.substring(0, lineStart) +
            newLine +
            textFieldValue.text.substring(lineEnd)

    onValueChange(
        TextFieldValue(
            text = newText,
            selection = androidx.compose.ui.text.TextRange(cursorPos)
        )
    )
}
