package com.appthere.mdwriter.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StylesheetTest {

    @Test
    fun `valid stylesheet should validate successfully`() {
        val stylesheet = Stylesheet(
            id = "main-style",
            name = "Main Stylesheet",
            content = "body { font-family: serif; }"
        )
        val result = stylesheet.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `stylesheet with blank id should fail validation`() {
        val stylesheet = Stylesheet(
            id = "",
            name = "Test",
            content = "body {}"
        )
        val result = stylesheet.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Stylesheet ID is required") })
    }

    @Test
    fun `stylesheet with invalid id format should fail validation`() {
        val stylesheet = Stylesheet(
            id = "Main Style",
            name = "Main Stylesheet",
            content = "body {}"
        )
        val result = stylesheet.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Invalid stylesheet ID format") })
    }

    @Test
    fun `stylesheet with uppercase in id should fail validation`() {
        val stylesheet = Stylesheet(
            id = "MainStyle",
            name = "Main Stylesheet",
            content = "body {}"
        )
        val result = stylesheet.validate()

        assertFalse(result.isValid())
    }

    @Test
    fun `stylesheet with blank name should fail validation`() {
        val stylesheet = Stylesheet(
            id = "main-style",
            name = "",
            content = "body {}"
        )
        val result = stylesheet.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Stylesheet name is required") })
    }

    @Test
    fun `stylesheet with empty content should validate successfully`() {
        val stylesheet = Stylesheet(
            id = "empty-style",
            name = "Empty Stylesheet",
            content = ""
        )
        val result = stylesheet.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `stylesheet with global scope should validate successfully`() {
        val stylesheet = Stylesheet(
            id = "global-style",
            name = "Global Stylesheet",
            content = "body { margin: 0; }",
            scope = StylesheetScope.GLOBAL
        )
        val result = stylesheet.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `stylesheet with manual scope should validate successfully`() {
        val stylesheet = Stylesheet(
            id = "manual-style",
            name = "Manual Stylesheet",
            content = ".class { color: red; }",
            scope = StylesheetScope.MANUAL
        )
        val result = stylesheet.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `stylesheet with custom priority should validate successfully`() {
        val stylesheet = Stylesheet(
            id = "priority-style",
            name = "Priority Stylesheet",
            content = "body {}",
            priority = 100
        )
        val result = stylesheet.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `stylesheet with disabled state should validate successfully`() {
        val stylesheet = Stylesheet(
            id = "disabled-style",
            name = "Disabled Stylesheet",
            content = "body {}",
            enabled = false
        )
        val result = stylesheet.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `StylesheetScope fromString should parse global correctly`() {
        assertEquals(StylesheetScope.GLOBAL, StylesheetScope.fromString("global"))
        assertEquals(StylesheetScope.GLOBAL, StylesheetScope.fromString("GLOBAL"))
        assertEquals(StylesheetScope.GLOBAL, StylesheetScope.fromString("Global"))
    }

    @Test
    fun `StylesheetScope fromString should parse manual correctly`() {
        assertEquals(StylesheetScope.MANUAL, StylesheetScope.fromString("manual"))
        assertEquals(StylesheetScope.MANUAL, StylesheetScope.fromString("MANUAL"))
        assertEquals(StylesheetScope.MANUAL, StylesheetScope.fromString("Manual"))
    }

    @Test
    fun `StylesheetScope fromString should default to manual for unknown values`() {
        assertEquals(StylesheetScope.MANUAL, StylesheetScope.fromString("unknown"))
        assertEquals(StylesheetScope.MANUAL, StylesheetScope.fromString(""))
        assertEquals(StylesheetScope.MANUAL, StylesheetScope.fromString("auto"))
    }
}
