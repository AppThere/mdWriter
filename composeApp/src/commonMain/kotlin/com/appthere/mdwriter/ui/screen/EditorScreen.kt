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
import com.appthere.mdwriter.ui.components.*

/**
 * Editor tabs for different editing modes
 */
enum class EditorTab {
    MARKDOWN,
    CSS,
    FRONTMATTER,
    METADATA
}

/**
 * Main editor screen with tabs for Markdown, CSS, Frontmatter, and Metadata editing
 *
 * Features:
 * - Tabbed interface for different editing modes
 * - Integration with EditorViewModel
 * - Phase 4 advanced editing components
 * - Dialogs for inserting links/images
 * - Error handling and loading states
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = createEditorViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(EditorTab.MARKDOWN) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }

    // Create new document on first launch
    LaunchedEffect(Unit) {
        viewModel.handleIntent(EditorIntent.CreateNewDocument)
    }

    Scaffold(
        modifier = modifier,
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
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Tab(
                    selected = selectedTab == EditorTab.MARKDOWN,
                    onClick = { selectedTab = EditorTab.MARKDOWN },
                    text = { Text("Markdown") },
                    icon = { Icon(Icons.Default.Edit, "Markdown Editor") }
                )
                Tab(
                    selected = selectedTab == EditorTab.CSS,
                    onClick = { selectedTab = EditorTab.CSS },
                    text = { Text("CSS") },
                    icon = { Icon(Icons.Default.Brush, "CSS Editor") }
                )
                Tab(
                    selected = selectedTab == EditorTab.FRONTMATTER,
                    onClick = { selectedTab = EditorTab.FRONTMATTER },
                    text = { Text("Frontmatter") },
                    icon = { Icon(Icons.Default.Settings, "Frontmatter") }
                )
                Tab(
                    selected = selectedTab == EditorTab.METADATA,
                    onClick = { selectedTab = EditorTab.METADATA },
                    text = { Text("Metadata") },
                    icon = { Icon(Icons.Default.Info, "Metadata") }
                )
            }

            // Content based on selected tab
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    EditorTab.MARKDOWN -> {
                        MarkdownTabContent(
                            state = state,
                            viewModel = viewModel,
                            onShowLinkDialog = { showLinkDialog = true },
                            onShowImageDialog = { showImageDialog = true }
                        )
                    }
                    EditorTab.CSS -> {
                        CSSTabContent(
                            stylesheets = state.document?.stylesheets ?: emptyList(),
                            onStylesheetChange = { /* TODO: Implement */ }
                        )
                    }
                    EditorTab.FRONTMATTER -> {
                        FrontmatterTabContent(
                            frontmatter = state.document?.let { doc ->
                                state.currentSectionId?.let { sectionId ->
                                    doc.getSection(sectionId)?.parseFrontmatter() ?: emptyMap()
                                }
                            } ?: emptyMap(),
                            onFrontmatterChange = { /* TODO: Implement */ }
                        )
                    }
                    EditorTab.METADATA -> {
                        MetadataTabContent(
                            metadata = state.document?.metadata,
                            onMetadataChange = { newMetadata ->
                                viewModel.handleIntent(
                                    EditorIntent.UpdateMetadata(
                                        title = newMetadata.title,
                                        author = newMetadata.author
                                    )
                                )
                            }
                        )
                    }
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
    }

    // Error snackbar
    state.error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            viewModel.handleIntent(EditorIntent.DismissError)
        }
    }

    // Link dialog - using Phase 4 component
    if (showLinkDialog) {
        val selectedText = state.editorContent.let {
            if (it.selection.start < it.selection.end) {
                it.text.substring(it.selection.start, it.selection.end)
            } else ""
        }

        InsertLinkDialog(
            onDismiss = { showLinkDialog = false },
            onInsert = { text, url ->
                viewModel.handleIntent(EditorIntent.InsertLink(url, text))
                showLinkDialog = false
            },
            initialText = selectedText
        )
    }

    // Image dialog - using Phase 4 component
    if (showImageDialog) {
        InsertImageDialog(
            onDismiss = { showImageDialog = false },
            onInsert = { url, altText, title ->
                viewModel.handleIntent(EditorIntent.InsertImage(url, altText))
                showImageDialog = false
            }
        )
    }
}

/**
 * Markdown editor tab content
 */
@Composable
private fun MarkdownTabContent(
    state: com.appthere.mdwriter.presentation.editor.EditorState,
    viewModel: EditorViewModel,
    onShowLinkDialog: () -> Unit,
    onShowImageDialog: () -> Unit
) {
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Toolbar
        MarkdownToolbar(
            currentValue = state.editorContent,
            onFormatAction = { currentValue, format ->
                viewModel.handleIntent(EditorIntent.ApplyFormat(currentValue, format))
            },
            onInsertLink = onShowLinkDialog,
            onInsertImage = onShowImageDialog,
            onAddCssClass = { /* TODO: Show CSS class dialog */ }
        )

        // Editor
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            MarkdownEditor(
                value = state.editorContent,
                onValueChange = { newValue ->
                    viewModel.handleIntent(EditorIntent.TextChanged(newValue))
                },
                focusRequester = focusRequester,
                placeholder = "Start writing your markdown document..."
            )
        }
    }
}

/**
 * CSS editor tab content
 */
@Composable
private fun CSSTabContent(
    stylesheets: List<com.appthere.mdwriter.data.model.Stylesheet>,
    onStylesheetChange: (String) -> Unit
) {
    var selectedStylesheetIndex by remember { mutableStateOf(0) }
    var cssContent by remember(selectedStylesheetIndex, stylesheets) {
        mutableStateOf(stylesheets.getOrNull(selectedStylesheetIndex)?.content ?: "")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (stylesheets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No stylesheets",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { /* TODO: Add stylesheet */ }) {
                        Text("Add Stylesheet")
                    }
                }
            }
        } else {
            // Stylesheet selector
            if (stylesheets.size > 1) {
                ScrollableTabRow(
                    selectedTabIndex = selectedStylesheetIndex,
                    edgePadding = 0.dp
                ) {
                    stylesheets.forEachIndexed { index, stylesheet ->
                        Tab(
                            selected = selectedStylesheetIndex == index,
                            onClick = { selectedStylesheetIndex = index },
                            text = { Text(stylesheet.name) }
                        )
                    }
                }
            }

            // CSS Editor
            CSSEditor(
                value = cssContent,
                onValueChange = { newValue ->
                    cssContent = newValue
                    onStylesheetChange(newValue)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            )
        }
    }
}

/**
 * Frontmatter editor tab content
 */
@Composable
private fun FrontmatterTabContent(
    frontmatter: Map<String, String>,
    onFrontmatterChange: (Map<String, String>) -> Unit
) {
    FrontmatterEditor(
        frontmatter = frontmatter,
        onFrontmatterChange = onFrontmatterChange,
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Metadata editor tab content
 */
@OptIn(kotlin.time.ExperimentalTime::class)
@Composable
private fun MetadataTabContent(
    metadata: com.appthere.mdwriter.data.model.Metadata?,
    onMetadataChange: (DocumentMetadata) -> Unit
) {
    if (metadata == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        DocumentMetadataEditor(
            metadata = DocumentMetadata(
                title = metadata.title,
                author = metadata.author,
                description = metadata.description,
                subject = metadata.subject,
                created = metadata.created.toString(),
                modified = metadata.modified.toString(),
                language = metadata.language
            ),
            onMetadataChange = onMetadataChange,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Helper function to parse frontmatter from section content
 */
private fun com.appthere.mdwriter.data.model.Section.parseFrontmatter(): Map<String, String> {
    val frontmatterRegex = Regex("^---\\s*\\n(.*?)\\n---\\s*\\n", RegexOption.DOT_MATCHES_ALL)
    val match = frontmatterRegex.find(content) ?: return emptyMap()

    val yamlContent = match.groupValues[1]
    return yamlContent.lines()
        .mapNotNull { line ->
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) {
                parts[0].trim() to parts[1].trim().removeSurrounding("\"")
            } else null
        }
        .toMap()
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
