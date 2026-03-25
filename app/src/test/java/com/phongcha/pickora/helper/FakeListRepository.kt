package com.phongcha.pickora.helper

import com.phongcha.pickora.data.repository.SavedList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory ListRepository replacement for testing.
 */
class FakeListRepository {

    private val _allLists = MutableStateFlow<List<SavedList>>(emptyList())
    val allLists: Flow<List<SavedList>> = _allLists.asStateFlow()

    val lastUsedList: Flow<SavedList?> = _allLists.map { lists ->
        lists.filter { it.lastUsedAt > 0 }.maxByOrNull { it.lastUsedAt }
    }

    val lists: List<SavedList> get() = _allLists.value

    suspend fun saveList(name: String, items: List<String>, preferredMode: String = "wheel"): Long {
        val newList = SavedList(name = name, items = items, preferredMode = preferredMode)
        _allLists.value = listOf(newList) + _allLists.value
        return newList.id
    }

    suspend fun updateList(id: Long, name: String, items: List<String>) {
        val current = _allLists.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            current[index] = current[index].copy(name = name, items = items, updatedAt = System.currentTimeMillis())
            _allLists.value = current
        }
    }

    suspend fun markUsed(id: Long, mode: String? = null) {
        val current = _allLists.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            current[index] = current[index].copy(
                lastUsedAt = System.currentTimeMillis(),
                preferredMode = mode ?: current[index].preferredMode
            )
            _allLists.value = current
        }
    }

    suspend fun deleteList(id: Long) {
        _allLists.value = _allLists.value.filter { it.id != id }
    }

    suspend fun getListById(id: Long): SavedList? {
        return _allLists.value.find { it.id == id }
    }
}