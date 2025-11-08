package com.appthere.mdwriter.data.model

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentMetadataTest {

    @Test
    fun `fromDocument should extract correct metadata`() {
        val document = Document(
            metadata = Metadata(
                title = "Test Document",
                creator = "John Doe",
                language = "en",
                created = Instant.parse("2025-11-08T10:00:00Z"),
                modified = Instant.parse("2025-11-08T15:30:00Z")
            ),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test")
            )
        )

        val metadata = DocumentMetadata.fromDocument("doc-001", document, 1024)

        assertEquals("doc-001", metadata.id)
        assertEquals("Test Document", metadata.title)
        assertEquals("John Doe", metadata.creator)
        assertEquals("en", metadata.language)
        assertEquals(Instant.parse("2025-11-08T10:00:00Z"), metadata.created)
        assertEquals(Instant.parse("2025-11-08T15:30:00Z"), metadata.modified)
        assertEquals(1024L, metadata.fileSize)
    }

    @Test
    fun `fromDocument should extract tags from custom metadata`() {
        val document = Document(
            metadata = Metadata(
                title = "Test",
                custom = mapOf("tags" to "kotlin, multiplatform, mobile")
            ),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test")
            )
        )

        val metadata = DocumentMetadata.fromDocument("doc-001", document)

        assertEquals(3, metadata.tags.size)
        assertTrue(metadata.tags.contains("kotlin"))
        assertTrue(metadata.tags.contains("multiplatform"))
        assertTrue(metadata.tags.contains("mobile"))
    }

    @Test
    fun `fromDocument should handle missing tags`() {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test")
            )
        )

        val metadata = DocumentMetadata.fromDocument("doc-001", document)

        assertTrue(metadata.tags.isEmpty())
    }

    @Test
    fun `fromDocument should handle null fileSize`() {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test")
            )
        )

        val metadata = DocumentMetadata.fromDocument("doc-001", document, null)

        assertEquals(null, metadata.fileSize)
    }
}
