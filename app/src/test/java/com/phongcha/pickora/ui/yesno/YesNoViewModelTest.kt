package com.phongcha.pickora.ui.yesno

import com.phongcha.pickora.data.repository.HistoryRepository
import com.phongcha.pickora.helper.FakeRandomEngine
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
class YesNoViewModelTest {

    private lateinit var fakeEngine: FakeRandomEngine
    private lateinit var historyRepo: HistoryRepository
    private lateinit var viewModel: YesNoViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeEngine = FakeRandomEngine()
        historyRepo = mockk(relaxed = true)
        coEvery { historyRepo.addEntry(any(), any(), any()) } returns Unit
        viewModel = YesNoViewModel(fakeEngine, historyRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `pickerType is yesno`() {
        assertEquals("yesno", viewModel.pickerType)
    }

    @Test
    fun `initial displayAnswer is null`() {
        assertNull(viewModel.displayAnswer.value)
    }

    @Test
    fun `decide with result 1 selects YES`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(1)
        viewModel.decide()
        advanceUntilIdle()

        assertTrue(viewModel.displayAnswer.value == true)
        assertEquals("YES", viewModel.result.value?.label)
        assertTrue(viewModel.showConfetti.value)
    }

    @Test
    fun `decide with result 0 selects NO`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(0)
        viewModel.decide()
        advanceUntilIdle()

        assertTrue(viewModel.displayAnswer.value == false)
        assertEquals("NO", viewModel.result.value?.label)
    }

    @Test
    fun `decide adds to history`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(1)
        viewModel.decide()
        advanceUntilIdle()

        assertEquals(1, viewModel.history.value.size)
        assertEquals("YES", viewModel.history.value[0].label)
    }

    @Test
    fun `decide sets isAnimating to false after completion`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(0)
        viewModel.decide()
        advanceUntilIdle()
        assertFalse(viewModel.isAnimating.value)
    }

    @Test
    fun `decide does not run when already animating`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(1)
        viewModel.decide()
        // Don't advance - still animating
        assertTrue(viewModel.isAnimating.value)

        fakeEngine.enqueueNumber(0)
        viewModel.decide() // Should be blocked

        advanceUntilIdle()
        // Result should be from first decide (YES)
        assertEquals("YES", viewModel.result.value?.label)
    }

    @Test
    fun `multiple decides track history`() = runTest(testDispatcher) {
        fakeEngine.enqueueNumber(1)
        viewModel.decide()
        advanceUntilIdle()

        fakeEngine.enqueueNumber(0)
        viewModel.decide()
        advanceUntilIdle()

        assertEquals(2, viewModel.history.value.size)
        assertEquals("NO", viewModel.history.value[0].label)
        assertEquals("YES", viewModel.history.value[1].label)
    }
}
