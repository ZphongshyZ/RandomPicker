package com.phongcha.pickora.domain.engine

import com.phongcha.pickora.domain.model.PickerOption
import kotlin.random.Random

class SimpleRandomEngine : RandomEngine {

    override fun selectRandom(options: List<PickerOption>): PickerOption {
        require(options.isNotEmpty()) { "Options list must not be empty" }
        return options[Random.nextInt(options.size)]
    }

    override fun generateNumberInRange(min: Int, max: Int): Int {
        require(min <= max) { "min must be <= max" }
        return Random.nextInt(min, max + 1)
    }
}
