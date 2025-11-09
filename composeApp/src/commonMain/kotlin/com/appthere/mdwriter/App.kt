package com.appthere.mdwriter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.appthere.mdwriter.data.model.DocumentInfo
import com.appthere.mdwriter.presentation.documents.DocumentListViewModel
import com.appthere.mdwriter.ui.screen.DocumentListScreen
import com.appthere.mdwriter.ui.screen.EditorScreen
import com.appthere.mdwriter.ui.theme.MdWriterTheme

/**
 * Main application composable with navigation between document list and editor
 */
@Composable
fun App() {
    // Initialize platform-specific dependencies
    val dependencies = remember { createAppDependencies() }

    // Track current screen
    var currentScreen by remember { mutableStateOf<Screen>(Screen.DocumentList) }

    MdWriterTheme {
        when (val screen = currentScreen) {
            is Screen.DocumentList -> {
                // Create document list ViewModel
                val listViewModel = remember {
                    DocumentListViewModel(
                        documentRepository = dependencies.documentRepository,
                        recentDocumentsManager = dependencies.recentDocumentsManager
                    )
                }

                val listState by listViewModel.state.collectAsState()

                DocumentListScreen(
                    state = listState,
                    onIntent = listViewModel::handleIntent,
                    onNavigateToEditor = { documentInfo ->
                        currentScreen = Screen.Editor(documentInfo)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            is Screen.Editor -> {
                // For now, EditorScreen creates its own ViewModel
                // TODO: Pass documentInfo to editor for loading specific document
                EditorScreen(
                    onNavigateBack = {
                        currentScreen = Screen.DocumentList
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Sealed class representing different screens in the app
 */
sealed class Screen {
    data object DocumentList : Screen()
    data class Editor(val documentInfo: DocumentInfo) : Screen()
}
