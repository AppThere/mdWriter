package com.appthere.mdwriter.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.appthere.mdwriter.data.local.RecentDocument
import com.appthere.mdwriter.data.model.DocumentInfo
import com.appthere.mdwriter.presentation.documents.DocumentListIntent
import com.appthere.mdwriter.presentation.documents.DocumentListState
import com.appthere.mdwriter.presentation.documents.SortOption
import com.appthere.mdwriter.ui.components.*

/**
 * Document list screen with search, sort, and document management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentListScreen(
    state: DocumentListState,
    onIntent: (DocumentListIntent) -> Unit,
    onNavigateToEditor: (DocumentInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var selectedDocument by remember { mutableStateOf<DocumentInfo?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("My Documents") },
                actions = {
                    // Sort button
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, "Sort documents")
                    }

                    // Sort menu
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (state.sortOption == option) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Spacer(Modifier.size(16.dp))
                                        }
                                        Text(option.label)
                                    }
                                },
                                onClick = {
                                    onIntent(DocumentListIntent.ChangeSortOption(option))
                                    showSortMenu = false
                                }
                            )
                        }
                    }

                    // More menu button
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "More options")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Import Document") },
                            onClick = {
                                onIntent(DocumentListIntent.ImportDocument)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.FileUpload, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Refresh") },
                            onClick = {
                                onIntent(DocumentListIntent.RefreshDocuments)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Refresh, null)
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onIntent(DocumentListIntent.ShowCreateDialog) }
            ) {
                Icon(Icons.Default.Add, "Create new document")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { onIntent(DocumentListIntent.SearchDocuments(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Loading indicator
            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Error message
            state.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Document list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Recent documents section
                if (state.recentDocuments.isNotEmpty() && state.searchQuery.isBlank()) {
                    item {
                        Text(
                            "Recent",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(
                        items = state.recentDocuments.take(5),
                        key = { it.id }
                    ) { recentDoc ->
                        RecentDocumentItem(
                            recentDocument = recentDoc,
                            onClick = {
                                // Find full document info from documents list
                                val docInfo = state.documents.find { it.id == recentDoc.id }
                                docInfo?.let { onNavigateToEditor(it) }
                            }
                        )
                    }

                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            "All Documents",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                // All documents
                items(
                    items = state.documents,
                    key = { it.id }
                ) { document ->
                    DocumentListItem(
                        documentInfo = document,
                        onClick = {
                            onIntent(DocumentListIntent.OpenDocument(document))
                            onNavigateToEditor(document)
                        },
                        onLongClick = {
                            selectedDocument = document
                        },
                        onMenuClick = { action ->
                            when (action) {
                                DocumentAction.OPEN -> {
                                    onIntent(DocumentListIntent.OpenDocument(document))
                                    onNavigateToEditor(document)
                                }
                                DocumentAction.RENAME -> {
                                    onIntent(DocumentListIntent.ShowRenameDialog(document))
                                }
                                DocumentAction.DELETE -> {
                                    onIntent(DocumentListIntent.ShowDeleteDialog(document))
                                }
                                DocumentAction.EXPORT -> {
                                    onIntent(DocumentListIntent.ExportDocument(document))
                                }
                            }
                        }
                    )
                }

                // Empty state
                if (state.documents.isEmpty() && !state.isLoading) {
                    item {
                        EmptyState(
                            message = if (state.searchQuery.isNotBlank()) {
                                "No documents found matching \"${state.searchQuery}\""
                            } else {
                                "No documents yet. Create your first document!"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                    }
                }
            }
        }

        // Dialogs
        if (state.showCreateDialog) {
            CreateDocumentDialog(
                onDismiss = { onIntent(DocumentListIntent.HideCreateDialog) },
                onCreate = { title, author ->
                    onIntent(DocumentListIntent.CreateDocument(title, author))
                }
            )
        }

        if (state.showDeleteDialog && state.documentToDelete != null) {
            DeleteDocumentDialog(
                documentInfo = state.documentToDelete,
                onDismiss = { onIntent(DocumentListIntent.HideDeleteDialog) },
                onConfirm = { onIntent(DocumentListIntent.ConfirmDelete) }
            )
        }

        if (state.showRenameDialog && state.documentToRename != null) {
            RenameDocumentDialog(
                documentInfo = state.documentToRename,
                onDismiss = { onIntent(DocumentListIntent.HideRenameDialog) },
                onRename = { newTitle ->
                    onIntent(
                        DocumentListIntent.RenameDocument(
                            state.documentToRename,
                            newTitle
                        )
                    )
                }
            )
        }
    }
}

/**
 * Search bar component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search documents...") },
        leadingIcon = {
            Icon(Icons.Default.Search, "Search")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, "Clear search")
                }
            }
        },
        singleLine = true
    )
}

/**
 * Document list item with context menu
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentListItem(
    documentInfo: DocumentInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuClick: (DocumentAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    onLongClick()
                    showMenu = true
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = documentInfo.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (documentInfo.author.isNotBlank()) {
                    Text(
                        text = documentInfo.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "${documentInfo.sectionCount} sections",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${documentInfo.wordCount} words",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Modified: ${formatDate(documentInfo.modified)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "More options")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open") },
                        onClick = {
                            onMenuClick(DocumentAction.OPEN)
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.OpenInNew, null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            onMenuClick(DocumentAction.RENAME)
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Export") },
                        onClick = {
                            onMenuClick(DocumentAction.EXPORT)
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.FileDownload, null)
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onMenuClick(DocumentAction.DELETE)
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        }
    }
}

/**
 * Recent document item
 */
@Composable
fun RecentDocumentItem(
    recentDocument: RecentDocument,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recentDocument.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Opened: ${formatDate(recentDocument.lastOpened)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/**
 * Empty state component
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Description,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Document actions enum
 */
enum class DocumentAction {
    OPEN, RENAME, DELETE, EXPORT
}

/**
 * Format instant to readable date string
 */
private fun formatDate(instant: kotlinx.datetime.Instant): String {
    return instant.toString().substringBefore('T')
}
