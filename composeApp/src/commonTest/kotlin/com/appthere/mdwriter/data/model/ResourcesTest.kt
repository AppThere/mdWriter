package com.appthere.mdwriter.data.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResourcesTest {

    @Test
    fun `empty resources should validate successfully`() {
        val resources = Resources()
        val result = resources.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `valid font resource should validate successfully`() {
        val font = FontResource(
            id = "font-001",
            name = "Georgia",
            family = "Georgia",
            path = "fonts/georgia.ttf",
            format = "truetype",
            weight = 400,
            style = "normal"
        )
        val result = font.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `font with invalid id should fail validation`() {
        val font = FontResource(
            id = "Font 001",
            name = "Georgia",
            family = "Georgia",
            path = "fonts/georgia.ttf",
            format = "truetype"
        )
        val result = font.validate()

        assertFalse(result.isValid())
    }

    @Test
    fun `font with blank name should fail validation`() {
        val font = FontResource(
            id = "font-001",
            name = "",
            family = "Georgia",
            path = "fonts/georgia.ttf",
            format = "truetype"
        )
        val result = font.validate()

        assertFalse(result.isValid())
    }

    @Test
    fun `font with invalid format should fail validation`() {
        val font = FontResource(
            id = "font-001",
            name = "Georgia",
            family = "Georgia",
            path = "fonts/georgia.ttf",
            format = "invalid"
        )
        val result = font.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Invalid font format") })
    }

    @Test
    fun `font with weight out of range should fail validation`() {
        val font = FontResource(
            id = "font-001",
            name = "Georgia",
            family = "Georgia",
            path = "fonts/georgia.ttf",
            format = "truetype",
            weight = 1000
        )
        val result = font.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Font weight must be between") })
    }

    @Test
    fun `font with invalid style should fail validation`() {
        val font = FontResource(
            id = "font-001",
            name = "Georgia",
            family = "Georgia",
            path = "fonts/georgia.ttf",
            format = "truetype",
            style = "bold"
        )
        val result = font.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Font style must be one of") })
    }

    @Test
    fun `valid image resource should validate successfully`() {
        val image = ImageResource(
            id = "img-001",
            name = "Diagram 1",
            path = "images/diagram1.png",
            format = "png",
            width = 1200,
            height = 800,
            alt = "System architecture diagram",
            caption = "Figure 1: System Architecture"
        )
        val result = image.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `image with invalid format should fail validation`() {
        val image = ImageResource(
            id = "img-001",
            name = "Image",
            path = "images/image.bmp",
            format = "bmp"
        )
        val result = image.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Invalid image format") })
    }

    @Test
    fun `image with negative width should fail validation`() {
        val image = ImageResource(
            id = "img-001",
            name = "Image",
            path = "images/image.png",
            format = "png",
            width = -100
        )
        val result = image.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Image width must be positive") })
    }

    @Test
    fun `image with negative height should fail validation`() {
        val image = ImageResource(
            id = "img-001",
            name = "Image",
            path = "images/image.png",
            format = "png",
            height = -100
        )
        val result = image.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Image height must be positive") })
    }

    @Test
    fun `valid attachment resource should validate successfully`() {
        val attachment = AttachmentResource(
            id = "attach-001",
            name = "dataset.csv",
            path = "attachments/dataset.csv",
            mimeType = "text/csv",
            size = 524288
        )
        val result = attachment.validate()

        assertTrue(result.isValid())
    }

    @Test
    fun `attachment with blank mime type should fail validation`() {
        val attachment = AttachmentResource(
            id = "attach-001",
            name = "file",
            path = "attachments/file",
            mimeType = ""
        )
        val result = attachment.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("MIME type is required") })
    }

    @Test
    fun `attachment with negative size should fail validation`() {
        val attachment = AttachmentResource(
            id = "attach-001",
            name = "file",
            path = "attachments/file",
            mimeType = "text/plain",
            size = -100
        )
        val result = attachment.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("size cannot be negative") })
    }

    @Test
    fun `resources with duplicate IDs should fail validation`() {
        val resources = Resources(
            fonts = listOf(
                FontResource(
                    id = "resource-001",
                    name = "Font",
                    family = "Font",
                    path = "font.ttf",
                    format = "truetype"
                )
            ),
            images = listOf(
                ImageResource(
                    id = "resource-001",
                    name = "Image",
                    path = "image.png",
                    format = "png"
                )
            )
        )
        val result = resources.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Duplicate resource IDs") })
    }

    @Test
    fun `resources with invalid font should fail validation`() {
        val resources = Resources(
            fonts = listOf(
                FontResource(
                    id = "font-001",
                    name = "",
                    family = "Font",
                    path = "font.ttf",
                    format = "truetype"
                )
            )
        )
        val result = resources.validate()

        assertFalse(result.isValid())
        assertTrue(result.getErrorsOrEmpty().any { it.contains("Font") })
    }
}
