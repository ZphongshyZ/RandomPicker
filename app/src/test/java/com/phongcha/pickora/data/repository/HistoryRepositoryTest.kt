package com.phongcha.pickora.data.repository

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30], manifest = Config.NONE)
class HistoryRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: HistoryRepository

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        File(context.filesDir, "history.json").delete()
        File(context.filesDir, "history.json.tmp").delete()
        repository = HistoryRepository(context)
    }

    @Test
    fun `initial history is empty`() = runTest {
        val history = repository.allHistory.first()
        assertTrue(history.isEmpty())
    }

    @Test
    fun `addEntry adds entry to history`() = runTest {
        repository.addEntry("wheel", "Pizza", listOf("Pizza", "Sushi", "Burger"))

        val history = repository.allHistory.first()
        assertEquals(1, history.size)
        assertEquals("wheel", history[0].pickerType)
        assertEquals("Pizza", history[0].result)
        assertEquals(listOf("Pizza", "Sushi", "Burger"), history[0].options)
    }

    @Test
    fun `addEntry prepends newest entry`() = runTest {
        repository.addEntry("wheel", "First", emptyList())
        repository.addEntry("wheel", "Second", emptyList())

        val history = repository.allHistory.first()
        assertEquals(2, history.size)
        assertEquals("Second", history[0].result)
        assertEquals("First", history[1].result)
    }

    @Test
    fun `addEntry caps at 200 entries`() = runTest {
        repeat(210) { i ->
            repository.addEntry("test", "Entry$i", emptyList())
        }

        val history = repository.allHistory.first()
        assertEquals(200, history.size)
        assertEquals("Entry209", history[0].result)
    }

    @Test
    fun `clearAll removes all entries`() = runTest {
        repository.addEntry("wheel", "Test", emptyList())
        repository.addEntry("number", "42", emptyList())

        repository.clearAll()

        val history = repository.allHistory.first()
        assertTrue(history.isEmpty())
    }

    @Test
    fun `deleteEntry removes specific entry`() = runTest {
        repository.addEntry("wheel", "A", emptyList())
        Thread.sleep(10)
        repository.addEntry("wheel", "B", emptyList())

        val history = repository.allHistory.first()
        val idToDelete = history[0].id

        repository.deleteEntry(idToDelete)

        val updated = repository.allHistory.first()
        assertEquals(1, updated.size)
        assertFalse(updated.any { it.id == idToDelete })
    }

    @Test
    fun `deleteEntry with non-existent id does nothing`() = runTest {
        repository.addEntry("wheel", "A", emptyList())
        repository.deleteEntry(999999L)

        val history = repository.allHistory.first()
        assertEquals(1, history.size)
    }

    @Test
    fun `getRecentHistory returns limited entries`() = runTest {
        repeat(10) { i ->
            repository.addEntry("test", "Entry$i", emptyList())
        }

        val recent = repository.getRecentHistory(3).first()
        assertEquals(3, recent.size)
    }

    @Test
    fun `getRecentHistory with limit larger than total returns all`() = runTest {
        repository.addEntry("test", "A", emptyList())
        repository.addEntry("test", "B", emptyList())

        val recent = repository.getRecentHistory(50).first()
        assertEquals(2, recent.size)
    }

    @Test
    fun `data persists across repository instances`() = runTest {
        repository.addEntry("wheel", "Persistent", listOf("A", "B"))

        val newRepo = HistoryRepository(context)
        val history = newRepo.allHistory.first()
        assertEquals(1, history.size)
        assertEquals("Persistent", history[0].result)
    }

    @Test
    fun `clearAll persists across instances`() = runTest {
        repository.addEntry("test", "Data", emptyList())
        repository.clearAll()

        val newRepo = HistoryRepository(context)
        val history = newRepo.allHistory.first()
        assertTrue(history.isEmpty())
    }

    @Test
    fun `HistoryEntry has correct default values`() {
        val entry = HistoryEntry(pickerType = "test", result = "result")
        assertTrue(entry.id > 0)
        assertEquals("test", entry.pickerType)
        assertEquals("result", entry.result)
        assertTrue(entry.options.isEmpty())
        assertTrue(entry.timestamp > 0)
    }

    @Test
    fun `repository handles corrupted file gracefully`() = runTest {
        File(context.filesDir, "history.json").writeText("not valid json {{{")
        val repo = HistoryRepository(context)
        val history = repo.allHistory.first()
        assertTrue(history.isEmpty()) // Should not crash, returns empty
    }

    @Test
    fun `repository handles empty file gracefully`() = runTest {
        File(context.filesDir, "history.json").writeText("")
        val repo = HistoryRepository(context)
        val history = repo.allHistory.first()
        assertTrue(history.isEmpty())
    }
}
