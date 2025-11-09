package com.appthere.mdwriter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * Document metadata following Dublin Core standard
 */
data class DocumentMetadata(
    val title: String = "",
    val author: String = "",
    val description: String = "",
    val subject: String = "",
    val publisher: String = "",
    val contributor: String = "",
    val created: String = "",
    val modified: String = "",
    val language: String = "en",
    val rights: String = "",
    val tags: List<String> = emptyList(),
    val format: String = "text/markdown",
    val identifier: String = "",
    val source: String = "",
    val relation: String = "",
    val coverage: String = "",
    val wordCount: Int = 0,
    val characterCount: Int = 0
)

/**
 * Editor for document metadata with Dublin Core fields
 * Includes tags input, date fields, and read-only statistics
 */
@Composable
fun DocumentMetadataEditor(
    metadata: DocumentMetadata,
    onMetadataChange: (DocumentMetadata) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    var currentTag by remember { mutableStateOf("") }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Document Metadata",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Basic Information Section
                SectionHeader("Basic Information")

                OutlinedTextField(
                    value = metadata.title,
                    onValueChange = { onMetadataChange(metadata.copy(title = it)) },
                    label = { Text("Title *") },
                    placeholder = { Text("Document title") },
                    singleLine = true,
                    readOnly = readOnly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Document title field" },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                OutlinedTextField(
                    value = metadata.author,
                    onValueChange = { onMetadataChange(metadata.copy(author = it)) },
                    label = { Text("Author") },
                    placeholder = { Text("Author name") },
                    singleLine = true,
                    readOnly = readOnly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Document author field" },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                OutlinedTextField(
                    value = metadata.description,
                    onValueChange = { onMetadataChange(metadata.copy(description = it)) },
                    label = { Text("Description") },
                    placeholder = { Text("Brief description of the document") },
                    singleLine = false,
                    maxLines = 4,
                    readOnly = readOnly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Document description field" },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                // Tags Section
                SectionHeader("Tags")

                TagsInput(
                    tags = metadata.tags,
                    onTagsChange = { onMetadataChange(metadata.copy(tags = it)) },
                    currentTag = currentTag,
                    onCurrentTagChange = { currentTag = it },
                    readOnly = readOnly
                )

                HorizontalDivider()

                // Publication Information Section
                SectionHeader("Publication Information")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = metadata.language,
                        onValueChange = { onMetadataChange(metadata.copy(language = it)) },
                        label = { Text("Language") },
                        placeholder = { Text("en") },
                        singleLine = true,
                        readOnly = readOnly,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Document language code" },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    OutlinedTextField(
                        value = metadata.format,
                        onValueChange = { onMetadataChange(metadata.copy(format = it)) },
                        label = { Text("Format") },
                        placeholder = { Text("text/markdown") },
                        singleLine = true,
                        readOnly = true, // Format is always read-only
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Document format" },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        enabled = false
                    )
                }

                OutlinedTextField(
                    value = metadata.publisher,
                    onValueChange = { onMetadataChange(metadata.copy(publisher = it)) },
                    label = { Text("Publisher") },
                    placeholder = { Text("Publishing organization") },
                    singleLine = true,
                    readOnly = readOnly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Publisher field" },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = metadata.contributor,
                    onValueChange = { onMetadataChange(metadata.copy(contributor = it)) },
                    label = { Text("Contributors") },
                    placeholder = { Text("Other contributors") },
                    singleLine = false,
                    maxLines = 2,
                    readOnly = readOnly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Contributors field" },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = metadata.rights,
                    onValueChange = { onMetadataChange(metadata.copy(rights = it)) },
                    label = { Text("Rights/License") },
                    placeholder = { Text("Copyright and license information") },
                    singleLine = false,
                    maxLines = 2,
                    readOnly = readOnly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Rights and license field" },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                HorizontalDivider()

                // Additional Dublin Core Fields Section
                SectionHeader("Additional Information")

                OutlinedTextField(
                    value = metadata.subject,
                    onValueChange = { onMetadataChange(metadata.copy(subject = it)) },
                    label = { Text("Subject") },
                    placeholder = { Text("Topic or theme") },
                    singleLine = true,
                    readOnly = readOnly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Subject field" },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = metadata.identifier,
                    onValueChange = { onMetadataChange(metadata.copy(identifier = it)) },
                    label = { Text("Identifier") },
                    placeholder = { Text("DOI, ISBN, or unique ID") },
                    singleLine = true,
                    readOnly = readOnly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Document identifier field" },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = metadata.source,
                    onValueChange = { onMetadataChange(metadata.copy(source = it)) },
                    label = { Text("Source") },
                    placeholder = { Text("Original source") },
                    singleLine = true,
                    readOnly = readOnly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Source field" },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = metadata.relation,
                    onValueChange = { onMetadataChange(metadata.copy(relation = it)) },
                    label = { Text("Relation") },
                    placeholder = { Text("Related resources") },
                    singleLine = false,
                    maxLines = 2,
                    readOnly = readOnly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Related resources field" },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = metadata.coverage,
                    onValueChange = { onMetadataChange(metadata.copy(coverage = it)) },
                    label = { Text("Coverage") },
                    placeholder = { Text("Spatial or temporal coverage") },
                    singleLine = true,
                    readOnly = readOnly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Coverage field" },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                HorizontalDivider()

                // Statistics Section (Read-only)
                SectionHeader("Statistics")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatisticRow("Created", metadata.created.ifEmpty { "Not set" })
                        StatisticRow("Last Modified", metadata.modified.ifEmpty { "Not set" })
                        StatisticRow("Word Count", metadata.wordCount.toString())
                        StatisticRow("Character Count", metadata.characterCount.toString())
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun StatisticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TagsInput(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    currentTag: String,
    onCurrentTagChange: (String) -> Unit,
    readOnly: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tag input field
        if (!readOnly) {
            OutlinedTextField(
                value = currentTag,
                onValueChange = onCurrentTagChange,
                label = { Text("Add Tag") },
                placeholder = { Text("Enter tag and press Enter") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Add tag input field" },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (currentTag.isNotBlank() && currentTag !in tags) {
                            onTagsChange(tags + currentTag.trim())
                            onCurrentTagChange("")
                        }
                    }
                ),
                trailingIcon = {
                    if (currentTag.isNotBlank()) {
                        TextButton(
                            onClick = {
                                if (currentTag.isNotBlank() && currentTag !in tags) {
                                    onTagsChange(tags + currentTag.trim())
                                    onCurrentTagChange("")
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    }
                }
            )
        }

        // Tags chips
        if (tags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        onRemove = if (!readOnly) {
                            { onTagsChange(tags - tag) }
                        } else null
                    )
                }
            }
        } else {
            Text(
                text = if (readOnly) "No tags" else "No tags. Add some above.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun TagChip(
    tag: String,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    InputChip(
        selected = false,
        onClick = { },
        label = { Text(tag) },
        trailingIcon = if (onRemove != null) {
            {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove tag $tag",
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onRemove() }
                        .semantics {
                            contentDescription = "Remove tag $tag"
                        }
                )
            }
        } else null,
        modifier = modifier.semantics {
            contentDescription = "Tag: $tag"
        }
    )
}

@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Simple flow layout - in production, use androidx.compose.foundation.layout.FlowRow
    // This is a simplified version for demonstration
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        content()
    }
}
