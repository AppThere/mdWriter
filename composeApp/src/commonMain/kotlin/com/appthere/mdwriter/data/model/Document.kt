package com.appthere.mdwriter.data.model

import kotlinx.serialization.Serializable

/**
 * Complete document model with multipart Markdown content
 */
@Serializable
data class Document(
    val metadata: Metadata,
    val spine: List<String> = emptyList(), // Ordered list of section IDs
    val sections: Map<String, Section> = emptyMap(),
    val stylesheets: List<Stylesheet> = emptyList(),
    val resources: DocumentResources = DocumentResources(),
    val settings: DocumentSettings = DocumentSettings()
) {
    /**
     * Get sections in spine order
     */
    fun getSectionsInOrder(): List<Section> {
        return spine.mapNotNull { sectionId ->
            sections[sectionId]
        }
    }

    /**
     * Get all active stylesheets for a section
     * Includes global stylesheets and section-specific ones
     */
    fun getActiveStylesheetsForSection(sectionId: String): List<Stylesheet> {
        val section = sections[sectionId] ?: return emptyList()

        val globalStylesheetIds = settings.defaultStylesheets
        val sectionStylesheetIds = section.stylesheets

        val activeIds = (globalStylesheetIds + sectionStylesheetIds).distinct()

        return stylesheets.filter { it.id in activeIds }
    }

    /**
     * Get stylesheet by ID
     */
    fun getStylesheet(id: String): Stylesheet? {
        return stylesheets.find { it.id == id }
    }

    /**
     * Get section by ID
     */
    fun getSection(id: String): Section? {
        return sections[id]
    }
}
