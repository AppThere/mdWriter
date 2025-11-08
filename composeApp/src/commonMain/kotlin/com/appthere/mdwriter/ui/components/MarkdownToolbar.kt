package com.appthere.mdwriter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.appthere.mdwriter.domain.model.MarkdownFormat

/**
 * Toolbar for Markdown formatting actions
 *
 * Features:
 * - Horizontal scrolling for narrow screens
 * - Minimum 48dp touch targets
 * - Proper accessibility labels
 * - E Ink optimized styling
 */
@Composable
fun MarkdownToolbar(
    onFormatAction: (MarkdownFormat) -> Unit,
    onInsertLink: () -> Unit,
    onInsertImage: () -> Unit,
    onAddCssClass: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Inline formatting
            ToolbarSection(title = "Format") {
                ToolbarButton(
                    icon = Icons.Default.FormatBold,
                    contentDescription = "Bold",
                    onClick = { onFormatAction(MarkdownFormat.Bold) }
                )
                ToolbarButton(
                    icon = Icons.Default.FormatItalic,
                    contentDescription = "Italic",
                    onClick = { onFormatAction(MarkdownFormat.Italic) }
                )
                ToolbarButton(
                    icon = Icons.Default.Code,
                    contentDescription = "Inline Code",
                    onClick = { onFormatAction(MarkdownFormat.Code) }
                )
                ToolbarButton(
                    icon = Icons.Default.StrikethroughS,
                    contentDescription = "Strikethrough",
                    onClick = { onFormatAction(MarkdownFormat.Strikethrough) }
                )
            }

            VerticalDivider(modifier = Modifier.height(40.dp))

            // Headings
            ToolbarSection(title = "Headings") {
                ToolbarButton(
                    text = "H1",
                    contentDescription = "Heading 1",
                    onClick = { onFormatAction(MarkdownFormat.Heading1) }
                )
                ToolbarButton(
                    text = "H2",
                    contentDescription = "Heading 2",
                    onClick = { onFormatAction(MarkdownFormat.Heading2) }
                )
                ToolbarButton(
                    text = "H3",
                    contentDescription = "Heading 3",
                    onClick = { onFormatAction(MarkdownFormat.Heading3) }
                )
                ToolbarButton(
                    text = "H4",
                    contentDescription = "Heading 4",
                    onClick = { onFormatAction(MarkdownFormat.Heading4) }
                )
            }

            VerticalDivider(modifier = Modifier.height(40.dp))

            // Lists
            ToolbarSection(title = "Lists") {
                ToolbarButton(
                    icon = Icons.Default.FormatListBulleted,
                    contentDescription = "Bullet List",
                    onClick = { onFormatAction(MarkdownFormat.BulletList) }
                )
                ToolbarButton(
                    icon = Icons.Default.FormatListNumbered,
                    contentDescription = "Numbered List",
                    onClick = { onFormatAction(MarkdownFormat.NumberedList) }
                )
                ToolbarButton(
                    icon = Icons.Default.CheckBox,
                    contentDescription = "Task List",
                    onClick = { onFormatAction(MarkdownFormat.TaskList) }
                )
            }

            VerticalDivider(modifier = Modifier.height(40.dp))

            // Blocks
            ToolbarSection(title = "Blocks") {
                ToolbarButton(
                    icon = Icons.Default.FormatQuote,
                    contentDescription = "Blockquote",
                    onClick = { onFormatAction(MarkdownFormat.Blockquote) }
                )
                ToolbarButton(
                    icon = Icons.Default.DataObject,
                    contentDescription = "Code Block",
                    onClick = { onFormatAction(MarkdownFormat.CodeBlock) }
                )
                ToolbarButton(
                    icon = Icons.Default.HorizontalRule,
                    contentDescription = "Horizontal Rule",
                    onClick = { onFormatAction(MarkdownFormat.HorizontalRule) }
                )
            }

            VerticalDivider(modifier = Modifier.height(40.dp))

            // Insert
            ToolbarSection(title = "Insert") {
                ToolbarButton(
                    icon = Icons.Default.Link,
                    contentDescription = "Insert Link",
                    onClick = onInsertLink
                )
                ToolbarButton(
                    icon = Icons.Default.Image,
                    contentDescription = "Insert Image",
                    onClick = onInsertImage
                )
                ToolbarButton(
                    icon = Icons.Default.Style,
                    contentDescription = "Add CSS Class",
                    onClick = onAddCssClass
                )
            }
        }
    }
}

/**
 * Section of toolbar buttons with optional label
 */
@Composable
private fun ToolbarSection(
    title: String,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/**
 * Toolbar button with icon
 *
 * Ensures:
 * - Minimum 48dp touch target
 * - Proper accessibility labels
 * - E Ink optimized styling
 */
@Composable
private fun ToolbarButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .semantics { this.contentDescription = contentDescription }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Toolbar button with text label
 */
@Composable
private fun ToolbarButton(
    text: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 48.dp)
            .widthIn(min = 48.dp)
            .semantics { this.contentDescription = contentDescription },
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(1.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
    )
}
