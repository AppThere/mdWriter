package com.appthere.mdwriter.data.model

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SectionTest {

    @Test
    fun `valid section should validate successfully`() {
        val section = Section(
            id = "section-001",
            content = "# Test Content"
        )
        val result = section.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `section with blank id should fail validation`() {
        val section = Section(
            id = "",
            content = "Test"
        )
        val result = section.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Section ID is required") })
    }

    @Test
    fun `section with invalid id format should fail validation`() {
        val section = Section(
            id = "Section 001",
            content = "Test"
        )
        val result = section.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Invalid section ID format") })
    }

    @Test
    fun `section with uppercase in id should fail validation`() {
        val section = Section(
            id = "Section-001",
            content = "Test"
        )
        val result = section.validate()

        assertFalse(result.isValid())
    }

    @Test
    fun `section with valid id containing hyphens and underscores should validate`() {
        val section = Section(
            id = "section_001-test",
            content = "Test"
        )
        val result = section.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `section with empty content should validate successfully`() {
        val section = Section(
            id = "section-001",
            content = ""
        )
        val result = section.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `section with invalid stylesheet id should fail validation`() {
        val section = Section(
            id = "section-001",
            content = "Test",
            stylesheets = listOf("valid-id", "Invalid ID")
        )
        val result = section.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Invalid stylesheet ID") })
    }

    @Test
    fun `section metadata with negative word count should fail validation`() {
        val section = Section(
            id = "section-001",
            content = "Test",
            metadata = SectionMetadata(wordCount = -10)
        )
        val result = section.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Word count cannot be negative") })
    }

    @Test
    fun `section metadata with negative char count should fail validation`() {
        val section = Section(
            id = "section-001",
            content = "Test",
            metadata = SectionMetadata(charCount = -100)
        )
        val result = section.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Character count cannot be negative") })
    }

    @Test
    fun `section with frontmatter should extract correctly`() {
        val content = """---
title: Test Section
author: John Doe
---

# Section Content

This is the actual content.
"""
        val section = Section(
            id = "section-001",
            content = content
        )

        val (frontmatter, mainContent) = section.extractFrontmatter()

        assertNotNull(frontmatter)
        assertTrue(frontmatter.contains("title: Test Section"))
        assertTrue(frontmatter.contains("author: John Doe"))
        assertTrue(mainContent.startsWith("# Section Content"))
        assertFalse(mainContent.contains("---"))
    }

    @Test
    fun `section without frontmatter should return null frontmatter`() {
        val content = "# Section Content\n\nNo frontmatter here."
        val section = Section(
            id = "section-001",
            content = content
        )

        val (frontmatter, mainContent) = section.extractFrontmatter()

        assertNull(frontmatter)
        assertEquals(content, mainContent)
    }

    @Test
    fun `section with incomplete frontmatter should return null frontmatter`() {
        val content = """---
title: Test Section
# Missing closing delimiter

Content here
"""
        val section = Section(
            id = "section-001",
            content = content
        )

        val (frontmatter, mainContent) = section.extractFrontmatter()

        assertNull(frontmatter)
        assertEquals(content, mainContent)
    }

    @Test
    fun `getFrontmatter should return frontmatter if present`() {
        val content = """---
title: Test
---

Content"""
        val section = Section(id = "section-001", content = content)

        val frontmatter = section.getFrontmatter()

        assertNotNull(frontmatter)
        assertTrue(frontmatter.contains("title: Test"))
    }

    @Test
    fun `getContentWithoutFrontmatter should return content without frontmatter`() {
        val content = """---
title: Test
---

# Main Content"""
        val section = Section(id = "section-001", content = content)

        val mainContent = section.getContentWithoutFrontmatter()

        assertTrue(mainContent.startsWith("# Main Content"))
        assertFalse(mainContent.contains("title: Test"))
    }

    @Test
    fun `section with all fields should validate successfully`() {
        val section = Section(
            id = "chapter-001",
            title = "Chapter One",
            content = "# Content",
            order = 1,
            stylesheets = listOf("style-1", "style-2"),
            metadata = SectionMetadata(
                wordCount = 250,
                charCount = 1420,
                created = Instant.parse("2025-11-08T10:00:00Z"),
                modified = Instant.parse("2025-11-08T10:15:00Z")
            ),
            locked = false,
            hidden = false
        )
        val result = section.validate()

        assertTrue(result.isValid())
    }
}
