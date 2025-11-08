package com.appthere.mdwriter.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DocumentSerializationTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun `minimal document should serialize and deserialize correctly`() {
        val document = Document(
            metadata = Metadata(title = "Test Document"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(
                    id = "section-1",
                    content = "# Test Content"
                )
            )
        )

        val jsonString = json.encodeToString(document)
        val deserialized = json.decodeFromString<Document>(jsonString)

        assertEquals(document.metadata.title, deserialized.metadata.title)
        assertEquals(document.spine, deserialized.spine)
        assertEquals(document.sections.size, deserialized.sections.size)
        assertEquals(document.sections["section-1"]?.content, deserialized.sections["section-1"]?.content)
    }

    @Test
    fun `document with Instant fields should serialize to ISO 8601 format`() {
        val created = Instant.parse("2025-11-08T10:00:00Z")
        val modified = Instant.parse("2025-11-08T15:30:00Z")

        val document = Document(
            metadata = Metadata(
                title = "Test",
                created = created,
                modified = modified
            ),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test")
            )
        )

        val jsonString = json.encodeToString(document)

        assertTrue(jsonString.contains("2025-11-08T10:00:00Z"))
        assertTrue(jsonString.contains("2025-11-08T15:30:00Z"))
    }

    @Test
    fun `document with Instant fields should deserialize from ISO 8601 format`() {
        val jsonString = """
        {
            "version": "1.0",
            "metadata": {
                "title": "Test",
                "created": "2025-11-08T10:00:00Z",
                "modified": "2025-11-08T15:30:00Z"
            },
            "spine": ["section-1"],
            "sections": {
                "section-1": {
                    "id": "section-1",
                    "content": "Test"
                }
            }
        }
        """.trimIndent()

        val document = json.decodeFromString<Document>(jsonString)

        assertNotNull(document.metadata.created)
        assertNotNull(document.metadata.modified)
        assertEquals("2025-11-08T10:00:00Z", document.metadata.created.toString())
        assertEquals("2025-11-08T15:30:00Z", document.metadata.modified.toString())
    }

    @Test
    fun `complete document should serialize and deserialize correctly`() {
        val document = Document(
            schema = "https://example.com/mdoc-schema-v1.json",
            version = "1.0",
            metadata = Metadata(
                title = "Technical Guide",
                creator = "Jane Smith",
                date = "2025-11-08",
                language = "en",
                created = Instant.parse("2025-11-08T09:00:00Z"),
                modified = Instant.parse("2025-11-08T16:30:00Z"),
                custom = mapOf(
                    "tags" to "tutorial,technical",
                    "category" to "documentation"
                )
            ),
            spine = listOf("intro", "chapter-1"),
            sections = mapOf(
                "intro" to Section(
                    id = "intro",
                    title = "Introduction",
                    content = "---\ntitle: Introduction\n---\n\n# Introduction {.chapter-heading}\n\nWelcome.",
                    order = 0,
                    stylesheets = emptyList(),
                    metadata = SectionMetadata(wordCount = 50)
                ),
                "chapter-1" to Section(
                    id = "chapter-1",
                    title = "Chapter 1",
                    content = "# Chapter 1",
                    order = 1,
                    stylesheets = listOf("chapter-style")
                )
            ),
            stylesheets = listOf(
                Stylesheet(
                    id = "base",
                    name = "Base Stylesheet",
                    content = "body { font-family: 'Georgia', serif; }",
                    enabled = true,
                    priority = 0,
                    scope = StylesheetScope.GLOBAL
                ),
                Stylesheet(
                    id = "chapter-style",
                    name = "Chapter Stylesheet",
                    content = ".chapter-heading { color: #2c3e50; }",
                    enabled = true,
                    priority = 10,
                    scope = StylesheetScope.MANUAL
                )
            ),
            resources = Resources(
                fonts = listOf(
                    FontResource(
                        id = "font-001",
                        name = "Georgia",
                        family = "Georgia",
                        path = "fonts/georgia.ttf",
                        format = "truetype",
                        weight = 400,
                        style = "normal"
                    )
                ),
                images = listOf(
                    ImageResource(
                        id = "img-001",
                        name = "Diagram 1",
                        path = "images/diagram1.png",
                        format = "png",
                        width = 1200,
                        height = 800,
                        alt = "System architecture diagram"
                    )
                )
            ),
            settings = DocumentSettings(
                defaultStylesheets = listOf("base"),
                theme = "light",
                fontSize = 16
            )
        )

        val jsonString = json.encodeToString(document)
        val deserialized = json.decodeFromString<Document>(jsonString)

        assertEquals(document.metadata.title, deserialized.metadata.title)
        assertEquals(document.spine, deserialized.spine)
        assertEquals(document.sections.size, deserialized.sections.size)
        assertEquals(document.stylesheets.size, deserialized.stylesheets.size)
        assertEquals(document.resources.fonts.size, deserialized.resources.fonts.size)
        assertEquals(document.resources.images.size, deserialized.resources.images.size)
        assertEquals(document.settings.defaultStylesheets, deserialized.settings.defaultStylesheets)
    }

    @Test
    fun `serialized document should include schema field with dollar sign`() {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test")
            )
        )

        val jsonString = json.encodeToString(document)

        assertTrue(jsonString.contains("\"\$schema\""))
    }

    @Test
    fun `stylesheet scope should serialize correctly`() {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test")
            ),
            stylesheets = listOf(
                Stylesheet(
                    id = "global",
                    name = "Global",
                    content = "",
                    scope = StylesheetScope.GLOBAL
                ),
                Stylesheet(
                    id = "manual",
                    name = "Manual",
                    content = "",
                    scope = StylesheetScope.MANUAL
                )
            )
        )

        val jsonString = json.encodeToString(document)

        assertTrue(jsonString.contains("\"scope\": \"GLOBAL\"") || jsonString.contains("\"scope\":\"GLOBAL\""))
        assertTrue(jsonString.contains("\"scope\": \"MANUAL\"") || jsonString.contains("\"scope\":\"MANUAL\""))
    }

    @Test
    fun `optional fields should not be serialized when null or default`() {
        val section = Section(
            id = "section-1",
            content = "Test"
        )

        val jsonString = json.encodeToString(section)
        val deserialized = json.decodeFromString<Section>(jsonString)

        assertEquals(section.id, deserialized.id)
        assertEquals(section.content, deserialized.content)
        assertEquals(null, deserialized.title)
        assertEquals(null, deserialized.order)
        assertEquals(emptyList(), deserialized.stylesheets)
    }

    @Test
    fun `section metadata with Instant should serialize correctly`() {
        val created = Instant.parse("2025-11-08T10:00:00Z")
        val section = Section(
            id = "section-1",
            content = "Test",
            metadata = SectionMetadata(
                wordCount = 100,
                created = created
            )
        )

        val jsonString = json.encodeToString(section)
        val deserialized = json.decodeFromString<Section>(jsonString)

        assertEquals(100, deserialized.metadata?.wordCount)
        assertEquals("2025-11-08T10:00:00Z", deserialized.metadata?.created.toString())
    }
}
