package com.hntech.pickora.ui.coinflip

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
class CoinFlipViewModelTest {

    private lateinit var fakeEngine: FakeRandomEngine
    private lateinit var historyRepo: HistoryRepository
    private lateinit var viewModel: CoinFlipViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeEngine = FakeRandomEngine()
        historyRepo = mockk(relaxed = true)
        coEvery { historyRepo.addEntry(any(), any(), any()) } returns Unit
        viewModel = CoinFlipViewModel(fakeEngine, historyRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `pickerType is coinflip`() {
        assertEquals("coinflip", viewModel.pickerType)
    }

    @Test
    fun `initial isHeads is null`() {
        assertNull(viewModel.isHeads.value)
    }

    @Test
    fun `initial flipRotation is 0`() {
        assertEquals(0f, viewModel.flipRotation.value)
    }

    @Test
    fun `flip with result 1 selects Heads`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(1) // isHeads = true
        fakeEngine.enqueueNumber(4) // extra flips (6 + 4 = 10)
        viewModel.flip()
        advanceUntilIdle()

        assertEquals(true, viewModel.isHeads.value)
        assertEquals("Heads", viewModel.result.value?.label)
        assertTrue(viewModel.showConfetti.value)
    }

    @Test
    fun `flip with result 0 selects Tails`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(0) // isHeads = false
        fakeEngine.enqueueNumber(3) // extra flips
        viewModel.flip()
        advanceUntilIdle()

        assertEquals(false, viewModel.isHeads.value)
        assertEquals("Tails", viewModel.result.value?.label)
    }

    @Test
    fun `flip updates rotation for Heads`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(1) // Heads
        fakeEngine.enqueueNumber(2) // extra flips = 2, total flips = 6 + 2 = 8
        viewModel.flip()
        advanceUntilIdle()

        // Heads: 8 * 180 + 0 = 1440
        assertEquals(1440f, viewModel.flipRotation.value)
    }

    @Test
    fun `flip updates rotation for Tails with extra 180`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(0) // Tails
        fakeEngine.enqueueNumber(2) // extra flips = 2, total = 8
        viewModel.flip()
        advanceUntilIdle()

        // Tails: 8 * 180 + 180 = 1620
        assertEquals(1620f, viewModel.flipRotation.value)
    }

    @Test
    fun `flip completes and stops animating`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(1)
        fakeEngine.enqueueNumber(3)
        viewModel.flip()
        advanceUntilIdle()
        assertFalse(viewModel.isAnimating.value)
    }

    @Test
    fun `flip adds to history`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(0)
        fakeEngine.enqueueNumber(2)
        viewModel.flip()
        advanceUntilIdle()

        assertEquals(1, viewModel.history.value.size)
        assertEquals("Tails", viewModel.history.value[0].label)
    }

    @Test
    fun `consecutive flips accumulate rotation`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(1) // Heads
        fakeEngine.enqueueNumber(2) // 8 flips
        viewModel.flip()
        advanceUntilIdle()
        val firstRotation = viewModel.flipRotation.value
        assertTrue(firstRotation > 0)

        fakeEngine.enqueueNumber(0) // Tails
        fakeEngine.enqueueNumber(2) // 8 flips
        viewModel.flip()
        advanceUntilIdle()

        assertTrue(viewModel.flipRotation.value > firstRotation)
    }

    @Test
    fun `flip does not run when already animating`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(1)
        fakeEngine.enqueueNumber(3)
        viewModel.flip()
        // Don't advance - still animating
        assertTrue(viewModel.isAnimating.value)

        fakeEngine.enqueueNumber(0)
        fakeEngine.enqueueNumber(2)
        viewModel.flip() // blocked

        advanceUntilIdle()
        assertEquals("Heads", viewModel.result.value?.label)
    }
}
