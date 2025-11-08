package com.appthere.mdwriter.data.model

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MetadataTest {

    @Test
    fun `valid metadata with only title should validate successfully`() {
        val metadata = Metadata(title = "Test Document")
        val result = metadata.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `metadata with blank title should fail validation`() {
        val metadata = Metadata(title = "   ")
        val result = metadata.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Title is required") })
    }

    @Test
    fun `metadata with empty title should fail validation`() {
        val metadata = Metadata(title = "")
        val result = metadata.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Title is required") })
    }

    @Test
    fun `metadata with valid language code should validate successfully`() {
        val metadata = Metadata(
            title = "Test Document",
            language = "en"
        )
        val result = metadata.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `metadata with language code with region should validate successfully`() {
        val metadata = Metadata(
            title = "Test Document",
            language = "en-US"
        )
        val result = metadata.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `metadata with invalid language code should fail validation`() {
        val metadata = Metadata(
            title = "Test Document",
            language = "english"
        )
        val result = metadata.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Invalid language code") })
    }

    @Test
    fun `metadata with invalid language code uppercase should fail validation`() {
        val metadata = Metadata(
            title = "Test Document",
            language = "EN"
        )
        val result = metadata.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Invalid language code") })
    }

    @Test
    fun `metadata with valid date format should validate successfully`() {
        val metadata = Metadata(
            title = "Test Document",
            date = "2025-11-08"
        )
        val result = metadata.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `metadata with invalid date format should fail validation`() {
        val metadata = Metadata(
            title = "Test Document",
            date = "11/08/2025"
        )
        val result = metadata.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Invalid date format") })
    }

    @Test
    fun `metadata with all Dublin Core fields should validate successfully`() {
        val metadata = Metadata(
            title = "Complete Document",
            creator = "John Doe",
            subject = "Testing",
            description = "A comprehensive test document",
            publisher = "Test Publisher",
            contributor = listOf("Jane Smith", "Bob Johnson"),
            date = "2025-11-08",
            type = "Text",
            format = "text/markdown",
            identifier = "uuid:550e8400-e29b-41d4-a716-446655440000",
            source = "Original Work",
            language = "en",
            relation = "Related Document",
            coverage = "Global",
            rights = "Copyright 2025",
            created = Instant.parse("2025-11-08T10:00:00Z"),
            modified = Instant.parse("2025-11-08T15:30:00Z")
        )
        val result = metadata.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `metadata with custom fields should validate successfully`() {
        val metadata = Metadata(
            title = "Test Document",
            custom = mapOf(
                "wordCount" to "5432",
                "readingTime" to "27 minutes",
                "tags" to "tag1,tag2"
            )
        )
        val result = metadata.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `metadata validation should collect multiple errors`() {
        val metadata = Metadata(
            title = "",
            language = "invalid",
            date = "invalid-date"
        )
        val result = metadata.validate()

        assertFalse(result.isValid())
        assertEquals(3, result.getErrorsOrEmpty().size)
    }
}
