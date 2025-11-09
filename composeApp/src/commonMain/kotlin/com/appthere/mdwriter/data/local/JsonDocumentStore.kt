package com.appthere.mdwriter.data.local

import com.appthere.mdwriter.data.model.Document
import com.appthere.mdwriter.data.model.DocumentInfo
import com.appthere.mdwriter.data.repository.DocumentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.appthere.mdwriter.util.now
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * JSON-based document storage implementation
 */
class JsonDocumentStore(
    private val fileSystem: FileSystem
) : DocumentRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val documentsFlow = MutableStateFlow<List<DocumentInfo>>(emptyList())
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        // Initialize by loading documents asynchronously
        scope.launch {
            refreshDocumentsList()
        }
    }

    override fun getAllDocuments(): Flow<List<DocumentInfo>> = documentsFlow

    override suspend fun getDocument(id: String): Result<Document> {
        return try {
            val path = getDocumentPath(id)
            fileSystem.readFile(path).mapCatching { content ->
                json.decodeFromString<Document>(content)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loadDocument(path: String): Result<Document> {
        return try {
            fileSystem.readFile(path).mapCatching { content ->
                json.decodeFromString<Document>(content)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveDocument(document: Document): Result<Unit> {
        return try {
            fileSystem.ensureDocumentsDirectoryExists()
            val id = document.metadata.identifier.ifBlank { Document.generateId() }
            // Ensure document has the correct identifier
            val documentToStore = if (document.metadata.identifier.isBlank()) {
                document.copy(metadata = document.metadata.copy(identifier = id))
            } else {
                document
            }
            val path = getDocumentPath(id)
            val content = json.encodeToString(documentToStore)
            val writeResult = fileSystem.writeFile(path, content)
            writeResult.also {
                refreshDocumentsList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun saveDocumentToPath(path: String, document: Document): Result<Unit> {
        return try {
            val content = json.encodeToString(document)
            fileSystem.writeFile(path, content).also {
                refreshDocumentsList()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createDocument(): Result<Document> {
        return try {
            val document = Document.create()
            saveDocument(document).map { document }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDocument(id: String): Result<Unit> {
        return try {
            val path = getDocumentPath(id)
            fileSystem.deleteFile(path).also {
                refreshDocumentsList()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDocumentAtPath(path: String): Result<Unit> {
        return try {
            fileSystem.deleteFile(path).also {
                refreshDocumentsList()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun renameDocument(id: String, newTitle: String): Result<Unit> {
        return try {
            getDocument(id).fold(
                onSuccess = { document ->
                    val updated = document.copy(
                        metadata = document.metadata.copy(
                            title = newTitle,
                            modified = now()
                        )
                    )
                    saveDocument(updated)
                },
                onFailure = { Result.failure<Unit>(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun exportDocument(document: Document, exportPath: String): Result<Unit> {
        return try {
            val content = json.encodeToString(document)
            fileSystem.writeFile(exportPath, content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun importDocument(importPath: String): Result<Document> {
        return try {
            fileSystem.readFile(importPath).mapCatching { content ->
                val document = json.decodeFromString<Document>(content)
                // Save to local storage
                saveDocument(document)
                document
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchDocuments(query: String): Flow<List<DocumentInfo>> {
        return documentsFlow.map { docs ->
            docs.filter { doc ->
                doc.title.contains(query, ignoreCase = true) ||
                doc.author.contains(query, ignoreCase = true)
            }
        }
    }

    override suspend fun listDocuments(): Result<List<String>> {
        return try {
            Result.success(fileSystem.listDocumentFiles())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun refreshDocumentsList() {
        try {
            val files = fileSystem.listDocumentFiles()
            val documents = files.mapNotNull { path ->
                fileSystem.readFile(path).getOrNull()?.let { content ->
                    try {
                        val doc = json.decodeFromString<Document>(content)
                        val id = doc.metadata.identifier.ifBlank {
                            path.substringAfterLast("/").substringBeforeLast(".mddoc")
                        }
                        DocumentInfo(
                            id = id,
                            title = doc.metadata.title,
                            author = doc.metadata.author,
                            created = doc.metadata.created,
                            modified = doc.metadata.modified,
                            filePath = path,
                            wordCount = calculateWordCount(doc),
                            sectionCount = doc.sections.size
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            documentsFlow.value = documents.sortedByDescending { it.modified }
        } catch (e: Exception) {
            // Log error or handle
        }
    }

    private fun calculateWordCount(document: Document): Int {
        return document.sections.values.sumOf { section ->
            section.content.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        }
    }

    private fun getDocumentPath(id: String): String {
        val docsDir = fileSystem.getDocumentsDirectory()
        return "$docsDir/$id.mddoc"
    }
}
