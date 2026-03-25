package com.phongcha.pickora.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phongcha.pickora.data.repository.HistoryEntry
import com.phongcha.pickora.data.repository.HistoryRepository
import com.phongcha.pickora.data.repository.ListRepository
import com.phongcha.pickora.data.repository.SavedList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    historyRepository: HistoryRepository,
    private val listRepository: ListRepository
) : ViewModel() {
    val recentPicks: StateFlow<List<HistoryEntry>> = historyRepository.getRecentHistory(3)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val lastUsedList: StateFlow<SavedList?> = listRepository.lastUsedList
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
