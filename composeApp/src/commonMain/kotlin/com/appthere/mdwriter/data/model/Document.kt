package com.appthere.mdwriter.data.model

import kotlinx.serialization.Serializable

/**
 * Complete document structure with metadata, sections, and stylesheets
 * Represents a multipart Markdown document with styling and resources
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

    companion object {
        /**
         * Create a new document with default values
         */
        fun create(
            title: String = "Untitled Document",
            author: String = ""
        ): Document {
            val timestamp = System.currentTimeMillis()
            val id = generateId()
            return Document(
                metadata = Metadata(
                    title = title,
                    author = author,
                    created = timestamp,
                    modified = timestamp,
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
        }

        /**
         * Generate a unique document ID
         */
        fun generateId(): String {
            return "doc-${System.currentTimeMillis()}"
        }
    }
}

/**
 * Document info for list display (lighter weight than full Document)
 */
@Serializable
data class DocumentInfo(
    val id: String,
    val title: String,
    val author: String,
    val created: Long, // epoch milliseconds
    val modified: Long, // epoch milliseconds
    val filePath: String,
    val wordCount: Int = 0,
    val sectionCount: Int = 0
)
