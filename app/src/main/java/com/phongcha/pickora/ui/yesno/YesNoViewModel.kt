package com.phongcha.pickora.ui.yesno

import androidx.compose.ui.graphics.Color
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

class YesNoViewModel(
    randomEngine: RandomEngine,
    historyRepository: HistoryRepository
) : BasePickerViewModel(randomEngine, historyRepository) {

    override val pickerType = "yesno"

    /** true = YES, false = NO, null = no answer yet */
    private val _displayAnswer = MutableStateFlow<Boolean?>(null)
    val displayAnswer: StateFlow<Boolean?> = _displayAnswer.asStateFlow()

    private val yesOption = PickerOption("yes", "YES", Color(0xFF4CAF50))
    private val noOption = PickerOption("no", "NO", Color(0xFFF44336))

    fun decide() {
        if (_isAnimating.value) return
        _isAnimating.value = true
        _result.value = null

        val isYes = randomEngine.generateNumberInRange(0, 1) == 1
        val winner = if (isYes) yesOption else noOption

        viewModelScope.launch {
            repeat(12) { i ->
                _displayAnswer.value = i % 2 == 0
                delay(80L + i * 20L)
            }

            _displayAnswer.value = isYes
            _isAnimating.value = false
            onResultSelected(winner)
        }
    }
}
