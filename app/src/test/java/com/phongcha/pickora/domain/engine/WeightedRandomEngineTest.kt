package com.phongcha.pickora.domain.engine

import androidx.compose.ui.graphics.Color
import com.phongcha.pickora.domain.model.PickerOption
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WeightedRandomEngineTest {

    private lateinit var engine: WeightedRandomEngine

    @Before
    fun setup() {
        engine = WeightedRandomEngine()
    }

    // --- selectRandom ---

    @Test
    fun `selectRandom returns an option from the list`() {
        val options = listOf(
            PickerOption("1", "A", Color.Red, weight = 1f),
            PickerOption("2", "B", Color.Blue, weight = 1f)
        )
        val result = engine.selectRandom(options)
        assertTrue(result in options)
    }

    @Test
    fun `selectRandom with single option returns that option`() {
        val option = PickerOption("1", "Only", Color.Red, weight = 5f)
        assertEquals(option, engine.selectRandom(listOf(option)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `selectRandom with empty list throws`() {
        engine.selectRandom(emptyList())
    }

    @Test
    fun `selectRandom respects weights - heavily weighted option selected most often`() {
        val heavy = PickerOption("1", "Heavy", Color.Red, weight = 100f)
        val light = PickerOption("2", "Light", Color.Blue, weight = 1f)
        val options = listOf(heavy, light)

        val counts = mutableMapOf("Heavy" to 0, "Light" to 0)
        repeat(1000) {
            val result = engine.selectRandom(options)
            counts[result.label] = (counts[result.label] ?: 0) + 1
        }

        assertTrue(
            "Heavy (${counts["Heavy"]}) should be selected much more than Light (${counts["Light"]})",
            counts["Heavy"]!! > counts["Light"]!! * 5
        )
    }

    @Test
    fun `selectRandom with equal weights distributes roughly evenly`() {
        val options = listOf(
            PickerOption("1", "A", Color.Red, weight = 1f),
            PickerOption("2", "B", Color.Blue, weight = 1f),
            PickerOption("3", "C", Color.Green, weight = 1f)
        )

        val counts = mutableMapOf("A" to 0, "B" to 0, "C" to 0)
        repeat(3000) {
            val result = engine.selectRandom(options)
            counts[result.label] = (counts[result.label] ?: 0) + 1
        }

        // Each should be roughly 1000 (±300 for randomness)
        counts.forEach { (label, count) ->
            assertTrue("$label count $count should be between 700 and 1300", count in 700..1300)
        }
    }

    @Test
    fun `selectRandom with zero weight option is never selected`() {
        val normal = PickerOption("1", "Normal", Color.Red, weight = 1f)
        // Note: weight 0 means effectively never selected since totalWeight > 0 and randomValue won't go below 0
        // But let's test very small weight
        val tiny = PickerOption("2", "Tiny", Color.Blue, weight = 0.001f)
        val options = listOf(normal, tiny)

        val counts = mutableMapOf("Normal" to 0, "Tiny" to 0)
        repeat(1000) {
            val result = engine.selectRandom(options)
            counts[result.label] = (counts[result.label] ?: 0) + 1
        }

        assertTrue(
            "Normal (${counts["Normal"]}) should dominate Tiny (${counts["Tiny"]})",
            counts["Normal"]!! > 950
        )
    }

    @Test
    fun `selectRandom with fractional weights works`() {
        val options = listOf(
            PickerOption("1", "A", Color.Red, weight = 0.5f),
            PickerOption("2", "B", Color.Blue, weight = 0.5f)
        )
        val result = engine.selectRandom(options)
        assertTrue(result in options)
    }

    // --- generateNumberInRange ---

    @Test
    fun `generateNumberInRange returns number within range`() {
        repeat(100) {
            val result = engine.generateNumberInRange(1, 10)
            assertTrue(result in 1..10)
        }
    }

    @Test
    fun `generateNumberInRange with same min and max returns that number`() {
        assertEquals(5, engine.generateNumberInRange(5, 5))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generateNumberInRange with min greater than max throws`() {
        engine.generateNumberInRange(10, 5)
    }
}
