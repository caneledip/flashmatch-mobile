package com.flashmatch.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashmatch.mobile.data.model.Deck
import com.flashmatch.mobile.data.repository.DeckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: DeckRepository) : ViewModel() {

    private val _decks = MutableStateFlow<List<Deck>>(emptyList())
    val decks: StateFlow<List<Deck>> = _decks

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        observeDecks()
    }

    private fun observeDecks() {
        viewModelScope.launch {
            repository.observeDecks()
                .catch { e -> _error.value = e.message; _isLoading.value = false }
                .collect { list ->
                    _decks.value = list.sortedByDescending { it.createdAt }
                    _isLoading.value = false
                }
        }
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            try {
                repository.deleteDeck(deckId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    companion object {
        fun factory(repository: DeckRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>) = HomeViewModel(repository) as T
        }
    }
}
