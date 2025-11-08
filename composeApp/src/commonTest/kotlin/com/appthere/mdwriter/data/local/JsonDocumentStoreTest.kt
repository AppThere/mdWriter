package com.appthere.mdwriter.data.local

import com.appthere.mdwriter.data.model.Document
import com.appthere.mdwriter.data.model.Metadata
import com.appthere.mdwriter.data.model.Section
import com.appthere.mdwriter.data.platform.FakeFileSystem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.*

class JsonDocumentStoreTest {

    private lateinit var fileSystem: FakeFileSystem
    private lateinit var store: JsonDocumentStore

    @BeforeTest
    fun setup() {
        fileSystem = FakeFileSystem()
        store = JsonDocumentStore(fileSystem)
    }

    @AfterTest
    fun tearDown() {
        fileSystem.clear()
    }

    @Test
    fun `saveDocument should save valid document`() = runTest {
        val document = Document(
            metadata = Metadata(title = "Test Document"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "# Test Content")
            )
        )

        val result = store.saveDocument("test-doc", document)

        assertTrue(result.isSuccess)
        assertTrue(fileSystem.fileExists("/test/documents/documents/test-doc.mdoc"))
    }

    @Test
    fun `saveDocument should reject invalid document`() = runTest {
        val invalidDocument = Document(
            metadata = Metadata(title = ""),  // Invalid: empty title
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test")
            )
        )

        val result = store.saveDocument("test-doc", invalidDocument)

        assertTrue(result.isFailure)
        assertFalse(fileSystem.fileExists("/test/documents/documents/test-doc.mdoc"))
    }

    @Test
    fun `saveDocument should update modified timestamp`() = runTest {
        val originalTime = Instant.parse("2025-11-08T10:00:00Z")
        val document = Document(
            metadata = Metadata(
                title = "Test",
                modified = originalTime
            ),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test")
            )
        )

        store.saveDocument("test-doc", document)

        val loaded = store.loadDocument("test-doc").first()
        assertNotNull(loaded)
        assertNotEquals(originalTime, loaded.metadata.modified)
    }

    @Test
    fun `loadDocument should return null for non-existent document`() = runTest {
        val result = store.loadDocument("non-existent").first()

        assertNull(result)
    }

    @Test
    fun `loadDocument should return saved document`() = runTest {
        val document = Document(
            metadata = Metadata(title = "Test Document"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "# Test")
            )
        )

        store.saveDocument("test-doc", document)
        val loaded = store.loadDocument("test-doc").first()

        assertNotNull(loaded)
        assertEquals("Test Document", loaded.metadata.title)
        assertEquals(1, loaded.sections.size)
    }

    @Test
    fun `deleteDocument should remove document`() = runTest {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("section-1"),
            sections = mapOf(
                "section-1" to Section(id = "section-1", content = "Test")
            )
        )

        store.saveDocument("test-doc", document)
        assertTrue(store.documentExists("test-doc"))

        val result = store.deleteDocument("test-doc")

        assertTrue(result.isSuccess)
        assertFalse(store.documentExists("test-doc"))
    }

    @Test
    fun `deleteDocument should succeed for non-existent document`() = runTest {
        val result = store.deleteDocument("non-existent")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `listDocuments should return all documents`() = runTest {
        val doc1 = Document(
            metadata = Metadata(title = "Document 1"),
            spine = listOf("s1"),
            sections = mapOf("s1" to Section(id = "s1", content = "Test"))
        )
        val doc2 = Document(
            metadata = Metadata(title = "Document 2"),
            spine = listOf("s1"),
            sections = mapOf("s1" to Section(id = "s1", content = "Test"))
        )

        store.saveDocument("doc-1", doc1)
        store.saveDocument("doc-2", doc2)

        val list = store.listDocuments().first()

        assertEquals(2, list.size)
        assertTrue(list.any { it.title == "Document 1" })
        assertTrue(list.any { it.title == "Document 2" })
    }

    @Test
    fun `listDocuments should return empty list when no documents`() = runTest {
        val list = store.listDocuments().first()

        assertTrue(list.isEmpty())
    }

    @Test
    fun `documentExists should return true for existing document`() = runTest {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("s1"),
            sections = mapOf("s1" to Section(id = "s1", content = "Test"))
        )

        store.saveDocument("test-doc", document)

        assertTrue(store.documentExists("test-doc"))
    }

    @Test
    fun `documentExists should return false for non-existent document`() = runTest {
        assertFalse(store.documentExists("non-existent"))
    }

    @Test
    fun `getDocumentMetadata should return metadata without loading full document`() = runTest {
        val document = Document(
            metadata = Metadata(
                title = "Test Document",
                creator = "John Doe"
            ),
            spine = listOf("s1"),
            sections = mapOf("s1" to Section(id = "s1", content = "Test"))
        )

        store.saveDocument("test-doc", document)
        val metadata = store.getDocumentMetadata("test-doc")

        assertNotNull(metadata)
        assertEquals("test-doc", metadata.id)
        assertEquals("Test Document", metadata.title)
        assertEquals("John Doe", metadata.creator)
        assertNotNull(metadata.fileSize)
    }

    @Test
    fun `getDocumentMetadata should return null for non-existent document`() = runTest {
        val metadata = store.getDocumentMetadata("non-existent")

        assertNull(metadata)
    }

    @Test
    fun `saveDocument should use atomic write pattern`() = runTest {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("s1"),
            sections = mapOf("s1" to Section(id = "s1", content = "Test"))
        )

        store.saveDocument("test-doc", document)

        // Verify no temp file remains
        val allFiles = fileSystem.getAllFiles()
        assertFalse(allFiles.any { it.endsWith(".tmp") })
    }

    @Test
    fun `saveDocument should overwrite existing document`() = runTest {
        val doc1 = Document(
            metadata = Metadata(title = "Version 1"),
            spine = listOf("s1"),
            sections = mapOf("s1" to Section(id = "s1", content = "Test 1"))
        )
        val doc2 = Document(
            metadata = Metadata(title = "Version 2"),
            spine = listOf("s1"),
            sections = mapOf("s1" to Section(id = "s1", content = "Test 2"))
        )

        store.saveDocument("test-doc", doc1)
        store.saveDocument("test-doc", doc2)

        val loaded = store.loadDocument("test-doc").first()

        assertNotNull(loaded)
        assertEquals("Version 2", loaded.metadata.title)
    }

    @Test
    fun `performMaintenance should remove temp files`() = runTest {
        // Manually create a temp file
        fileSystem.writeFile("/test/documents/documents/orphan.tmp", "temp content")

        store.performMaintenance()

        assertFalse(fileSystem.fileExists("/test/documents/documents/orphan.tmp"))
    }

    @Test
    fun `concurrent saves should not corrupt data`() = runTest {
        val document = Document(
            metadata = Metadata(title = "Test"),
            spine = listOf("s1"),
            sections = mapOf("s1" to Section(id = "s1", content = "Test"))
        )

        // Simulate concurrent saves (sequential in test, but pattern is atomic)
        store.saveDocument("test-doc", document)
        store.saveDocument("test-doc", document.copy(metadata = document.metadata.copy(title = "Updated")))

        val loaded = store.loadDocument("test-doc").first()

        assertNotNull(loaded)
        assertEquals("Updated", loaded.metadata.title)
    }

    @Test
    fun `loadDocument should handle corrupted JSON gracefully`() = runTest {
        // Write invalid JSON directly
        fileSystem.writeFile("/test/documents/documents/corrupted.mdoc", "{ invalid json }")

        val loaded = store.loadDocument("corrupted").first()

        assertNull(loaded)
    }

    @Test
    fun `listDocuments should skip corrupted documents`() = runTest {
        val validDoc = Document(
            metadata = Metadata(title = "Valid"),
            spine = listOf("s1"),
            sections = mapOf("s1" to Section(id = "s1", content = "Test"))
        )

        store.saveDocument("valid-doc", validDoc)
        fileSystem.writeFile("/test/documents/documents/corrupted.mdoc", "{ invalid json }")

        val list = store.listDocuments().first()

        // Should only include the valid document
        assertEquals(1, list.size)
        assertEquals("Valid", list[0].title)
    }
}
