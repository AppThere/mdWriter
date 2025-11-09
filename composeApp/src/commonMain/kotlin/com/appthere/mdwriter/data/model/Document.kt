package com.appthere.mdwriter.data.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * Complete document structure with metadata, sections, and stylesheets
 */
@Serializable
data class Document(
    val id: String,
    val metadata: Metadata,
    val spine: List<String> = emptyList(), // Ordered list of section IDs
    val sections: Map<String, Section> = emptyMap(),
    val stylesheets: List<Stylesheet> = emptyList(),
    val resources: Resources = Resources(),
    val settings: DocumentSettings = DocumentSettings()
) {
    companion object {
        fun create(
            id: String = generateId(),
            title: String = "Untitled Document",
            author: String = ""
        ): Document {
            val now = Clock.System.now()
            return Document(
                id = id,
                metadata = Metadata(
                    title = title,
                    author = author,
                    created = now,
                    modified = now
                )
            )
        }

        private fun generateId(): String {
            return "doc-${Clock.System.now().toEpochMilliseconds()}"
        }
    }
}

@Serializable
data class Resources(
    val fonts: List<String> = emptyList(),
    val images: List<String> = emptyList()
)

@Serializable
data class DocumentSettings(
    val defaultStylesheets: List<String> = emptyList() // IDs of global stylesheets
)

/**
 * Document info for list display (lighter weight than full Document)
 */
@Serializable
data class DocumentInfo(
    val id: String,
    val title: String,
    val author: String,
    val created: kotlinx.datetime.Instant,
    val modified: kotlinx.datetime.Instant,
    val filePath: String,
    val wordCount: Int = 0,
    val sectionCount: Int = 0
)
