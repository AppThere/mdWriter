package com.appthere.mdwriter.util

import kotlin.test.*

class FrontmatterParserTest {

    private lateinit var parser: FrontmatterParser

    @BeforeTest
    fun setup() {
        parser = FrontmatterParser()
    }

    @Test
    fun `parse should handle simple key-value pairs`() {
        val yaml = """
            title: Test Document
            author: John Doe
            date: 2025-11-08
        """.trimIndent()

        val result = parser.parse(yaml)

        assertEquals("Test Document", result["title"])
        assertEquals("John Doe", result["author"])
        assertEquals("2025-11-08", result["date"])
    }

    @Test
    fun `parse should handle boolean values`() {
        val yaml = """
            draft: true
            published: false
        """.trimIndent()

        val result = parser.parse(yaml)

        assertEquals(true, result["draft"])
        assertEquals(false, result["published"])
    }

    @Test
    fun `parse should handle numeric values`() {
        val yaml = """
            count: 42
            rating: 4.5
        """.trimIndent()

        val result = parser.parse(yaml)

        assertEquals(42, result["count"])
        assertEquals(4.5, result["rating"])
    }

    @Test
    fun `parse should handle lists`() {
        val yaml = """
            tags:
              - kotlin
              - markdown
              - multiplatform
        """.trimIndent()

        val result = parser.parse(yaml)

        val tags = result["tags"] as? List<*>
        assertNotNull(tags)
        assertEquals(3, tags.size)
        assertTrue(tags.contains("kotlin"))
        assertTrue(tags.contains("markdown"))
        assertTrue(tags.contains("multiplatform"))
    }

    @Test
    fun `parse should handle nested objects`() {
        val yaml = """
            author:
              name: John Doe
              email: john@example.com
        """.trimIndent()

        val result = parser.parse(yaml)

        val author = result["author"] as? Map<*, *>
        assertNotNull(author)
        assertEquals("John Doe", author["name"])
        assertEquals("john@example.com", author["email"])
    }

    @Test
    fun `parse should return empty map for empty string`() {
        val result = parser.parse("")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `parse should return empty map for blank string`() {
        val result = parser.parse("   \n  \n  ")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `parse should handle invalid YAML gracefully`() {
        val invalidYaml = """
            title: Test
            invalid: [unclosed
            another: value
        """.trimIndent()

        val result = parser.parse(invalidYaml)

        // Should return empty map on parse error
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseWithResult should return success for valid YAML`() {
        val yaml = "title: Test"

        val result = parser.parseWithResult(yaml)

        assertTrue(result.isSuccess)
        assertEquals("Test", result.getOrNull()?.get("title"))
    }

    @Test
    fun `parseWithResult should return failure for invalid YAML`() {
        val invalidYaml = "invalid: [unclosed"

        val result = parser.parseWithResult(invalidYaml)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is YamlParseException)
    }

    @Test
    fun `extractFrontmatter should extract frontmatter from markdown`() {
        val markdown = """
            ---
            title: Test Document
            author: John Doe
            ---

            # Main Content

            This is the content.
        """.trimIndent()

        val (frontmatter, content) = parser.extractFrontmatter(markdown)

        assertEquals("Test Document", frontmatter["title"])
        assertEquals("John Doe", frontmatter["author"])
        assertTrue(content.startsWith("# Main Content"))
        assertFalse(content.contains("---"))
    }

    @Test
    fun `extractFrontmatter should handle markdown without frontmatter`() {
        val markdown = """
            # Main Content

            This is content without frontmatter.
        """.trimIndent()

        val (frontmatter, content) = parser.extractFrontmatter(markdown)

        assertTrue(frontmatter.isEmpty())
        assertEquals(markdown, content)
    }

    @Test
    fun `extractFrontmatter should handle incomplete frontmatter`() {
        val markdown = """
            ---
            title: Test
            # Missing closing delimiter

            Content here
        """.trimIndent()

        val (frontmatter, content) = parser.extractFrontmatter(markdown)

        assertTrue(frontmatter.isEmpty())
        assertEquals(markdown, content)
    }

    @Test
    fun `extractFrontmatter should handle empty frontmatter`() {
        val markdown = """
            ---
            ---

            # Content
        """.trimIndent()

        val (frontmatter, content) = parser.extractFrontmatter(markdown)

        assertTrue(frontmatter.isEmpty())
        assertTrue(content.startsWith("# Content"))
    }

    @Test
    fun `extractFrontmatter should handle frontmatter with complex values`() {
        val markdown = """
            ---
            title: My Document
            tags:
              - tag1
              - tag2
            metadata:
              author: John
              date: 2025-11-08
            draft: false
            ---

            Content starts here.
        """.trimIndent()

        val (frontmatter, content) = parser.extractFrontmatter(markdown)

        assertEquals("My Document", frontmatter["title"])
        assertEquals(false, frontmatter["draft"])

        val tags = frontmatter["tags"] as? List<*>
        assertNotNull(tags)
        assertEquals(2, tags.size)

        val metadata = frontmatter["metadata"] as? Map<*, *>
        assertNotNull(metadata)
        assertEquals("John", metadata["author"])
    }

    @Test
    fun `parse should handle multiline strings`() {
        val yaml = """
            description: |
              This is a multiline
              description that spans
              multiple lines.
        """.trimIndent()

        val result = parser.parse(yaml)

        val description = result["description"] as? String
        assertNotNull(description)
        assertTrue(description.contains("multiline"))
    }
}
