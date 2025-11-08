package com.appthere.mdwriter.data.repository

import com.appthere.mdwriter.data.model.*
import com.appthere.mdwriter.domain.model.Result
import com.appthere.mdwriter.domain.model.resultOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime

/**
 * In-memory implementation of DocumentRepository for testing and development
 */
class InMemoryDocumentRepository : DocumentRepository {
    private val documents = mutableMapOf<String, MutableStateFlow<Document>>()

    override suspend fun loadDocument(path: String): Result<Document> = resultOf {
        documents[path]?.value ?: throw IllegalArgumentException("Document not found: $path")
    }

    override suspend fun saveDocument(path: String, document: Document): Result<Unit> = resultOf {
        if (documents.containsKey(path)) {
            documents[path]?.value = document
        } else {
            documents[path] = MutableStateFlow(document)
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun createDocument(): Result<Document> = resultOf {
        val now = kotlin.time.Clock.System.now()
        Document(
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
    }

    override suspend fun deleteDocument(path: String): Result<Unit> = resultOf {
        documents.remove(path) ?: throw IllegalArgumentException("Document not found: $path")
        Unit
    }

    override suspend fun listDocuments(): Result<List<String>> = resultOf {
        documents.keys.toList()
    }

    @OptIn(ExperimentalTime::class)
    override fun observeDocument(path: String): Flow<Result<Document>> {
        return documents.getOrPut(path) {
            // Create initial document without suspend
            val now = kotlin.time.Clock.System.now()
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
        }.map { Result.Success(it) }
    }
}
