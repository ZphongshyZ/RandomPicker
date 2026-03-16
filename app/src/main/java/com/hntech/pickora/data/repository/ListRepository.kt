package com.hntech.pickora.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

data class SavedList(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val items: List<String>,
    val preferredMode: String = "wheel",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = 0L
)

class ListRepository(private val context: Context) {

    private val gson = Gson()
    private val file get() = File(context.filesDir, "saved_lists.json")
    private val mutex = Mutex()

    private val _allLists = MutableStateFlow<List<SavedList>>(emptyList())

    val allLists: Flow<List<SavedList>> = _allLists.asStateFlow()

    val lastUsedList: Flow<SavedList?> = _allLists.map { lists ->
        lists.filter { it.lastUsedAt > 0 }.maxByOrNull { it.lastUsedAt }
    }

    init {
        _allLists.value = loadFromDisk().sortedByDescending { maxOf(it.lastUsedAt, it.updatedAt) }
    }

    suspend fun saveList(name: String, items: List<String>, preferredMode: String = "wheel"): Long {
        return mutex.withLock {
            val newList = SavedList(name = name, items = items, preferredMode = preferredMode)
            val current = listOf(newList) + _allLists.value
            _allLists.value = current
            writeToDisk(current)
            newList.id
        }
    }

    suspend fun updateList(id: Long, name: String, items: List<String>) {
        mutex.withLock {
            val current = _allLists.value.toMutableList()
            val index = current.indexOfFirst { it.id == id }
            if (index >= 0) {
                current[index] = current[index].copy(name = name, items = items, updatedAt = System.currentTimeMillis())
                _allLists.value = current.sortedByDescending { maxOf(it.lastUsedAt, it.updatedAt) }
                writeToDisk(_allLists.value)
            }
        }
    }

    suspend fun markUsed(id: Long, mode: String? = null) {
        mutex.withLock {
            val current = _allLists.value.toMutableList()
            val index = current.indexOfFirst { it.id == id }
            if (index >= 0) {
                current[index] = current[index].copy(
                    lastUsedAt = System.currentTimeMillis(),
                    preferredMode = mode ?: current[index].preferredMode
                )
                _allLists.value = current.sortedByDescending { maxOf(it.lastUsedAt, it.updatedAt) }
                writeToDisk(_allLists.value)
            }
        }
    }

    suspend fun markLastSavedAsUsed(items: List<String>, mode: String) {
        val match = _allLists.value.find { it.items == items }
        if (match != null) markUsed(match.id, mode)
    }

    suspend fun deleteList(id: Long) {
        mutex.withLock {
            _allLists.value = _allLists.value.filter { it.id != id }
            writeToDisk(_allLists.value)
        }
    }

    suspend fun getListById(id: Long): SavedList? {
        return _allLists.value.find { it.id == id }
    }

    private fun loadFromDisk(): List<SavedList> {
        return try {
            if (!file.exists()) return emptyList()
            val json = file.readText()
            if (json.isBlank()) return emptyList()
            val type = object : TypeToken<List<SavedList>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e("ListRepository", "Failed to load saved lists from disk", e)
            emptyList()
        }
    }

    private suspend fun writeToDisk(lists: List<SavedList>) {
        withContext(Dispatchers.IO) {
            try {
                val tempFile = File(context.filesDir, "saved_lists.json.tmp")
                tempFile.writeText(gson.toJson(lists))
                tempFile.renameTo(file)
            } catch (e: Exception) {
                Log.e("ListRepository", "Failed to write saved lists to disk", e)
            }
        }
    }
}
