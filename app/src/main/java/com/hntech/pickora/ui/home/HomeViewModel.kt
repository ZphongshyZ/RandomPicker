package com.hntech.pickora.ui.home

import androidx.lifecycle.ViewModel
import com.hntech.pickora.data.repository.HistoryEntry
import com.hntech.pickora.data.repository.HistoryRepository
import com.hntech.pickora.data.repository.ListRepository
import com.hntech.pickora.data.repository.SavedList
import kotlinx.coroutines.flow.Flow

class HomeViewModel(
    historyRepository: HistoryRepository,
    private val listRepository: ListRepository
) : ViewModel() {
    val recentPicks: Flow<List<HistoryEntry>> = historyRepository.getRecentHistory(3)
    val lastUsedList: Flow<SavedList?> = listRepository.lastUsedList
}
