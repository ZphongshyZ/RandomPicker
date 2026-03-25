package com.phongcha.pickora.ui.savedlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phongcha.pickora.data.repository.ListRepository
import com.phongcha.pickora.data.repository.SavedList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedListViewModel(
    private val listRepository: ListRepository
) : ViewModel() {

    val savedLists: StateFlow<List<SavedList>> = listRepository.allLists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun dismissError() {
        _errorMessage.value = null
    }

    fun saveNewList(name: String, items: List<String>, preferredMode: String = "wheel") {
        viewModelScope.launch {
            try {
                listRepository.saveList(name, items, preferredMode)
            } catch (e: Exception) {
                Log.e("SavedListViewModel", "Failed to save list", e)
                _errorMessage.value = "Failed to save list"
            }
        }
    }

    fun updateList(id: Long, name: String, items: List<String>) {
        viewModelScope.launch {
            try {
                listRepository.updateList(id, name, items)
            } catch (e: Exception) {
                Log.e("SavedListViewModel", "Failed to update list", e)
                _errorMessage.value = "Failed to update list"
            }
        }
    }

    fun deleteList(id: Long) {
        viewModelScope.launch {
            try {
                listRepository.deleteList(id)
            } catch (e: Exception) {
                Log.e("SavedListViewModel", "Failed to delete list", e)
                _errorMessage.value = "Failed to delete list"
            }
        }
    }

    fun duplicateList(list: SavedList) {
        viewModelScope.launch {
            try {
                listRepository.saveList("${list.name} (copy)", list.items, list.preferredMode)
            } catch (e: Exception) {
                Log.e("SavedListViewModel", "Failed to duplicate list", e)
                _errorMessage.value = "Failed to duplicate list"
            }
        }
    }

    fun renameList(id: Long, newName: String) {
        viewModelScope.launch {
            try {
                val existing = listRepository.getListById(id) ?: return@launch
                listRepository.updateList(id, newName, existing.items)
            } catch (e: Exception) {
                Log.e("SavedListViewModel", "Failed to rename list", e)
                _errorMessage.value = "Failed to rename list"
            }
        }
    }
}
