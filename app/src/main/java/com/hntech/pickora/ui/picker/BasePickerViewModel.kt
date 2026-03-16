package com.hntech.pickora.ui.picker

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hntech.pickora.data.repository.HistoryRepository
import com.hntech.pickora.domain.engine.RandomEngine
import com.hntech.pickora.domain.model.PickerOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BasePickerViewModel(
    protected val randomEngine: RandomEngine,
    protected val historyRepository: HistoryRepository
) : ViewModel() {

    abstract val pickerType: String

    protected val _options = MutableStateFlow<List<PickerOption>>(emptyList())
    val options: StateFlow<List<PickerOption>> = _options.asStateFlow()

    protected val _isAnimating = MutableStateFlow(false)
    val isAnimating: StateFlow<Boolean> = _isAnimating.asStateFlow()

    protected val _result = MutableStateFlow<PickerOption?>(null)
    val result: StateFlow<PickerOption?> = _result.asStateFlow()

    protected val _history = MutableStateFlow<List<PickerOption>>(emptyList())
    val history: StateFlow<List<PickerOption>> = _history.asStateFlow()

    private val _removeAfterPick = MutableStateFlow(false)
    val removeAfterPick: StateFlow<Boolean> = _removeAfterPick.asStateFlow()

    protected val _showConfetti = MutableStateFlow(false)
    val showConfetti: StateFlow<Boolean> = _showConfetti.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun dismissError() {
        _errorMessage.value = null
    }

    fun toggleRemoveAfterPick() {
        _removeAfterPick.value = !_removeAfterPick.value
    }

    fun updateOptions(newOptions: List<PickerOption>) {
        _options.value = newOptions
    }

    fun loadOptionsFromStrings(items: List<String>) {
        val colors = sectorColors()
        _options.value = items.mapIndexed { index, label ->
            PickerOption(
                id = "item_$index",
                label = label,
                color = colors[index % colors.size]
            )
        }
        clearResult()
    }

    fun addBatchOptions(text: String) {
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

    fun addOption(label: String) {
        val current = _options.value
        val colors = sectorColors()
        val newOption = PickerOption(
            id = "opt_${System.currentTimeMillis()}",
            label = label,
            color = colors[current.size % colors.size]
        )
        _options.value = current + newOption
        clearResult()
    }

    fun updateOptionColor(id: String, color: Color) {
        _options.value = _options.value.map { if (it.id == id) it.copy(color = color) else it }
    }

    fun updateOptionWeight(id: String, weight: Float) {
        _options.value = _options.value.map { if (it.id == id) it.copy(weight = weight.coerceIn(0.1f, 10f)) else it }
    }

    fun removeOption(id: String) {
        _options.value = _options.value.filter { it.id != id }
        _result.value?.let { current ->
            if (current.id == id) clearResult()
        }
    }

    private fun clearResult() {
        _result.value = null
        _showConfetti.value = false
    }

    fun clearHistory() {
        _history.value = emptyList()
    }

    fun dismissConfetti() {
        _showConfetti.value = false
    }

    protected fun onResultSelected(winner: PickerOption) {
        _result.value = winner
        _showConfetti.value = true
        addToHistory(winner)

        viewModelScope.launch {
            try {
                val optionsToSave = if (_options.value.isNotEmpty()) _options.value.map { it.label } else emptyList()
                historyRepository.addEntry(
                    pickerType = pickerType,
                    result = winner.label,
                    options = optionsToSave
                )
            } catch (e: Exception) {
                Log.e("BasePickerViewModel", "Failed to save history entry", e)
            }
        }

        if (_removeAfterPick.value) {
            _options.value = _options.value.filter { it.id != winner.id }
        }
    }

    private fun addToHistory(option: PickerOption) {
        _history.value = listOf(option) + _history.value
    }

    companion object {
        private val SECTOR_COLORS = listOf(
            Color(0xFFE57373), // Red
            Color(0xFF81C784), // Green
            Color(0xFF64B5F6), // Blue
            Color(0xFFFFD54F), // Yellow
            Color(0xFFBA68C8), // Purple
            Color(0xFF4DD0E1), // Cyan
            Color(0xFFFF8A65), // Orange
            Color(0xFFA1887F), // Brown
            Color(0xFF90A4AE), // Grey
            Color(0xFFF06292), // Pink
        )

        fun sectorColors(): List<Color> = SECTOR_COLORS
    }
}
