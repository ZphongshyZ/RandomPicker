package com.hntech.pickora.data.repository

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
class ListRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: ListRepository

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        File(context.filesDir, "saved_lists.json").delete()
        File(context.filesDir, "saved_lists.json.tmp").delete()
        repository = ListRepository(context)
    }

    @Test
    fun `initial lists are empty`() = runTest {
        val lists = repository.allLists.first()
        assertTrue(lists.isEmpty())
    }

    @Test
    fun `saveList adds new list`() = runTest {
        repository.saveList("My List", listOf("A", "B", "C"), "wheel")

        val lists = repository.allLists.first()
        assertEquals(1, lists.size)
        assertEquals("My List", lists[0].name)
        assertEquals(listOf("A", "B", "C"), lists[0].items)
        assertEquals("wheel", lists[0].preferredMode)
    }

    @Test
    fun `saveList returns valid id`() = runTest {
        val id = repository.saveList("Test", listOf("A"), "wheel")
        assertTrue(id > 0)
    }

    @Test
    fun `saveList with multiple lists preserves order`() = runTest {
        repository.saveList("First", listOf("1"), "wheel")
        Thread.sleep(10)
        repository.saveList("Second", listOf("2"), "name")

        val lists = repository.allLists.first()
        assertEquals(2, lists.size)
        assertEquals("Second", lists[0].name)
    }

    @Test
    fun `updateList modifies existing list`() = runTest {
        val id = repository.saveList("Original", listOf("A"), "wheel")

        repository.updateList(id, "Updated", listOf("A", "B"))

        val lists = repository.allLists.first()
        val updated = lists.find { it.id == id }
        assertNotNull(updated)
        assertEquals("Updated", updated!!.name)
        assertEquals(listOf("A", "B"), updated.items)
    }

    @Test
    fun `updateList with non-existent id does nothing`() = runTest {
        repository.saveList("Test", listOf("A"), "wheel")
        repository.updateList(999999L, "Ghost", listOf("X"))

        val lists = repository.allLists.first()
        assertEquals(1, lists.size)
        assertEquals("Test", lists[0].name)
    }

    @Test
    fun `deleteList removes list`() = runTest {
        val id = repository.saveList("ToDelete", listOf("A"), "wheel")
        repository.deleteList(id)

        val lists = repository.allLists.first()
        assertTrue(lists.isEmpty())
    }

    @Test
    fun `deleteList with non-existent id does nothing`() = runTest {
        repository.saveList("Keep", listOf("A"), "wheel")
        repository.deleteList(999999L)

        val lists = repository.allLists.first()
        assertEquals(1, lists.size)
    }

    @Test
    fun `getListById returns matching list`() = runTest {
        val id = repository.saveList("Find Me", listOf("A"), "wheel")
        val found = repository.getListById(id)

        assertNotNull(found)
        assertEquals("Find Me", found!!.name)
    }

    @Test
    fun `getListById returns null for non-existent id`() = runTest {
        assertNull(repository.getListById(999999L))
    }

    @Test
    fun `markUsed updates lastUsedAt`() = runTest {
        val id = repository.saveList("Test", listOf("A"), "wheel")
        assertEquals(0L, repository.getListById(id)!!.lastUsedAt)

        repository.markUsed(id)

        val updated = repository.getListById(id)
        assertTrue(updated!!.lastUsedAt > 0)
    }

    @Test
    fun `markUsed with mode updates preferredMode`() = runTest {
        val id = repository.saveList("Test", listOf("A"), "wheel")
        repository.markUsed(id, "race")

        val updated = repository.getListById(id)
        assertEquals("race", updated!!.preferredMode)
    }

    @Test
    fun `markUsed without mode keeps existing mode`() = runTest {
        val id = repository.saveList("Test", listOf("A"), "name")
        repository.markUsed(id)

        val updated = repository.getListById(id)
        assertEquals("name", updated!!.preferredMode)
    }

    @Test
    fun `lastUsedList returns most recently used`() = runTest {
        val id1 = repository.saveList("First", listOf("A"), "wheel")
        val id2 = repository.saveList("Second", listOf("B"), "name")

        repository.markUsed(id1)
        Thread.sleep(10)
        repository.markUsed(id2)

        val lastUsed = repository.lastUsedList.first()
        assertNotNull(lastUsed)
        assertEquals("Second", lastUsed!!.name)
    }

    @Test
    fun `lastUsedList returns null when no list has been used`() = runTest {
        repository.saveList("Unused", listOf("A"), "wheel")
        val lastUsed = repository.lastUsedList.first()
        assertNull(lastUsed)
    }

    @Test
    fun `markLastSavedAsUsed marks matching list`() = runTest {
        repository.saveList("Match", listOf("X", "Y"), "wheel")
        repository.markLastSavedAsUsed(listOf("X", "Y"), "race")

        val lists = repository.allLists.first()
        val matched = lists.find { it.name == "Match" }
        assertTrue(matched!!.lastUsedAt > 0)
        assertEquals("race", matched.preferredMode)
    }

    @Test
    fun `data persists across instances`() = runTest {
        repository.saveList("Persist", listOf("A", "B"), "wheel")

        val newRepo = ListRepository(context)
        val lists = newRepo.allLists.first()
        assertEquals(1, lists.size)
        assertEquals("Persist", lists[0].name)
    }

    @Test
    fun `SavedList has correct default values`() {
        val list = SavedList(name = "Test", items = listOf("A"))
        assertTrue(list.id > 0)
        assertEquals("wheel", list.preferredMode)
        assertTrue(list.createdAt > 0)
        assertTrue(list.updatedAt > 0)
        assertEquals(0L, list.lastUsedAt)
    }

    @Test
    fun `repository handles corrupted file gracefully`() = runTest {
        File(context.filesDir, "saved_lists.json").writeText("{corrupted json data!!")
        val repo = ListRepository(context)
        val lists = repo.allLists.first()
        assertTrue(lists.isEmpty())
    }
}
