package com.appthere.mdwriter.data

import com.appthere.mdwriter.data.model.*
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DocumentTest {
    @Test
    fun `getSectionsInOrder returns sections in spine order`() {
        val now = Clock.System.now()
        val section1 = Section(id = "sec-1", content = "First", order = 0)
        val section2 = Section(id = "sec-2", content = "Second", order = 1)
        val section3 = Section(id = "sec-3", content = "Third", order = 2)

        val document = Document(
            metadata = Metadata(
                title = "Test",
                author = "Author",
                created = now,
                modified = now
            ),
            spine = listOf("sec-2", "sec-1", "sec-3"),
            sections = mapOf(
                "sec-1" to section1,
                "sec-2" to section2,
                "sec-3" to section3
            )
        )

        val sectionsInOrder = document.getSectionsInOrder()

        assertEquals(3, sectionsInOrder.size)
        assertEquals("sec-2", sectionsInOrder[0].id)
        assertEquals("sec-1", sectionsInOrder[1].id)
        assertEquals("sec-3", sectionsInOrder[2].id)
    }

    @Test
    fun `getActiveStylesheetsForSection returns global and section stylesheets`() {
        val now = Clock.System.now()
        val globalStyle = Stylesheet(id = "global-1", name = "Global", scope = StylesheetScope.GLOBAL)
        val manualStyle1 = Stylesheet(id = "manual-1", name = "Manual 1", scope = StylesheetScope.MANUAL)
        val manualStyle2 = Stylesheet(id = "manual-2", name = "Manual 2", scope = StylesheetScope.MANUAL)

        val section = Section(
            id = "sec-1",
            content = "Content",
            stylesheets = listOf("manual-1")
        )

        val document = Document(
            metadata = Metadata(
                title = "Test",
                author = "Author",
                created = now,
                modified = now
            ),
            spine = listOf("sec-1"),
            sections = mapOf("sec-1" to section),
            stylesheets = listOf(globalStyle, manualStyle1, manualStyle2),
            settings = DocumentSettings(defaultStylesheets = listOf("global-1"))
        )

        val activeStyles = document.getActiveStylesheetsForSection("sec-1")

        assertEquals(2, activeStyles.size)
        assertEquals("global-1", activeStyles[0].id)
        assertEquals("manual-1", activeStyles[1].id)
    }

    @Test
    fun `getActiveStylesheetsForSection avoids duplicates`() {
        val now = Clock.System.now()
        val style = Stylesheet(id = "style-1", name = "Style", scope = StylesheetScope.MANUAL)

        val section = Section(
            id = "sec-1",
            content = "Content",
            stylesheets = listOf("style-1")
        )

        val document = Document(
            metadata = Metadata(
                title = "Test",
                author = "Author",
                created = now,
                modified = now
            ),
            spine = listOf("sec-1"),
            sections = mapOf("sec-1" to section),
            stylesheets = listOf(style),
            settings = DocumentSettings(defaultStylesheets = listOf("style-1"))
        )

        val activeStyles = document.getActiveStylesheetsForSection("sec-1")

        assertEquals(1, activeStyles.size) // No duplicates
        assertEquals("style-1", activeStyles[0].id)
    }

    @Test
    fun `getStylesheet returns correct stylesheet`() {
        val now = Clock.System.now()
        val style1 = Stylesheet(id = "style-1", name = "Style 1")
        val style2 = Stylesheet(id = "style-2", name = "Style 2")

        val document = Document(
            metadata = Metadata(
                title = "Test",
                author = "Author",
                created = now,
                modified = now
            ),
            stylesheets = listOf(style1, style2)
        )

        assertNotNull(document.getStylesheet("style-1"))
        assertEquals("Style 1", document.getStylesheet("style-1")?.name)
        assertNull(document.getStylesheet("non-existent"))
    }

    @Test
    fun `getSection returns correct section`() {
        val now = Clock.System.now()
        val section1 = Section(id = "sec-1", content = "Content 1")
        val section2 = Section(id = "sec-2", content = "Content 2")

        val document = Document(
            metadata = Metadata(
                title = "Test",
                author = "Author",
                created = now,
                modified = now
            ),
            sections = mapOf(
                "sec-1" to section1,
                "sec-2" to section2
            )
        )

        assertNotNull(document.getSection("sec-1"))
        assertEquals("Content 1", document.getSection("sec-1")?.content)
        assertNull(document.getSection("non-existent"))
    }
}
