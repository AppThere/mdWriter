package com.appthere.mdwriter.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appthere.mdwriter.data.repository.InMemoryDocumentRepository
import com.appthere.mdwriter.domain.model.MarkdownFormat
import com.appthere.mdwriter.domain.usecase.*
import com.appthere.mdwriter.presentation.editor.EditorIntent
import com.appthere.mdwriter.presentation.editor.EditorViewModel
import com.appthere.mdwriter.ui.components.MarkdownEditor
import com.appthere.mdwriter.ui.components.MarkdownToolbar

/**
 * Main editor screen
 *
 * Features:
 * - Scaffold with top bar and toolbar
 * - Integration with EditorViewModel
 * - Dialogs for inserting links/images
 * - Error handling
 * - Loading states
 * - Responsive layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = createEditorViewModel()
) {
    val state by viewModel.state.collectAsState()

    var showLinkDialog by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    var showCssClassDialog by remember { mutableStateOf(false) }
    var showMetadataDialog by remember { mutableStateOf(false) }

    // Create new document on first launch
    LaunchedEffect(Unit) {
        viewModel.handleIntent(EditorIntent.CreateNewDocument)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.document?.metadata?.title ?: "Untitled Document",
                        maxLines = 1
                    )
                },
                actions = {
                    // Undo button
                    IconButton(
                        onClick = { viewModel.handleIntent(EditorIntent.Undo) },
                        enabled = state.canUndo
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo"
                        )
                    }

                    // Redo button
                    IconButton(
                        onClick = { viewModel.handleIntent(EditorIntent.Redo) },
                        enabled = state.canRedo
                    ) {
                        Icon(
                            imageVector = Icons.Default.Redo,
                            contentDescription = "Redo"
                        )
                    }

                    // Metadata button
                    IconButton(
                        onClick = { showMetadataDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Document Metadata"
                        )
                    }

                    // Save button
                    IconButton(
                        onClick = { viewModel.handleIntent(EditorIntent.SaveDocument) },
                        enabled = state.hasUnsavedChanges && state.documentPath != null
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Document"
                            )
                        }
                    }

                    // Menu button
                    IconButton(onClick = { /* TODO: Show menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = remember { SnackbarHostState() })
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Toolbar
            MarkdownToolbar(
                onFormatAction = { format ->
                    viewModel.handleIntent(EditorIntent.ApplyFormat(format))
                },
                onInsertLink = { showLinkDialog = true },
                onInsertImage = { showImageDialog = true },
                onAddCssClass = { showCssClassDialog = true }
            )

            // Editor
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    MarkdownEditor(
                        value = state.editorContent,
                        onValueChange = { newValue ->
                            viewModel.handleIntent(EditorIntent.TextChanged(newValue))
                        },
                        placeholder = "Start writing your markdown document..."
                    )
                }

                // Unsaved changes indicator
                if (state.hasUnsavedChanges) {
                    Text(
                        text = "Unsaved changes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    )
                }
            }
        }

        // Error snackbar
        state.error?.let { errorMessage ->
            LaunchedEffect(errorMessage) {
                // Show error snackbar
                // TODO: Implement proper snackbar handling
                viewModel.handleIntent(EditorIntent.DismissError)
            }
        }
    }

    // Link dialog
    if (showLinkDialog) {
        InsertLinkDialog(
            onDismiss = { showLinkDialog = false },
            onConfirm = { url, title ->
                viewModel.handleIntent(EditorIntent.InsertLink(url, title))
                showLinkDialog = false
            }
        )
    }

    // Image dialog
    if (showImageDialog) {
        InsertImageDialog(
            onDismiss = { showImageDialog = false },
            onConfirm = { url, alt ->
                viewModel.handleIntent(EditorIntent.InsertImage(url, alt))
                showImageDialog = false
            }
        )
    }

    // CSS class dialog
    if (showCssClassDialog) {
        AddCssClassDialog(
            onDismiss = { showCssClassDialog = false },
            onConfirm = { className ->
                viewModel.handleIntent(EditorIntent.AddCssClass(className))
                showCssClassDialog = false
            }
        )
    }

    // Metadata dialog
    if (showMetadataDialog) {
        MetadataDialog(
            title = state.document?.metadata?.title ?: "",
            author = state.document?.metadata?.author ?: "",
            onDismiss = { showMetadataDialog = false },
            onConfirm = { title, author ->
                viewModel.handleIntent(EditorIntent.UpdateMetadata(title, author))
                showMetadataDialog = false
            }
        )
    }
}

/**
 * Dialog for inserting a link
 */
@Composable
private fun InsertLinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (url: String, title: String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert Link") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    placeholder = { Text("https://example.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (optional)") },
                    placeholder = { Text("Link title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(url, title) },
                enabled = url.isNotBlank()
            ) {
                Text("Insert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog for inserting an image
 */
@Composable
private fun InsertImageDialog(
    onDismiss: () -> Unit,
    onConfirm: (url: String, alt: String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var alt by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert Image") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Image URL") },
                    placeholder = { Text("https://example.com/image.png") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = alt,
                    onValueChange = { alt = it },
                    label = { Text("Alt Text") },
                    placeholder = { Text("Image description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(url, alt) },
                enabled = url.isNotBlank()
            ) {
                Text("Insert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog for adding CSS class
 */
@Composable
private fun AddCssClassDialog(
    onDismiss: () -> Unit,
    onConfirm: (className: String) -> Unit
) {
    var className by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add CSS Class") },
        text = {
            OutlinedTextField(
                value = className,
                onValueChange = { className = it },
                label = { Text("Class Name") },
                placeholder = { Text("my-class") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(className) },
                enabled = className.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog for editing document metadata
 */
@Composable
private fun MetadataDialog(
    title: String,
    author: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, author: String) -> Unit
) {
    var titleText by remember { mutableStateOf(title) }
    var authorText by remember { mutableStateOf(author) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Document Metadata") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Title") },
                    placeholder = { Text("Document title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = authorText,
                    onValueChange = { authorText = it },
                    label = { Text("Author") },
                    placeholder = { Text("Author name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(titleText, authorText) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Factory function to create EditorViewModel with dependencies
 * This will be replaced with proper DI in production
 */
@Composable
private fun createEditorViewModel(): EditorViewModel {
    val repository = remember { InMemoryDocumentRepository() }
    return viewModel {
        EditorViewModel(
            loadDocumentUseCase = LoadDocumentUseCase(repository),
            saveDocumentUseCase = SaveDocumentUseCase(repository),
            createDocumentUseCase = CreateDocumentUseCase(repository),
            applyFormatUseCase = ApplyFormatUseCase(),
            updateSectionContentUseCase = UpdateSectionContentUseCase()
        )
    }
}
