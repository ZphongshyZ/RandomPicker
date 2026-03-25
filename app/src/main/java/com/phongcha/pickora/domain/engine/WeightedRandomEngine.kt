package com.phongcha.pickora.domain.engine

import com.phongcha.pickora.domain.model.PickerOption
import kotlin.random.Random

class WeightedRandomEngine : RandomEngine {

    override fun selectRandom(options: List<PickerOption>): PickerOption {
        require(options.isNotEmpty()) { "Options list must not be empty" }

        val totalWeight = options.sumOf { it.weight.toDouble() }
        val randomValue = Random.nextDouble() * totalWeight

        var cumulative = 0.0
        for (option in options) {
            cumulative += option.weight
            if (randomValue < cumulative) return option
        }

        return options.last()
    }

    override fun generateNumberInRange(min: Int, max: Int): Int {
        require(min <= max) { "min must be <= max" }
        return Random.nextInt(min, max + 1)
    }
}
