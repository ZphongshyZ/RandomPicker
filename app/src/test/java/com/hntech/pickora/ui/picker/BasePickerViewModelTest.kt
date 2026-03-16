package com.hntech.pickora.ui.picker

import androidx.compose.ui.graphics.Color
import com.hntech.pickora.data.repository.HistoryRepository
import com.hntech.pickora.domain.model.PickerOption
import com.hntech.pickora.helper.FakeRandomEngine
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BasePickerViewModelTest {

    private lateinit var fakeEngine: FakeRandomEngine
    private lateinit var historyRepo: HistoryRepository
    private lateinit var viewModel: TestPickerViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    /** Concrete subclass for testing the abstract BasePickerViewModel */
    class TestPickerViewModel(
        engine: FakeRandomEngine,
        historyRepo: HistoryRepository
    ) : BasePickerViewModel(engine, historyRepo) {
        override val pickerType = "test"

        fun selectResult(winner: PickerOption) = onResultSelected(winner)
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeEngine = FakeRandomEngine()
        historyRepo = mockk(relaxed = true)
        coEvery { historyRepo.addEntry(any(), any(), any()) } returns Unit
        viewModel = TestPickerViewModel(fakeEngine, historyRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Initial State ---

    @Test
    fun `initial state has empty options`() {
        assertTrue(viewModel.options.value.isEmpty())
    }

    @Test
    fun `initial state is not animating`() {
        assertFalse(viewModel.isAnimating.value)
    }

    @Test
    fun `initial result is null`() {
        assertNull(viewModel.result.value)
    }

    @Test
    fun `initial removeAfterPick is false`() {
        assertFalse(viewModel.removeAfterPick.value)
    }

    @Test
    fun `initial showConfetti is false`() {
        assertFalse(viewModel.showConfetti.value)
    }

    // --- toggleRemoveAfterPick ---

    @Test
    fun `toggleRemoveAfterPick toggles value`() {
        assertFalse(viewModel.removeAfterPick.value)
        viewModel.toggleRemoveAfterPick()
        assertTrue(viewModel.removeAfterPick.value)
        viewModel.toggleRemoveAfterPick()
        assertFalse(viewModel.removeAfterPick.value)
    }

    // --- updateOptions ---

    @Test
    fun `updateOptions sets options`() {
        val options = listOf(
            PickerOption("1", "A", Color.Red),
            PickerOption("2", "B", Color.Blue)
        )
        viewModel.updateOptions(options)
        assertEquals(options, viewModel.options.value)
    }

    @Test
    fun `updateOptions replaces existing options`() {
        viewModel.updateOptions(listOf(PickerOption("1", "A", Color.Red)))
        val newOptions = listOf(PickerOption("2", "B", Color.Blue))
        viewModel.updateOptions(newOptions)
        assertEquals(newOptions, viewModel.options.value)
    }

    // --- loadOptionsFromStrings ---

    @Test
    fun `loadOptionsFromStrings creates options with correct labels`() {
        viewModel.loadOptionsFromStrings(listOf("Apple", "Banana", "Cherry"))
        val options = viewModel.options.value
        assertEquals(3, options.size)
        assertEquals("Apple", options[0].label)
        assertEquals("Banana", options[1].label)
        assertEquals("Cherry", options[2].label)
    }

    @Test
    fun `loadOptionsFromStrings assigns colors from sectorColors`() {
        val colors = BasePickerViewModel.sectorColors()
        viewModel.loadOptionsFromStrings(listOf("A", "B", "C"))
        val options = viewModel.options.value
        assertEquals(colors[0], options[0].color)
        assertEquals(colors[1], options[1].color)
        assertEquals(colors[2], options[2].color)
    }

    @Test
    fun `loadOptionsFromStrings wraps colors when more items than colors`() {
        val colors = BasePickerViewModel.sectorColors()
        val items = (1..colors.size + 2).map { "Item $it" }
        viewModel.loadOptionsFromStrings(items)
        val options = viewModel.options.value
        assertEquals(colors[0], options[colors.size].color) // wraps around
    }

    @Test
    fun `loadOptionsFromStrings clears previous result`() {
        val option = PickerOption("1", "A", Color.Red)
        viewModel.updateOptions(listOf(option))
        viewModel.selectResult(option)
        assertNotNull(viewModel.result.value)

        viewModel.loadOptionsFromStrings(listOf("New"))
        assertNull(viewModel.result.value)
    }

    // --- addOption ---

    @Test
    fun `addOption adds to existing options`() {
        viewModel.loadOptionsFromStrings(listOf("A"))
        viewModel.addOption("B")
        assertEquals(2, viewModel.options.value.size)
        assertEquals("B", viewModel.options.value[1].label)
    }

    @Test
    fun `addOption to empty list creates first option`() {
        viewModel.addOption("First")
        assertEquals(1, viewModel.options.value.size)
        assertEquals("First", viewModel.options.value[0].label)
    }

    // --- addBatchOptions ---

    @Test
    fun `addBatchOptions splits by comma`() {
        viewModel.addBatchOptions("A, B, C")
        assertEquals(3, viewModel.options.value.size)
        assertEquals("A", viewModel.options.value[0].label)
        assertEquals("B", viewModel.options.value[1].label)
        assertEquals("C", viewModel.options.value[2].label)
    }

    @Test
    fun `addBatchOptions splits by newline`() {
        viewModel.addBatchOptions("A\nB\nC")
        assertEquals(3, viewModel.options.value.size)
    }

    @Test
    fun `addBatchOptions splits by semicolon`() {
        viewModel.addBatchOptions("A;B;C")
        assertEquals(3, viewModel.options.value.size)
    }

    @Test
    fun `addBatchOptions ignores empty entries`() {
        viewModel.addBatchOptions("A,,B,  ,C")
        assertEquals(3, viewModel.options.value.size)
    }

    @Test
    fun `addBatchOptions appends to existing options`() {
        viewModel.loadOptionsFromStrings(listOf("Existing"))
        viewModel.addBatchOptions("New1, New2")
        assertEquals(3, viewModel.options.value.size)
    }

    @Test
    fun `addBatchOptions with empty text does nothing`() {
        viewModel.addBatchOptions("")
        assertTrue(viewModel.options.value.isEmpty())
    }

    @Test
    fun `addBatchOptions with only whitespace does nothing`() {
        viewModel.addBatchOptions("   ,  , \n ")
        assertTrue(viewModel.options.value.isEmpty())
    }

    // --- updateOptionColor ---

    @Test
    fun `updateOptionColor changes color of matching option`() {
        viewModel.loadOptionsFromStrings(listOf("A", "B"))
        val id = viewModel.options.value[0].id
        viewModel.updateOptionColor(id, Color.Magenta)
        assertEquals(Color.Magenta, viewModel.options.value[0].color)
    }

    @Test
    fun `updateOptionColor does not affect other options`() {
        viewModel.loadOptionsFromStrings(listOf("A", "B"))
        val originalColorB = viewModel.options.value[1].color
        val idA = viewModel.options.value[0].id
        viewModel.updateOptionColor(idA, Color.Magenta)
        assertEquals(originalColorB, viewModel.options.value[1].color)
    }

    @Test
    fun `updateOptionColor with non-existent id does nothing`() {
        viewModel.loadOptionsFromStrings(listOf("A"))
        val original = viewModel.options.value.toList()
        viewModel.updateOptionColor("non_existent", Color.Magenta)
        assertEquals(original, viewModel.options.value)
    }

    // --- updateOptionWeight ---

    @Test
    fun `updateOptionWeight changes weight`() {
        viewModel.loadOptionsFromStrings(listOf("A"))
        val id = viewModel.options.value[0].id
        viewModel.updateOptionWeight(id, 5f)
        assertEquals(5f, viewModel.options.value[0].weight)
    }

    @Test
    fun `updateOptionWeight clamps to minimum 0_1`() {
        viewModel.loadOptionsFromStrings(listOf("A"))
        val id = viewModel.options.value[0].id
        viewModel.updateOptionWeight(id, 0f)
        assertEquals(0.1f, viewModel.options.value[0].weight)
    }

    @Test
    fun `updateOptionWeight clamps to maximum 10`() {
        viewModel.loadOptionsFromStrings(listOf("A"))
        val id = viewModel.options.value[0].id
        viewModel.updateOptionWeight(id, 20f)
        assertEquals(10f, viewModel.options.value[0].weight)
    }

    // --- removeOption ---

    @Test
    fun `removeOption removes matching option`() {
        viewModel.loadOptionsFromStrings(listOf("A", "B", "C"))
        val idB = viewModel.options.value[1].id
        viewModel.removeOption(idB)
        assertEquals(2, viewModel.options.value.size)
        assertFalse(viewModel.options.value.any { it.id == idB })
    }

    @Test
    fun `removeOption clears result if winner was removed`() {
        viewModel.loadOptionsFromStrings(listOf("A", "B"))
        val winner = viewModel.options.value[0]
        viewModel.selectResult(winner)
        assertNotNull(viewModel.result.value)

        viewModel.removeOption(winner.id)
        assertNull(viewModel.result.value)
    }

    @Test
    fun `removeOption does not clear result if non-winner removed`() {
        viewModel.loadOptionsFromStrings(listOf("A", "B"))
        val winner = viewModel.options.value[0]
        val other = viewModel.options.value[1]
        viewModel.selectResult(winner)

        viewModel.removeOption(other.id)
        assertNotNull(viewModel.result.value)
    }

    // --- onResultSelected ---

    @Test
    fun `onResultSelected sets result and showConfetti`() {
        val option = PickerOption("1", "Winner", Color.Red)
        viewModel.updateOptions(listOf(option))
        viewModel.selectResult(option)

        assertEquals(option, viewModel.result.value)
        assertTrue(viewModel.showConfetti.value)
    }

    @Test
    fun `onResultSelected adds to history`() {
        val option = PickerOption("1", "Winner", Color.Red)
        viewModel.updateOptions(listOf(option))
        viewModel.selectResult(option)

        assertEquals(1, viewModel.history.value.size)
        assertEquals(option, viewModel.history.value[0])
    }

    @Test
    fun `onResultSelected with removeAfterPick removes winner from options`() {
        viewModel.loadOptionsFromStrings(listOf("A", "B", "C"))
        viewModel.toggleRemoveAfterPick()
        val winner = viewModel.options.value[1]

        viewModel.selectResult(winner)

        assertFalse(viewModel.options.value.any { it.id == winner.id })
        assertEquals(2, viewModel.options.value.size)
    }

    @Test
    fun `onResultSelected without removeAfterPick keeps all options`() {
        viewModel.loadOptionsFromStrings(listOf("A", "B", "C"))
        val winner = viewModel.options.value[1]

        viewModel.selectResult(winner)

        assertEquals(3, viewModel.options.value.size)
    }

    @Test
    fun `multiple results are prepended to history`() {
        val opt1 = PickerOption("1", "First", Color.Red)
        val opt2 = PickerOption("2", "Second", Color.Blue)
        viewModel.updateOptions(listOf(opt1, opt2))

        viewModel.selectResult(opt1)
        viewModel.selectResult(opt2)

        assertEquals(2, viewModel.history.value.size)
        assertEquals(opt2, viewModel.history.value[0]) // most recent first
        assertEquals(opt1, viewModel.history.value[1])
    }

    // --- clearHistory ---

    @Test
    fun `clearHistory empties history`() {
        val option = PickerOption("1", "A", Color.Red)
        viewModel.updateOptions(listOf(option))
        viewModel.selectResult(option)
        assertFalse(viewModel.history.value.isEmpty())

        viewModel.clearHistory()
        assertTrue(viewModel.history.value.isEmpty())
    }

    // --- dismissConfetti ---

    @Test
    fun `dismissConfetti sets showConfetti to false`() {
        val option = PickerOption("1", "A", Color.Red)
        viewModel.updateOptions(listOf(option))
        viewModel.selectResult(option)
        assertTrue(viewModel.showConfetti.value)

        viewModel.dismissConfetti()
        assertFalse(viewModel.showConfetti.value)
    }

    // --- sectorColors ---

    @Test
    fun `sectorColors returns 10 colors`() {
        assertEquals(10, BasePickerViewModel.sectorColors().size)
    }

    @Test
    fun `sectorColors returns distinct colors`() {
        val colors = BasePickerViewModel.sectorColors()
        assertEquals(colors.size, colors.distinct().size)
    }
}
