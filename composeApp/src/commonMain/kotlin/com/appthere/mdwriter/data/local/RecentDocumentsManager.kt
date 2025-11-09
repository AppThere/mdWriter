package com.appthere.mdwriter.data.local

import com.appthere.mdwriter.data.model.DocumentInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.appthere.mdwriter.util.now
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Manages recently opened documents for quick access
 */
class RecentDocumentsManager(
    private val fileSystem: FileSystem
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val recentDocumentsFlow = MutableStateFlow<List<RecentDocument>>(emptyList())
    private val maxRecentDocuments = 10
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        // Load recent documents asynchronously
        scope.launch {
            loadRecentDocuments()
        }
    }

    /**
     * Get recent documents as a flow
     */
    fun getRecentDocuments(): Flow<List<RecentDocument>> = recentDocumentsFlow

    /**
     * Add a document to recent list
     */
    suspend fun addRecentDocument(documentInfo: DocumentInfo) {
        val recent = RecentDocument(
            id = documentInfo.id,
            title = documentInfo.title,
            lastOpened = now(),
            filePath = documentInfo.filePath
        )

        val currentList = recentDocumentsFlow.value.toMutableList()

        // Remove existing entry if present
        currentList.removeAll { it.id == recent.id }

        // Add to front
        currentList.add(0, recent)

        // Keep only max recent documents
        val trimmedList = currentList.take(maxRecentDocuments)

        recentDocumentsFlow.value = trimmedList
        saveRecentDocuments(trimmedList)
    }

    /**
     * Remove a document from recent list
     */
    suspend fun removeRecentDocument(documentId: String) {
        val currentList = recentDocumentsFlow.value.toMutableList()
        currentList.removeAll { it.id == documentId }
        recentDocumentsFlow.value = currentList
        saveRecentDocuments(currentList)
    }

    /**
     * Clear all recent documents
     */
    suspend fun clearRecentDocuments() {
        recentDocumentsFlow.value = emptyList()
        saveRecentDocuments(emptyList())
    }

    private suspend fun loadRecentDocuments() {
        try {
            val path = getRecentDocumentsPath()
            fileSystem.readFile(path).getOrNull()?.let { content ->
                val recent = json.decodeFromString<List<RecentDocument>>(content)
                recentDocumentsFlow.value = recent
            }
        } catch (e: Exception) {
            // If file doesn't exist or is corrupted, start fresh
            recentDocumentsFlow.value = emptyList()
        }
    }

    private suspend fun saveRecentDocuments(documents: List<RecentDocument>) {
        try {
            fileSystem.ensureDocumentsDirectoryExists()
            val path = getRecentDocumentsPath()
            val content = json.encodeToString(documents)
            fileSystem.writeFile(path, content)
        } catch (e: Exception) {
            // Log error or handle
        }
    }

    private fun getRecentDocumentsPath(): String {
        val docsDir = fileSystem.getDocumentsDirectory()
        return "$docsDir/.recent_documents.json"
    }
}

@Serializable
data class RecentDocument(
    val id: String,
    val title: String,
    val lastOpened: Long, // epoch milliseconds
    val filePath: String
)
