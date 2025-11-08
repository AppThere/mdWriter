package com.appthere.mdwriter.data

import com.appthere.mdwriter.data.model.*
import com.appthere.mdwriter.data.repository.InMemoryDocumentRepository
import com.appthere.mdwriter.domain.model.Result
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.*

class InMemoryDocumentRepositoryTest {
    private lateinit var repository: InMemoryDocumentRepository

    @BeforeTest
    fun setup() {
        repository = InMemoryDocumentRepository()
    }

    @Test
    fun `createDocument returns new document`() = runTest {
        val result = repository.createDocument()

        assertTrue(result.isSuccess)
        val document = result.getOrNull()
        assertNotNull(document)
        assertEquals("Untitled Document", document.metadata.title)
        assertTrue(document.spine.isNotEmpty())
    }

    @Test
    fun `saveDocument stores document successfully`() = runTest {
        val now = Clock.System.now()
        val document = Document(
            metadata = Metadata(
                title = "Test",
                author = "Author",
                created = now,
                modified = now
            ),
            spine = listOf("sec-1"),
            sections = mapOf(
                "sec-1" to Section(id = "sec-1", content = "Content")
            )
        )

        val saveResult = repository.saveDocument("test.json", document)

        assertTrue(saveResult.isSuccess)
    }

    @Test
    fun `loadDocument retrieves saved document`() = runTest {
        val now = Clock.System.now()
        val document = Document(
            metadata = Metadata(
                title = "Test Doc",
                author = "Test Author",
                created = now,
                modified = now
            ),
            spine = listOf("sec-1"),
            sections = mapOf(
                "sec-1" to Section(id = "sec-1", content = "Test content")
            )
        )

        repository.saveDocument("test.json", document)
        val loadResult = repository.loadDocument("test.json")

        assertTrue(loadResult.isSuccess)
        val loadedDoc = loadResult.getOrNull()
        assertNotNull(loadedDoc)
        assertEquals("Test Doc", loadedDoc.metadata.title)
        assertEquals("Test content", loadedDoc.sections["sec-1"]?.content)
    }

    @Test
    fun `loadDocument returns error for non-existent document`() = runTest {
        val result = repository.loadDocument("non-existent.json")

        assertTrue(result.isError)
    }

    @Test
    fun `deleteDocument removes document`() = runTest {
        val now = Clock.System.now()
        val document = Document(
            metadata = Metadata(
                title = "Test",
                author = "Author",
                created = now,
                modified = now
            )
        )

        repository.saveDocument("test.json", document)
        val deleteResult = repository.deleteDocument("test.json")

        assertTrue(deleteResult.isSuccess)

        val loadResult = repository.loadDocument("test.json")
        assertTrue(loadResult.isError)
    }

    @Test
    fun `deleteDocument returns error for non-existent document`() = runTest {
        val result = repository.deleteDocument("non-existent.json")

        assertTrue(result.isError)
    }

    @Test
    fun `listDocuments returns all document paths`() = runTest {
        val now = Clock.System.now()
        val doc1 = Document(
            metadata = Metadata(
                title = "Doc 1",
                author = "Author",
                created = now,
                modified = now
            )
        )
        val doc2 = Document(
            metadata = Metadata(
                title = "Doc 2",
                author = "Author",
                created = now,
                modified = now
            )
        )

        repository.saveDocument("doc1.json", doc1)
        repository.saveDocument("doc2.json", doc2)

        val result = repository.listDocuments()

        assertTrue(result.isSuccess)
        val paths = result.getOrNull()
        assertNotNull(paths)
        assertEquals(2, paths.size)
        assertTrue(paths.contains("doc1.json"))
        assertTrue(paths.contains("doc2.json"))
    }

    @Test
    fun `saveDocument updates existing document`() = runTest {
        val now = Clock.System.now()
        val doc1 = Document(
            metadata = Metadata(
                title = "Original",
                author = "Author",
                created = now,
                modified = now
            )
        )
        val doc2 = Document(
            metadata = Metadata(
                title = "Updated",
                author = "Author",
                created = now,
                modified = now
            )
        )

        repository.saveDocument("test.json", doc1)
        repository.saveDocument("test.json", doc2)

        val result = repository.loadDocument("test.json")
        assertTrue(result.isSuccess)
        assertEquals("Updated", result.getOrNull()?.metadata?.title)
    }
}
