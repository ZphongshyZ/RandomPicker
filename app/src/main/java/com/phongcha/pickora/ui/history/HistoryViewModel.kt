package com.phongcha.pickora.ui.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phongcha.pickora.data.repository.HistoryEntry
import com.phongcha.pickora.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    val history: StateFlow<List<HistoryEntry>> = historyRepository.getRecentHistory(100)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun dismissError() {
        _errorMessage.value = null
    }

    fun clearAll() {
        viewModelScope.launch {
            try {
                historyRepository.clearAll()
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Failed to clear history", e)
                _errorMessage.value = "Failed to clear history"
            }
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            try {
                historyRepository.deleteEntry(id)
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Failed to delete entry", e)
                _errorMessage.value = "Failed to delete entry"
            }
        }
    }
}
