package com.phongcha.pickora.domain.engine

import androidx.compose.ui.graphics.Color
import com.phongcha.pickora.domain.model.PickerOption
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SimpleRandomEngineTest {

    private lateinit var engine: SimpleRandomEngine

    @Before
    fun setup() {
        engine = SimpleRandomEngine()
    }

    // --- selectRandom ---

    @Test
    fun `selectRandom returns an option from the list`() {
        val options = listOf(
            PickerOption("1", "A", Color.Red),
            PickerOption("2", "B", Color.Blue),
            PickerOption("3", "C", Color.Green)
        )
        val result = engine.selectRandom(options)
        assertTrue("Result should be in the options list", result in options)
    }

    @Test
    fun `selectRandom with single option returns that option`() {
        val option = PickerOption("1", "Only", Color.Red)
        val result = engine.selectRandom(listOf(option))
        assertEquals(option, result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `selectRandom with empty list throws IllegalArgumentException`() {
        engine.selectRandom(emptyList())
    }

    @Test
    fun `selectRandom covers all options over many iterations`() {
        val options = listOf(
            PickerOption("1", "A", Color.Red),
            PickerOption("2", "B", Color.Blue),
            PickerOption("3", "C", Color.Green)
        )
        val results = (1..1000).map { engine.selectRandom(options) }.toSet()
        assertEquals("All options should be selected at least once", options.toSet(), results)
    }

    // --- generateNumberInRange ---

    @Test
    fun `generateNumberInRange returns number within range`() {
        repeat(100) {
            val result = engine.generateNumberInRange(1, 10)
            assertTrue("Result $result should be in [1, 10]", result in 1..10)
        }
    }

    @Test
    fun `generateNumberInRange with same min and max returns that number`() {
        val result = engine.generateNumberInRange(5, 5)
        assertEquals(5, result)
    }

    @Test
    fun `generateNumberInRange with min 0 and max 0 returns 0`() {
        assertEquals(0, engine.generateNumberInRange(0, 0))
    }

    @Test
    fun `generateNumberInRange covers full range`() {
        val results = (1..1000).map { engine.generateNumberInRange(0, 2) }.toSet()
        assertEquals("Should cover 0, 1, 2", setOf(0, 1, 2), results)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generateNumberInRange with min greater than max throws`() {
        engine.generateNumberInRange(10, 5)
    }

    @Test
    fun `generateNumberInRange with negative numbers works`() {
        repeat(100) {
            val result = engine.generateNumberInRange(-5, -1)
            assertTrue("Result $result should be in [-5, -1]", result in -5..-1)
        }
    }

    @Test
    fun `generateNumberInRange with large range works`() {
        repeat(100) {
            val result = engine.generateNumberInRange(0, 1000000)
            assertTrue("Result $result should be in [0, 1000000]", result in 0..1000000)
        }
    }
}
