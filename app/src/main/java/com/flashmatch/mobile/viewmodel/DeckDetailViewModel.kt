package com.flashmatch.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashmatch.mobile.data.model.Card
import com.flashmatch.mobile.data.model.Deck
import com.flashmatch.mobile.data.repository.DeckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DeckDetailViewModel(private val repository: DeckRepository) : ViewModel() {

    private val _deck = MutableStateFlow<Deck?>(null)
    val deck: StateFlow<Deck?> = _deck

    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: StateFlow<List<Card>> = _cards

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun load(deckId: String) {
        viewModelScope.launch {
            try {
                _deck.value = repository.getDeck(deckId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
        viewModelScope.launch {
            repository.observeCards(deckId)
                .catch { e -> _error.value = e.message; _isLoading.value = false }
                .collect { list ->
                    _cards.value = list
                    _isLoading.value = false
                }
        }
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            try {
                repository.deleteDeck(deckId)
                _deleted.value = true
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    companion object {
        fun factory(repository: DeckRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>) = DeckDetailViewModel(repository) as T
        }
    }
}
