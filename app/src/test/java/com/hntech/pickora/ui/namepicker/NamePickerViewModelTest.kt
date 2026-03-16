package com.hntech.pickora.ui.namepicker

import com.hntech.pickora.data.repository.HistoryRepository
import com.hntech.pickora.data.repository.ListRepository
import com.hntech.pickora.helper.FakeRandomEngine
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NamePickerViewModelTest {

    private lateinit var fakeEngine: FakeRandomEngine
    private lateinit var historyRepo: HistoryRepository
    private lateinit var listRepo: ListRepository
    private lateinit var viewModel: NamePickerViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeEngine = FakeRandomEngine()
        historyRepo = mockk(relaxed = true)
        listRepo = mockk(relaxed = true)
        coEvery { historyRepo.addEntry(any(), any(), any()) } returns Unit
        coEvery { listRepo.saveList(any(), any(), any()) } returns 1L
        coEvery { listRepo.markUsed(any(), any()) } returns Unit
        viewModel = NamePickerViewModel(fakeEngine, historyRepo, listRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `pickerType is name`() {
        assertEquals("name", viewModel.pickerType)
    }

    @Test
    fun `initial highlightedIndex is -1`() {
        assertEquals(-1, viewModel.highlightedIndex.value)
    }

    // --- addBatchNames ---

    @Test
    fun `addBatchNames splits by comma`() {
        viewModel.addBatchNames("Alice, Bob, Carol")
        assertEquals(3, viewModel.options.value.size)
        assertEquals("Alice", viewModel.options.value[0].label)
    }

    @Test
    fun `addBatchNames splits by newline`() {
        viewModel.addBatchNames("Alice\nBob\nCarol")
        assertEquals(3, viewModel.options.value.size)
    }

    @Test
    fun `addBatchNames splits by semicolon`() {
        viewModel.addBatchNames("Alice;Bob;Carol")
        assertEquals(3, viewModel.options.value.size)
    }

    @Test
    fun `addBatchNames ignores blank entries`() {
        viewModel.addBatchNames("Alice,,Bob,  ,Carol")
        assertEquals(3, viewModel.options.value.size)
    }

    @Test
    fun `addBatchNames appends to existing`() {
        viewModel.loadOptionsFromStrings(listOf("Existing"))
        viewModel.addBatchNames("New1, New2")
        assertEquals(3, viewModel.options.value.size)
    }

    @Test
    fun `addBatchNames with empty text does nothing`() {
        viewModel.addBatchNames("")
        assertTrue(viewModel.options.value.isEmpty())
    }

    // --- pickRandom ---

    @Test
    fun `pickRandom with less than 2 options does nothing`() {
        viewModel.loadOptionsFromStrings(listOf("Only"))
        viewModel.pickRandom()
        assertNull(viewModel.result.value)
    }

    @Test
    fun `pickRandom selects a winner`() = runTest(testDispatcher) {
        viewModel.loadOptionsFromStrings(listOf("A", "B", "C"))
        fakeEngine.enqueueSelectIndex(1) // winner = B
        repeat(20) { fakeEngine.enqueueNumber(0) } // animation highlights

        viewModel.pickRandom()
        advanceUntilIdle()

        assertNotNull(viewModel.result.value)
        assertEquals("B", viewModel.result.value?.label)
    }

    @Test
    fun `pickRandom sets highlightedIndex to winner index`() = runTest(testDispatcher) {
        viewModel.loadOptionsFromStrings(listOf("A", "B", "C"))
        fakeEngine.enqueueSelectIndex(2) // winner = C (index 2)
        repeat(20) { fakeEngine.enqueueNumber(0) }

        viewModel.pickRandom()
        advanceUntilIdle()

        assertEquals(2, viewModel.highlightedIndex.value)
    }

    @Test
    fun `pickRandom shows confetti`() = runTest(testDispatcher) {
        viewModel.loadOptionsFromStrings(listOf("A", "B"))
        fakeEngine.enqueueSelectIndex(0)
        repeat(20) { fakeEngine.enqueueNumber(0) }

        viewModel.pickRandom()
        advanceUntilIdle()

        assertTrue(viewModel.showConfetti.value)
    }

    @Test
    fun `pickRandom does not run when already animating`() = runTest(testDispatcher) {
        viewModel.loadOptionsFromStrings(listOf("A", "B", "C"))
        fakeEngine.enqueueSelectIndex(0)
        repeat(20) { fakeEngine.enqueueNumber(0) }
        viewModel.pickRandom()

        assertTrue(viewModel.isAnimating.value)

        // Second call should be blocked
        fakeEngine.enqueueSelectIndex(2)
        repeat(20) { fakeEngine.enqueueNumber(0) }
        viewModel.pickRandom()

        advanceUntilIdle()
        assertEquals("A", viewModel.result.value?.label) // first winner
    }

    // --- removeWinner ---

    @Test
    fun `removeWinner removes winner from options`() = runTest(testDispatcher) {
        viewModel.loadOptionsFromStrings(listOf("A", "B", "C"))
        fakeEngine.enqueueSelectIndex(1) // B
        repeat(20) { fakeEngine.enqueueNumber(0) }
        viewModel.pickRandom()
        advanceUntilIdle()

        viewModel.removeWinner()

        assertEquals(2, viewModel.options.value.size)
        assertFalse(viewModel.options.value.any { it.label == "B" })
        assertNull(viewModel.result.value)
    }

    @Test
    fun `removeWinner does nothing without result`() {
        viewModel.loadOptionsFromStrings(listOf("A", "B"))
        viewModel.removeWinner()
        assertEquals(2, viewModel.options.value.size)
    }

    // --- pickAgain ---

    @Test
    fun `pickAgain dismisses confetti and picks again`() = runTest(testDispatcher) {
        viewModel.loadOptionsFromStrings(listOf("A", "B", "C"))
        fakeEngine.enqueueSelectIndex(0)
        repeat(20) { fakeEngine.enqueueNumber(0) }
        viewModel.pickRandom()
        advanceUntilIdle()

        fakeEngine.enqueueSelectIndex(1)
        repeat(20) { fakeEngine.enqueueNumber(0) }
        viewModel.pickAgain()
        advanceUntilIdle()

        assertNotNull(viewModel.result.value)
    }

    // --- saveCurrentList ---

    @Test
    fun `saveCurrentList saves to list repository`() = runTest(testDispatcher) {
        viewModel.loadOptionsFromStrings(listOf("A", "B", "C"))
        viewModel.saveCurrentList("Test List")
        advanceUntilIdle()

        coVerify { listRepo.saveList("Test List", listOf("A", "B", "C"), "name") }
    }

    @Test
    fun `saveCurrentList with empty options does nothing`() = runTest(testDispatcher) {
        viewModel.saveCurrentList("Empty")
        advanceUntilIdle()
        coVerify(exactly = 0) { listRepo.saveList(any(), any(), any()) }
    }
}
