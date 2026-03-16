package com.hntech.pickora.helper

import com.hntech.pickora.domain.engine.RandomEngine
import com.hntech.pickora.domain.model.PickerOption

/**
 * Deterministic RandomEngine for testing.
 * Returns predictable results based on configured values.
 */
class FakeRandomEngine : RandomEngine {

    /** Queue of options to return from selectRandom. Falls back to first option if empty. */
    private val selectQueue = mutableListOf<Int>()

    /** Queue of numbers to return from generateNumberInRange. Falls back to min if empty. */
    private val numberQueue = mutableListOf<Int>()

    fun enqueueSelectIndex(vararg indices: Int) {
        selectQueue.addAll(indices.toList())
    }

    fun enqueueNumber(vararg numbers: Int) {
        numberQueue.addAll(numbers.toList())
    }

    override fun selectRandom(options: List<PickerOption>): PickerOption {
        require(options.isNotEmpty()) { "Options list must not be empty" }
        val index = if (selectQueue.isNotEmpty()) {
            selectQueue.removeAt(0).coerceIn(0, options.size - 1)
        } else {
            0
        }
        return options[index]
    }

    override fun generateNumberInRange(min: Int, max: Int): Int {
        require(min <= max) { "min must be <= max" }
        return if (numberQueue.isNotEmpty()) {
            numberQueue.removeAt(0).coerceIn(min, max)
        } else {
            min
        }
    }
}