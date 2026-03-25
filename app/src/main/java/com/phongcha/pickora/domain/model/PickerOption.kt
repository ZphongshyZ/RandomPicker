package com.phongcha.pickora.domain.model

import androidx.compose.ui.graphics.Color

data class PickerOption(
    val id: String,
    val label: String,
    val color: Color,
    val weight: Float = 1f
)
