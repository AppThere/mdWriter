package com.appthere.mdwriter.domain.usecase

import com.appthere.mdwriter.data.model.Document
import com.appthere.mdwriter.data.repository.DocumentRepository

/**
 * Use case for creating a new document
 */
class CreateDocumentUseCase(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(): Result<Document> {
        return repository.createDocument()
    }
}
