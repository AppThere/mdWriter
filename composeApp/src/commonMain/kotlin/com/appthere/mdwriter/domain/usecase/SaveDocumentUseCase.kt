package com.appthere.mdwriter.domain.usecase

import com.appthere.mdwriter.data.model.Document
import com.appthere.mdwriter.data.model.Metadata
import com.appthere.mdwriter.data.repository.DocumentRepository
import com.appthere.mdwriter.domain.model.Result
import kotlinx.datetime.Clock

/**
 * Use case for saving a document to storage
 * Updates the modified timestamp before saving
 */
class SaveDocumentUseCase(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(path: String, document: Document): Result<Unit> {
        // Update modified timestamp
        val updatedDocument = document.copy(
            metadata = document.metadata.copy(
                modified = Clock.System.now()
            )
        )
        return repository.saveDocument(path, updatedDocument)
    }
}
