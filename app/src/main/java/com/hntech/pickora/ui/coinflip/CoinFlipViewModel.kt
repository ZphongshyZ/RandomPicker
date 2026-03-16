package com.hntech.pickora.ui.coinflip

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.hntech.pickora.data.repository.HistoryRepository
import com.hntech.pickora.domain.engine.RandomEngine
import com.hntech.pickora.domain.model.PickerOption
import com.hntech.pickora.ui.picker.BasePickerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CoinFlipViewModel(
    randomEngine: RandomEngine,
    historyRepository: HistoryRepository
) : BasePickerViewModel(randomEngine, historyRepository) {

    override val pickerType = "coinflip"

    private val _isHeads = MutableStateFlow<Boolean?>(null)
    val isHeads: StateFlow<Boolean?> = _isHeads.asStateFlow()

    private val _flipRotation = MutableStateFlow(0f)
    val flipRotation: StateFlow<Float> = _flipRotation.asStateFlow()

    private val _targetFlipRotation = MutableStateFlow(0f)
    val targetFlipRotation: StateFlow<Float> = _targetFlipRotation.asStateFlow()

    private val heads = PickerOption("heads", "Heads", Color(0xFFFFD700))
    private val tails = PickerOption("tails", "Tails", Color(0xFFC0C0C0))

    fun flip() {
        if (_isAnimating.value) return
        _isAnimating.value = true
        _result.value = null

        val isHeadsResult = randomEngine.generateNumberInRange(0, 1) == 1
        val winner = if (isHeadsResult) heads else tails

        // Calculate rotation: each 180° is a flip. Heads = even multiples, Tails = odd
        val flips = 6 + randomEngine.generateNumberInRange(2, 6)
        val targetDegrees = _flipRotation.value + flips * 180f +
            if (isHeadsResult) 0f else 180f

        _targetFlipRotation.value = targetDegrees

        viewModelScope.launch {
            // Wait for animation to complete (driven by Compose)
            delay(1500L)
            _isHeads.value = isHeadsResult
            _flipRotation.value = targetDegrees
            _isAnimating.value = false
            onResultSelected(winner)
        }
    }
}
