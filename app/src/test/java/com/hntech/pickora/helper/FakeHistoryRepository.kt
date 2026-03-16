package com.hntech.pickora.helper

import com.hntech.pickora.data.repository.HistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory HistoryRepository replacement for testing.
 * Avoids Android Context dependency and file I/O.
 */
class FakeHistoryRepository {

    private val _allHistory = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val allHistory: Flow<List<HistoryEntry>> = _allHistory.asStateFlow()

    val entries: List<HistoryEntry> get() = _allHistory.value

    fun getRecentHistory(limit: Int = 50): Flow<List<HistoryEntry>> {
        return _allHistory.map { it.take(limit) }
    }

    suspend fun addEntry(pickerType: String, result: String, options: List<String>) {
        val entry = HistoryEntry(
            id = System.currentTimeMillis(),
            pickerType = pickerType,
            result = result,
            options = options
        )
        val current = listOf(entry) + _allHistory.value
        _allHistory.value = current.take(200)
    }

    suspend fun clearAll() {
        _allHistory.value = emptyList()
    }

    suspend fun deleteEntry(id: Long) {
        _allHistory.value = _allHistory.value.filter { it.id != id }
    }
}