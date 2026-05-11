package com.flashmatch.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashmatch.mobile.data.model.Card
import com.flashmatch.mobile.data.repository.DeckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CardDraft(
    val id: String = "",
    val front: String = "",
    val back: String = ""
)

class CreateDeckViewModel(private val repository: DeckRepository) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _savedDeckId = MutableStateFlow<String?>(null)
    val savedDeckId: StateFlow<String?> = _savedDeckId

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun createDeck(name: String, description: String, color: String, newCards: List<CardDraft>) {
        if (name.isBlank()) { _error.value = "Deck name cannot be empty"; return }
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val deckId = repository.createDeck(name, description, color)
                newCards.filter { it.front.isNotBlank() }.forEach {
                    repository.addCard(deckId, it.front, it.back)
                }
                _savedDeckId.value = deckId
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save deck"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun updateDeck(
        deckId: String,
        name: String,
        description: String,
        color: String,
        updatedExisting: List<Card>,
        newCards: List<CardDraft>,
        deletedCardIds: Set<String>
    ) {
        if (name.isBlank()) { _error.value = "Deck name cannot be empty"; return }
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val existing = repository.getDeck(deckId)
                    ?: throw Exception("Deck not found")
                repository.updateDeck(existing.copy(name = name, description = description, color = color))
                deletedCardIds.forEach { repository.deleteCard(deckId, it) }
                updatedExisting.forEach { repository.updateCard(deckId, it) }
                newCards.filter { it.front.isNotBlank() }.forEach {
                    repository.addCard(deckId, it.front, it.back)
                }
                _savedDeckId.value = deckId
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update deck"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearError() { _error.value = null }

    companion object {
        fun factory(repository: DeckRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>) = CreateDeckViewModel(repository) as T
        }
    }
}
