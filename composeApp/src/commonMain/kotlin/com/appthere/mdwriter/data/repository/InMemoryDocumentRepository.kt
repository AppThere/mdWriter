package com.appthere.mdwriter.data.repository

import com.appthere.mdwriter.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

/**
 * In-memory implementation of DocumentRepository for testing and development
 */
class InMemoryDocumentRepository : DocumentRepository {
    private val documents = mutableMapOf<String, MutableStateFlow<Document>>()
    private val documentInfoFlow = MutableStateFlow<List<DocumentInfo>>(emptyList())

    override fun getAllDocuments(): Flow<List<DocumentInfo>> = documentInfoFlow

    override suspend fun getDocument(id: String): Result<Document> = runCatching {
        documents[id]?.value ?: throw IllegalArgumentException("Document not found: $id")
    }

    override suspend fun loadDocument(path: String): Result<Document> = runCatching {
        documents[path]?.value ?: throw IllegalArgumentException("Document not found: $path")
    }

    override suspend fun saveDocument(document: Document): Result<Unit> = runCatching {
        val id = document.metadata.identifier.ifBlank { Document.generateId() }
        if (documents.containsKey(id)) {
            documents[id]?.value = document
        } else {
            documents[id] = MutableStateFlow(document)
        }
        updateDocumentInfoList()
    }

    override suspend fun saveDocumentToPath(path: String, document: Document): Result<Unit> = runCatching {
        if (documents.containsKey(path)) {
            documents[path]?.value = document
        } else {
            documents[path] = MutableStateFlow(document)
        }
        updateDocumentInfoList()
    }

    override suspend fun createDocument(): Result<Document> = runCatching {
        val now = Clock.System.now()
        val id = Document.generateId()
        val document = Document(
            metadata = Metadata(
                title = "Untitled Document",
                author = "",
                created = now,
                modified = now,
                language = "en",
                identifier = id
            ),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(
                    id = "section-1",
                    content = "",
                    order = 0
                )
            )
        )
        documents[id] = MutableStateFlow(document)
        updateDocumentInfoList()
        document
    }

    override suspend fun deleteDocument(id: String): Result<Unit> = runCatching {
        documents.remove(id) ?: throw IllegalArgumentException("Document not found: $id")
        updateDocumentInfoList()
    }

    override suspend fun deleteDocumentAtPath(path: String): Result<Unit> = runCatching {
        documents.remove(path) ?: throw IllegalArgumentException("Document not found: $path")
        updateDocumentInfoList()
    }

    override suspend fun renameDocument(id: String, newTitle: String): Result<Unit> = runCatching {
        val document = documents[id]?.value ?: throw IllegalArgumentException("Document not found: $id")
        val updated = document.copy(
            metadata = document.metadata.copy(
                title = newTitle,
                modified = Clock.System.now()
            )
        )
        documents[id]?.value = updated
        updateDocumentInfoList()
    }

    override suspend fun exportDocument(document: Document, exportPath: String): Result<Unit> = runCatching {
        // In-memory implementation doesn't actually export to filesystem
        // This is just for interface compatibility
        Unit
    }

    override suspend fun importDocument(importPath: String): Result<Document> = runCatching {
        // In-memory implementation doesn't actually import from filesystem
        // Just return a new document
        createDocument().getOrThrow()
    }

    override fun searchDocuments(query: String): Flow<List<DocumentInfo>> {
        return documentInfoFlow.map { docs ->
            docs.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.author.contains(query, ignoreCase = true)
            }
        }
    }

    override suspend fun listDocuments(): Result<List<String>> = runCatching {
        documents.keys.toList()
    }

    /**
     * Observe changes to a specific document
     */
    fun observeDocument(path: String): Flow<Result<Document>> {
        return documents.getOrPut(path) {
            val now = Clock.System.now()
            val initialDocument = Document(
                metadata = Metadata(
                    title = "Untitled Document",
                    author = "",
                    created = now,
                    modified = now,
                    language = "en"
                ),
                spine = listOf("section-1"),
                sections = mapOf(
                    "section-1" to Section(
                        id = "section-1",
                        content = "",
                        order = 0
                    )
                )
            )
            MutableStateFlow(initialDocument)
        }.map { Result.success(it) }
    }

    private fun updateDocumentInfoList() {
        documentInfoFlow.value = documents.map { (id, flow) ->
            val doc = flow.value
            DocumentInfo(
                id = id,
                title = doc.metadata.title,
                author = doc.metadata.author,
                created = doc.metadata.created,
                modified = doc.metadata.modified,
                filePath = id,
                wordCount = doc.sections.values.sumOf { section ->
                    section.content.split(Regex("\\s+")).filter { it.isNotBlank() }.size
                },
                sectionCount = doc.sections.size
            )
        }
    }
}
