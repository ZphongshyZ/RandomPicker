package com.phongcha.pickora.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

data class HistoryEntry(
    val id: Long = System.currentTimeMillis(),
    val pickerType: String,
    val result: String,
    val options: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

class HistoryRepository(private val context: Context) {

    private val gson = Gson()
    private val file get() = File(context.filesDir, "history.json")
    private val mutex = Mutex()

    private val _allHistory = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val allHistory: Flow<List<HistoryEntry>> = _allHistory.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val entries = loadFromDisk().sortedByDescending { it.timestamp }
            _allHistory.value = entries
        }
    }

    fun getRecentHistory(limit: Int = 50): Flow<List<HistoryEntry>> {
        return _allHistory.map { it.take(limit) }
    }

    suspend fun addEntry(pickerType: String, result: String, options: List<String>) {
        mutex.withLock {
            val entry = HistoryEntry(
                pickerType = pickerType,
                result = result,
                options = options
            )
            val current = listOf(entry) + _allHistory.value
            _allHistory.value = current.take(200)
            writeToDisk(_allHistory.value)
        }
    }

    suspend fun clearAll() {
        mutex.withLock {
            _allHistory.value = emptyList()
            writeToDisk(emptyList())
        }
    }

    suspend fun deleteEntry(id: Long) {
        mutex.withLock {
            val current = _allHistory.value.filter { it.id != id }
            _allHistory.value = current
            writeToDisk(current)
        }
    }

    private fun loadFromDisk(): List<HistoryEntry> {
        return try {
            if (!file.exists()) return emptyList()
            val json = file.readText()
            if (json.isBlank()) return emptyList()
            val type = object : TypeToken<List<HistoryEntry>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e("HistoryRepository", "Failed to load history from disk", e)
            emptyList()
        }
    }

    private suspend fun writeToDisk(entries: List<HistoryEntry>) {
        withContext(Dispatchers.IO) {
            try {
                val tempFile = File(context.filesDir, "history.json.tmp")
                tempFile.writeText(gson.toJson(entries))
                tempFile.renameTo(file)
            } catch (e: Exception) {
                Log.e("HistoryRepository", "Failed to write history to disk", e)
            }
        }
    }
}
