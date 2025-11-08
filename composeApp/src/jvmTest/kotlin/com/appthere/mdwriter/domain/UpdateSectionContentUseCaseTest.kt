package com.appthere.mdwriter.domain

import com.appthere.mdwriter.data.model.*
import com.appthere.mdwriter.domain.usecase.UpdateSectionContentUseCase
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UpdateSectionContentUseCaseTest {
    private val useCase = UpdateSectionContentUseCase()

    @Test
    fun `update section content successfully`() {
        val now = Clock.System.now()
        val section1 = Section(id = "sec-1", content = "Original content", order = 0)
        val section2 = Section(id = "sec-2", content = "Other section", order = 1)

        val document = Document(
            metadata = Metadata(
                title = "Test Doc",
                author = "Test Author",
                created = now,
                modified = now
            ),
            spine = listOf("sec-1", "sec-2"),
            sections = mapOf(
                "sec-1" to section1,
                "sec-2" to section2
            )
        )

        val result = useCase(document, "sec-1", "Updated content")

        assertNotNull(result.sections["sec-1"])
        assertEquals("Updated content", result.sections["sec-1"]?.content)
        assertEquals("Other section", result.sections["sec-2"]?.content) // Unchanged
    }

    @Test
    fun `update non-existent section returns unchanged document`() {
        val now = Clock.System.now()
        val section1 = Section(id = "sec-1", content = "Content", order = 0)

        val document = Document(
            metadata = Metadata(
                title = "Test Doc",
                author = "Test Author",
                created = now,
                modified = now
            ),
            spine = listOf("sec-1"),
            sections = mapOf("sec-1" to section1)
        )

        val result = useCase(document, "non-existent", "New content")

        assertEquals(document, result) // Document unchanged
    }

    @Test
    fun `update preserves other section properties`() {
        val now = Clock.System.now()
        val section = Section(
            id = "sec-1",
            content = "Original",
            order = 5,
            stylesheets = listOf("style-1", "style-2")
        )

        val document = Document(
            metadata = Metadata(
                title = "Test",
                author = "Author",
                created = now,
                modified = now
            ),
            spine = listOf("sec-1"),
            sections = mapOf("sec-1" to section)
        )

        val result = useCase(document, "sec-1", "Updated")

        val updatedSection = result.sections["sec-1"]
        assertNotNull(updatedSection)
        assertEquals("Updated", updatedSection.content)
        assertEquals(5, updatedSection.order)
        assertEquals(listOf("style-1", "style-2"), updatedSection.stylesheets)
    }
}
