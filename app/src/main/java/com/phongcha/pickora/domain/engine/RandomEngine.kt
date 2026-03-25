package com.phongcha.pickora.domain.engine

import com.phongcha.pickora.domain.model.PickerOption

interface RandomEngine {
    fun selectRandom(options: List<PickerOption>): PickerOption
    fun generateNumberInRange(min: Int, max: Int): Int
}
