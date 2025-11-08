package com.appthere.mdwriter.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DocumentTest {

    @Test
    fun `valid minimal document should validate successfully`() {
        val document = Document(
            metadata = Metadata(title = "Test Document"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(
                    id = "section-1",
                    content = "# Test"
                )
            )
        )
        val result = document.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `document with empty spine should fail validation`() {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = emptyList(),
            sections = emptyMap()
        )
        val result = document.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrors().any { it.contains("Spine must contain at least one") })
    }

    @Test
    fun `document with invalid version format should fail validation`() {
        val document = Document(
            version = "v1",
            metadata = Metadata(title = "Test"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test")
            )
        )
        val result = document.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrors().any { it.contains("Invalid version format") })
    }

    @Test
    fun `document with duplicate spine IDs should fail validation`() {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("section-1", "section-2", "section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test"),
                "section-2" to Section(id = "section-2", content = "Test")
            )
        )
        val result = document.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrors().any { it.contains("duplicate section IDs") })
    }

    @Test
    fun `document with spine referencing non-existent section should fail validation`() {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("section-1", "section-2"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test")
            )
        )
        val result = document.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrors().any { it.contains("non-existent section") })
    }

    @Test
    fun `document with section not in spine should fail validation`() {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test"),
                "section-2" to Section(id = "section-2", content = "Test")
            )
        )
        val result = document.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrors().any { it.contains("not in spine") })
    }

    @Test
    fun `document with section ID mismatch should fail validation`() {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "wrong-id", content = "Test")
            )
        )
        val result = document.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrors().any { it.contains("Section ID mismatch") })
    }

    @Test
    fun `document with duplicate stylesheet IDs should fail validation`() {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test")
            ),
            stylesheets = listOf(
                Stylesheet(id = "style-1", name = "Style 1", content = "body {}"),
                Stylesheet(id = "style-1", name = "Style 1 Duplicate", content = "p {}")
            )
        )
        val result = document.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrors().any { it.contains("Duplicate stylesheet IDs") })
    }

    @Test
    fun `document with section referencing non-existent stylesheet should fail validation`() {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(
                    id = "section-1",
                    content = "Test",
                    stylesheets = listOf("non-existent")
                )
            )
        )
        val result = document.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrors().any { it.contains("non-existent stylesheet") })
    }

    @Test
    fun `document with settings referencing non-existent stylesheet should fail validation`() {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test")
            ),
            settings = DocumentSettings(
                defaultStylesheets = listOf("non-existent")
            )
        )
        val result = document.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrors().any { it.contains("non-existent default stylesheet") })
    }

    @Test
    fun `complete valid document should validate successfully`() {
        val document = Document(
            metadata = Metadata(title = "Complete Document"),
            spine = listOf("intro", "chapter-1"),
            sections = mapOf(
                "intro" to Section(
                    id = "intro",
                    title = "Introduction",
                    content = "# Introduction",
                    stylesheets = listOf("base")
                ),
                "chapter-1" to Section(
                    id = "chapter-1",
                    title = "Chapter 1",
                    content = "# Chapter 1",
                    stylesheets = listOf("base", "chapter")
                )
            ),
            stylesheets = listOf(
                Stylesheet(
                    id = "base",
                    name = "Base Style",
                    content = "body { font-family: serif; }",
                    scope = StylesheetScope.GLOBAL
                ),
                Stylesheet(
                    id = "chapter",
                    name = "Chapter Style",
                    content = ".chapter-heading { font-size: 2em; }",
                    scope = StylesheetScope.MANUAL
                )
            ),
            settings = DocumentSettings(
                defaultStylesheets = listOf("base")
            )
        )
        val result = document.validate()

        assertTrue(result.isValid(), "Errors: ${result.getErrors()}")
    }

    @Test
    fun `getSectionsInOrder should return sections in spine order`() {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("section-2", "section-1", "section-3"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "1"),
                "section-2" to Section(id = "section-2", content = "2"),
                "section-3" to Section(id = "section-3", content = "3")
            )
        )

        val sections = document.getSectionsInOrder()

        assertEquals(3, sections.size)
        assertEquals("section-2", sections[0].id)
        assertEquals("section-1", sections[1].id)
        assertEquals("section-3", sections[2].id)
    }

    @Test
    fun `getGlobalStylesheets should return global scope stylesheets`() {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test")
            ),
            stylesheets = listOf(
                Stylesheet(id = "global-1", name = "Global 1", content = "", scope = StylesheetScope.GLOBAL),
                Stylesheet(id = "manual-1", name = "Manual 1", content = "", scope = StylesheetScope.MANUAL)
            )
        )

        val globalStylesheets = document.getGlobalStylesheets()

        assertEquals(1, globalStylesheets.size)
        assertEquals("global-1", globalStylesheets[0].id)
    }

    @Test
    fun `getGlobalStylesheets should include default stylesheets from settings`() {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test")
            ),
            stylesheets = listOf(
                Stylesheet(id = "base", name = "Base", content = "", scope = StylesheetScope.MANUAL)
            ),
            settings = DocumentSettings(
                defaultStylesheets = listOf("base")
            )
        )

        val globalStylesheets = document.getGlobalStylesheets()

        assertEquals(1, globalStylesheets.size)
        assertEquals("base", globalStylesheets[0].id)
    }

    @Test
    fun `getStylesheetsForSection should include global and section-specific stylesheets`() {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(
                    id = "section-1",
                    content = "Test",
                    stylesheets = listOf("chapter")
                )
            ),
            stylesheets = listOf(
                Stylesheet(id = "base", name = "Base", content = "", scope = StylesheetScope.GLOBAL),
                Stylesheet(id = "chapter", name = "Chapter", content = "", scope = StylesheetScope.MANUAL)
            )
        )

        val stylesheets = document.getStylesheetsForSection("section-1")

        assertEquals(2, stylesheets.size)
        assertEquals("base", stylesheets[0].id)
        assertEquals("chapter", stylesheets[1].id)
    }

    @Test
    fun `createNew should create valid minimal document`() {
        val document = Document.createNew("New Document")

        assertEquals("New Document", document.metadata.title)
        assertTrue(document.spine.isEmpty())
        assertTrue(document.sections.isEmpty())
        assertEquals("1.0", document.version)
    }

    @Test
    fun `document should collect multiple validation errors`() {
        val document = Document(
            version = "invalid",
            metadata = Metadata(title = ""),
            spine = emptyList(),
            sections = emptyMap()
        )
        val result = document.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrors().size >= 3)
    }
}
