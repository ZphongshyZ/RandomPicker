package com.phongcha.pickora.ui.namepicker

import androidx.lifecycle.viewModelScope
import com.phongcha.pickora.data.repository.HistoryRepository
import com.phongcha.pickora.data.repository.ListRepository
import com.phongcha.pickora.domain.engine.RandomEngine
import com.phongcha.pickora.domain.model.PickerOption
import com.phongcha.pickora.ui.picker.BasePickerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NamePickerViewModel(
    randomEngine: RandomEngine,
    historyRepository: HistoryRepository,
    private val listRepository: ListRepository
) : BasePickerViewModel(randomEngine, historyRepository) {

    override val pickerType = "name"

    private val _highlightedIndex = MutableStateFlow(-1)
    val highlightedIndex: StateFlow<Int> = _highlightedIndex.asStateFlow()

    fun addBatchNames(text: String) {
        val names = text.split("\n", ",", ";").map { it.trim() }.filter { it.isNotBlank() }
        if (names.isEmpty()) return
        val colors = sectorColors()
        val currentSize = _options.value.size
        val newOptions = names.mapIndexed { index, name ->
            PickerOption("batch_${System.currentTimeMillis()}_$index", name, colors[(currentSize + index) % colors.size])
        }
        _options.value = _options.value + newOptions
        clearResult()
    }

    fun pickRandom() {
        val currentOptions = _options.value
        if (currentOptions.size < 2 || _isAnimating.value) return
        _isAnimating.value = true
        _result.value = null
        val winner = randomEngine.selectRandom(currentOptions)
        val winnerIndex = currentOptions.indexOf(winner)
        val snapshotSize = currentOptions.size
        viewModelScope.launch {
            repeat(20) { i ->
                _highlightedIndex.value = randomEngine.generateNumberInRange(0, snapshotSize - 1)
                delay(50L + i * 15L)
            }
            _highlightedIndex.value = winnerIndex
            delay(300)
            _isAnimating.value = false
            onResultSelected(winner)
        }
    }

    fun pickAgain() {
        dismissConfetti()
        _result.value = null
        if (_options.value.size >= 2) pickRandom()
    }

    fun removeWinner() {
        val winner = _result.value ?: return
        if (_options.value.any { it.id == winner.id }) {
            _options.value = _options.value.filter { it.id != winner.id }
        }
        _result.value = null
        _showConfetti.value = false
    }

    fun saveCurrentList(name: String) {
        val items = _options.value.map { it.label }
        if (items.isEmpty()) return
        viewModelScope.launch {
            val id = listRepository.saveList(name, items, "name")
            listRepository.markUsed(id, "name")
        }
    }

    private fun clearResult() {
        _result.value = null
        _showConfetti.value = false
    }
}
