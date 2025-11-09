package com.appthere.mdwriter.domain.usecase

import com.appthere.mdwriter.data.model.Document
import com.appthere.mdwriter.data.repository.DocumentRepository

/**
 * Use case for loading a document from storage
 */
class LoadDocumentUseCase(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(path: String): Result<Document> {
        return repository.loadDocument(path)
    }
}
