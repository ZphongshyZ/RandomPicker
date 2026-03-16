package com.hntech.pickora.domain.engine

import com.hntech.pickora.domain.model.PickerOption

interface RandomEngine {
    fun selectRandom(options: List<PickerOption>): PickerOption
    fun generateNumberInRange(min: Int, max: Int): Int
}
