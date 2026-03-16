package com.hntech.pickora.ui.number

import com.hntech.pickora.data.repository.HistoryRepository
import com.hntech.pickora.helper.FakeRandomEngine
import io.mockk.coEvery
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
class NumberViewModelTest {

    private lateinit var fakeEngine: FakeRandomEngine
    private lateinit var historyRepo: HistoryRepository
    private lateinit var viewModel: NumberViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeEngine = FakeRandomEngine()
        historyRepo = mockk(relaxed = true)
        coEvery { historyRepo.addEntry(any(), any(), any()) } returns Unit
        viewModel = NumberViewModel(fakeEngine, historyRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `pickerType is number`() {
        assertEquals("number", viewModel.pickerType)
    }

    @Test
    fun `initial min is 1 and max is 100`() {
        assertEquals(1, viewModel.minValue.value)
        assertEquals(100, viewModel.maxValue.value)
    }

    @Test
    fun `initial displayNumber is null`() {
        assertNull(viewModel.displayNumber.value)
    }

    @Test
    fun `setRange updates min and max values`() {
        viewModel.setRange(10, 50)
        assertEquals(10, viewModel.minValue.value)
        assertEquals(50, viewModel.maxValue.value)
    }

    @Test
    fun `generate sets displayNumber to final result`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(42) // final number
        repeat(15) { fakeEngine.enqueueNumber(5) } // animation numbers

        viewModel.generate()
        advanceUntilIdle()

        assertEquals(42, viewModel.displayNumber.value)
        assertFalse(viewModel.isAnimating.value)
    }

    @Test
    fun `generate sets result and shows confetti`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(7)
        repeat(15) { fakeEngine.enqueueNumber(3) }

        viewModel.generate()
        advanceUntilIdle()

        assertNotNull(viewModel.result.value)
        assertEquals("7", viewModel.result.value?.label)
        assertTrue(viewModel.showConfetti.value)
    }

    @Test
    fun `generate does nothing when min greater than max`() {
        viewModel.setRange(100, 1)
        viewModel.generate()
        assertNull(viewModel.displayNumber.value)
    }

    @Test
    fun `generate does nothing when already animating`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(10)
        repeat(15) { fakeEngine.enqueueNumber(5) }
        viewModel.generate()

        // Before advancing, isAnimating is true
        assertTrue(viewModel.isAnimating.value)

        // Second generate should be blocked
        fakeEngine.enqueueNumber(99)
        repeat(15) { fakeEngine.enqueueNumber(5) }
        viewModel.generate()

        advanceUntilIdle()
        // Should still have first result
        assertEquals(10, viewModel.displayNumber.value)
    }

    @Test
    fun `generate with custom range respects range`() = runTest(testDispatcher) {
        viewModel.setRange(50, 60)
        fakeEngine.enqueueNumber(55)
        repeat(15) { fakeEngine.enqueueNumber(52) }

        viewModel.generate()
        advanceUntilIdle()

        assertEquals(55, viewModel.displayNumber.value)
    }

    @Test
    fun `generate adds entry to history`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(33)
        repeat(15) { fakeEngine.enqueueNumber(5) }

        viewModel.generate()
        advanceUntilIdle()

        assertEquals(1, viewModel.history.value.size)
        assertEquals("33", viewModel.history.value[0].label)
    }
}
