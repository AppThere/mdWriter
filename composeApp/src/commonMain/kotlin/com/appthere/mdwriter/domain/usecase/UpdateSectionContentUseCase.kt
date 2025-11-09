package com.appthere.mdwriter.domain.usecase

import com.appthere.mdwriter.data.model.Document
import com.appthere.mdwriter.data.model.Section

/**
 * Use case for updating a section's content in a document
 */
class UpdateSectionContentUseCase {
    operator fun invoke(
        document: Document,
        sectionId: String,
        newContent: String
    ): Document {
        val section = document.sections[sectionId] ?: return document

        val updatedSection = section.copy(content = newContent)
        val updatedSections = document.sections.toMutableMap()
        updatedSections[sectionId] = updatedSection

        return document.copy(sections = updatedSections)
    }
}
