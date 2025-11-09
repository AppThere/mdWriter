package com.appthere.mdwriter

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.appthere.mdwriter.presentation.documents.DocumentListViewModel
import com.appthere.mdwriter.ui.screen.DocumentListScreen
import com.appthere.mdwriter.ui.theme.MDWriterTheme

/**
 * Main application composable
 */
@Composable
fun App() {
    // Initialize platform-specific dependencies
    val dependencies = remember { createAppDependencies() }

    // Create ViewModel
    val viewModel = remember {
        DocumentListViewModel(
            documentRepository = dependencies.documentRepository,
            recentDocumentsManager = dependencies.recentDocumentsManager
        )
    }

    // Collect state
    val state by viewModel.state.collectAsState()

    MDWriterTheme {
        DocumentListScreen(
            state = state,
            onIntent = viewModel::handleIntent,
            onNavigateToEditor = { documentInfo ->
                // TODO: Navigate to editor screen when implemented
                println("Navigate to editor for document: ${documentInfo.title}")
            },
            modifier = Modifier
        )
    }
}