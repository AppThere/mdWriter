package com.appthere.mdwriter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Dialog for inserting a markdown image
 * Provides image URL/path, alt text, and optional title input
 */
@Composable
fun InsertImageDialog(
    onDismiss: () -> Unit,
    onInsert: (url: String, altText: String, title: String?) -> Unit,
    onBrowseFile: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var imageUrl by remember { mutableStateOf("") }
    var altText by remember { mutableStateOf("") }
    var imageTitle by remember { mutableStateOf("") }
    var urlError by remember { mutableStateOf<String?>(null) }
    var altTextError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .semantics {
                    contentDescription = "Insert image dialog"
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "Insert Image",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Image URL/Path input
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = {
                        imageUrl = it
                        urlError = validateImageUrl(it)
                    },
                    label = { Text("Image URL or Path") },
                    placeholder = { Text("https://example.com/image.jpg") },
                    singleLine = true,
                    isError = urlError != null,
                    supportingText = {
                        if (urlError != null) {
                            Text(
                                text = urlError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    trailingIcon = {
                        if (onBrowseFile != null) {
                            TextButton(
                                onClick = onBrowseFile,
                                modifier = Modifier.semantics {
                                    contentDescription = "Browse for image file"
                                }
                            ) {
                                Text("Browse", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Image URL or path input field"
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                // Alt text input (required for accessibility)
                OutlinedTextField(
                    value = altText,
                    onValueChange = {
                        altText = it
                        altTextError = if (it.isEmpty()) "Alt text is required for accessibility" else null
                    },
                    label = { Text("Alt Text *") },
                    placeholder = { Text("Describe the image") },
                    singleLine = false,
                    maxLines = 3,
                    isError = altTextError != null,
                    supportingText = {
                        if (altTextError != null) {
                            Text(
                                text = altTextError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "Describes the image for screen readers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Image alt text input field"
                        },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                // Optional title input
                OutlinedTextField(
                    value = imageTitle,
                    onValueChange = { imageTitle = it },
                    label = { Text("Title (Optional)") },
                    placeholder = { Text("Hover text") },
                    singleLine = true,
                    supportingText = {
                        Text(
                            text = "Shown when hovering over the image",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Image title input field (optional)"
                        },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (imageUrl.isNotEmpty() && altText.isNotEmpty() &&
                                urlError == null && altTextError == null) {
                                onInsert(imageUrl, altText, imageTitle.ifEmpty { null })
                            }
                        }
                    )
                )

                // Preview
                if (imageUrl.isNotEmpty() && altText.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Markdown Preview",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = generateMarkdownImage(imageUrl, altText, imageTitle.ifEmpty { null }),
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Image format tips
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Supported Formats",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "• JPG, PNG, GIF, WebP, SVG\n" +
                                   "• Absolute URLs (https://...)\n" +
                                   "• Relative paths (./images/...)\n" +
                                   "• Document resources",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.semantics {
                            contentDescription = "Cancel inserting image"
                        }
                    ) {
                        Text("Cancel")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onInsert(imageUrl, altText, imageTitle.ifEmpty { null })
                        },
                        enabled = imageUrl.isNotEmpty() &&
                                  altText.isNotEmpty() &&
                                  urlError == null &&
                                  altTextError == null,
                        modifier = Modifier.semantics {
                            contentDescription = "Insert image button"
                        }
                    ) {
                        Text("Insert")
                    }
                }
            }
        }
    }
}

/**
 * Validate image URL or path
 */
private fun validateImageUrl(url: String): String? {
    if (url.isEmpty()) return "Image URL or path is required"

    val validExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg", ".bmp")
    val hasValidExtension = validExtensions.any { url.lowercase().endsWith(it) }

    return when {
        url.startsWith("http://") || url.startsWith("https://") -> {
            if (!hasValidExtension) {
                "Warning: URL doesn't end with a common image extension"
            } else {
                null
            }
        }
        url.startsWith("/") || url.startsWith("./") || url.startsWith("../") -> {
            if (!hasValidExtension) {
                "Path should point to an image file (.jpg, .png, etc.)"
            } else {
                null
            }
        }
        url.contains("://") -> null // Other protocols (data:, etc.)
        hasValidExtension -> null // Assume it's a relative path
        else -> "URL should be a valid image path or URL"
    }
}

/**
 * Generate markdown image syntax
 */
private fun generateMarkdownImage(url: String, altText: String, title: String?): String {
    return if (title != null && title.isNotEmpty()) {
        "![${altText}](${url} \"${title}\")"
    } else {
        "![${altText}](${url})"
    }
}
