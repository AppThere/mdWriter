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
 * Dialog for inserting a markdown link
 * Provides URL and text input with validation
 */
@Composable
fun InsertLinkDialog(
    onDismiss: () -> Unit,
    onInsert: (text: String, url: String) -> Unit,
    initialText: String = "",
    modifier: Modifier = Modifier
) {
    var linkText by remember { mutableStateOf(initialText) }
    var linkUrl by remember { mutableStateOf("") }
    var urlError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .semantics {
                    contentDescription = "Insert link dialog"
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
                    text = "Insert Link",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Link text input
                OutlinedTextField(
                    value = linkText,
                    onValueChange = { linkText = it },
                    label = { Text("Link Text") },
                    placeholder = { Text("Click here") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Link text input field"
                        },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                // URL input
                OutlinedTextField(
                    value = linkUrl,
                    onValueChange = {
                        linkUrl = it
                        urlError = validateUrl(it)
                    },
                    label = { Text("URL") },
                    placeholder = { Text("https://example.com") },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "URL input field"
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (linkUrl.isNotEmpty() && urlError == null) {
                                onInsert(linkText, linkUrl)
                            }
                        }
                    )
                )

                // Preview
                if (linkUrl.isNotEmpty()) {
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
                                text = "Preview",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = generateMarkdownLink(linkText, linkUrl),
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
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
                            contentDescription = "Cancel inserting link"
                        }
                    ) {
                        Text("Cancel")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onInsert(linkText, linkUrl)
                        },
                        enabled = linkUrl.isNotEmpty() && urlError == null,
                        modifier = Modifier.semantics {
                            contentDescription = "Insert link button"
                        }
                    ) {
                        Text("Insert")
                    }
                }

                // Quick links section
                Text(
                    text = "Quick Links",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QuickLinkButton(
                        label = "Internal Link",
                        onClick = { linkUrl = "#" },
                        enabled = linkUrl.isEmpty()
                    )
                    QuickLinkButton(
                        label = "Email",
                        onClick = { linkUrl = "mailto:" },
                        enabled = linkUrl.isEmpty()
                    )
                    QuickLinkButton(
                        label = "Phone",
                        onClick = { linkUrl = "tel:" },
                        enabled = linkUrl.isEmpty()
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickLinkButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Quick link: $label"
            },
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Validate URL format
 */
private fun validateUrl(url: String): String? {
    if (url.isEmpty()) return null

    return when {
        url.startsWith("#") -> null // Internal link
        url.startsWith("mailto:") -> null // Email link
        url.startsWith("tel:") -> null // Phone link
        url.startsWith("http://") || url.startsWith("https://") -> {
            // Basic URL validation
            if (!url.contains(".")) {
                "URL should contain a domain (e.g., example.com)"
            } else {
                null
            }
        }
        url.startsWith("/") -> null // Relative link
        url.contains("://") -> null // Other protocols
        url.contains(".") -> null // Assume it's a domain without protocol
        else -> "URL should start with http://, https://, #, or mailto:"
    }
}

/**
 * Generate markdown link syntax
 */
private fun generateMarkdownLink(text: String, url: String): String {
    val displayText = text.ifEmpty { url }
    return "[$displayText]($url)"
}
