package com.appthere.mdwriter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * YAML Frontmatter Editor for section metadata
 * Provides key-value pair editing with validation and suggestions
 */
@Composable
fun FrontmatterEditor(
    frontmatter: Map<String, String>,
    onFrontmatterChange: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    var fields by remember(frontmatter) {
        mutableStateOf(frontmatter.toMutableMap())
    }

    var validationErrors by remember { mutableStateOf(emptyMap<String, String>()) }

    // Update fields when external frontmatter changes
    LaunchedEffect(frontmatter) {
        if (fields != frontmatter) {
            fields = frontmatter.toMutableMap()
        }
    }

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
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Frontmatter",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!readOnly) {
                    FilledTonalButton(
                        onClick = {
                            val newKey = "field${fields.size + 1}"
                            fields[newKey] = ""
                            onFrontmatterChange(fields)
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Add new frontmatter field"
                        }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Add Field")
                    }
                }
            }

            HorizontalDivider()

            // Field editor
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (fields.isEmpty()) {
                    Text(
                        text = "No frontmatter fields. Click 'Add Field' to create one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    fields.forEach { (key, value) ->
                        FrontmatterField(
                            key = key,
                            value = value,
                            onKeyChange = { newKey ->
                                val newFields = fields.toMutableMap()
                                newFields.remove(key)
                                newFields[newKey] = value

                                // Validate
                                val error = validateKey(newKey, newFields.keys - newKey)
                                validationErrors = if (error != null) {
                                    validationErrors + (newKey to error)
                                } else {
                                    validationErrors - key - newKey
                                }

                                fields = newFields
                                onFrontmatterChange(fields)
                            },
                            onValueChange = { newValue ->
                                fields[key] = newValue
                                onFrontmatterChange(fields)
                            },
                            onDelete = {
                                fields = fields.toMutableMap().apply { remove(key) }
                                validationErrors = validationErrors - key
                                onFrontmatterChange(fields)
                            },
                            error = validationErrors[key],
                            readOnly = readOnly,
                            suggestions = getCommonFrontmatterFields()
                        )
                    }
                }
            }

            // Preview
            if (fields.isNotEmpty()) {
                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "YAML Preview",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = generateYAML(fields),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.semantics {
                            contentDescription = "YAML preview of frontmatter"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FrontmatterField(
    key: String,
    value: String,
    onKeyChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onDelete: () -> Unit,
    error: String?,
    readOnly: Boolean,
    suggestions: List<String>,
    modifier: Modifier = Modifier
) {
    var showSuggestions by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = if (error != null) {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Key input
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = key,
                        onValueChange = {
                            onKeyChange(it)
                            showSuggestions = it.isNotEmpty()
                        },
                        label = { Text("Field Name") },
                        singleLine = true,
                        readOnly = readOnly,
                        isError = error != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Frontmatter field name: $key"
                            },
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace)
                    )

                    // Suggestions dropdown
                    if (showSuggestions && suggestions.isNotEmpty()) {
                        val filteredSuggestions = suggestions.filter {
                            it.contains(key, ignoreCase = true) && it != key
                        }

                        if (filteredSuggestions.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 150.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.verticalScroll(rememberScrollState())
                                ) {
                                    filteredSuggestions.forEach { suggestion ->
                                        TextButton(
                                            onClick = {
                                                onKeyChange(suggestion)
                                                showSuggestions = false
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .semantics {
                                                    contentDescription = "Suggestion: $suggestion"
                                                }
                                        ) {
                                            Text(
                                                text = suggestion,
                                                style = TextStyle(fontFamily = FontFamily.Monospace),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Value input
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text("Value") },
                    singleLine = false,
                    maxLines = 3,
                    readOnly = readOnly,
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = "Value for field $key"
                        },
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace)
                )

                // Delete button
                if (!readOnly) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(48.dp)
                            .semantics {
                                contentDescription = "Delete field $key"
                            }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Error message
            if (error != null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Field description
            val description = getFieldDescription(key)
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Validate frontmatter key
 */
private fun validateKey(key: String, existingKeys: Set<String>): String? {
    return when {
        key.isBlank() -> "Field name cannot be empty"
        key.contains(" ") -> "Field name cannot contain spaces"
        key.contains(":") -> "Field name cannot contain colons"
        key in existingKeys -> "Field name already exists"
        !key.matches(Regex("^[a-zA-Z][a-zA-Z0-9_-]*$")) ->
            "Field name must start with a letter and contain only letters, numbers, hyphens, and underscores"
        else -> null
    }
}

/**
 * Generate YAML from frontmatter map
 */
private fun generateYAML(frontmatter: Map<String, String>): String {
    if (frontmatter.isEmpty()) return "---\n---"

    val yaml = buildString {
        appendLine("---")
        frontmatter.forEach { (key, value) ->
            val escapedValue = if (value.contains("\n") || value.contains(":") || value.contains("#")) {
                "\"${value.replace("\"", "\\\"")}\""
            } else if (value.isEmpty()) {
                "\"\""
            } else {
                value
            }
            appendLine("$key: $escapedValue")
        }
        append("---")
    }

    return yaml
}

/**
 * Common Hugo frontmatter fields
 */
private fun getCommonFrontmatterFields(): List<String> {
    return listOf(
        "title",
        "date",
        "draft",
        "description",
        "summary",
        "tags",
        "categories",
        "author",
        "weight",
        "slug",
        "aliases",
        "layout",
        "type",
        "publishDate",
        "expiryDate",
        "lastmod",
        "featured",
        "keywords",
        "markup",
        "outputs"
    )
}

/**
 * Get description for common frontmatter fields
 */
private fun getFieldDescription(key: String): String? {
    return when (key.lowercase()) {
        "title" -> "The title of the content"
        "date" -> "Publication date (YYYY-MM-DD or ISO 8601)"
        "draft" -> "Whether this is a draft (true/false)"
        "description" -> "Short description of the content"
        "summary" -> "Summary shown in lists"
        "tags" -> "Comma-separated tags"
        "categories" -> "Content categories"
        "author" -> "Content author"
        "weight" -> "Order weight (lower numbers appear first)"
        "slug" -> "URL slug for this content"
        else -> null
    }
}
