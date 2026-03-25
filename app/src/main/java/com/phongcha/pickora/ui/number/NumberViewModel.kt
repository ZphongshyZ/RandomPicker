package com.phongcha.pickora.ui.number

import androidx.lifecycle.viewModelScope
import com.phongcha.pickora.data.repository.HistoryRepository
import com.phongcha.pickora.domain.engine.RandomEngine
import com.phongcha.pickora.domain.model.PickerOption
import com.phongcha.pickora.ui.picker.BasePickerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NumberViewModel(
    randomEngine: RandomEngine,
    historyRepository: HistoryRepository
) : BasePickerViewModel(randomEngine, historyRepository) {

    override val pickerType = "number"

    private val _minValue = MutableStateFlow(1)
    val minValue: StateFlow<Int> = _minValue.asStateFlow()

    private val _maxValue = MutableStateFlow(100)
    val maxValue: StateFlow<Int> = _maxValue.asStateFlow()

    private val _displayNumber = MutableStateFlow<Int?>(null)
    val displayNumber: StateFlow<Int?> = _displayNumber.asStateFlow()

    fun setRange(min: Int, max: Int) {
        _minValue.value = min
        _maxValue.value = max
    }

    fun generate() {
        if (_isAnimating.value) return

        val min = _minValue.value
        val max = _maxValue.value
        if (min > max) return

        _isAnimating.value = true

        val finalNumber = randomEngine.generateNumberInRange(min, max)

        viewModelScope.launch {
            repeat(15) { i ->
                _displayNumber.value = randomEngine.generateNumberInRange(min, max)
                delay(50L + i * 15L)
            }

            _displayNumber.value = finalNumber
            _isAnimating.value = false

            val resultOption = PickerOption(
                id = "num_${System.currentTimeMillis()}",
                label = finalNumber.toString(),
                color = sectorColors()[Math.abs(finalNumber) % sectorColors().size]
            )
            onResultSelected(resultOption)
        }
    }
}
