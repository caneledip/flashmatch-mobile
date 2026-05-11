package com.flashmatch.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashmatch.mobile.data.model.Card
import com.flashmatch.mobile.data.repository.DeckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class QuizState(
    val currentCard: Card? = null,
    val isFlipped: Boolean = false,
    val clearedCount: Int = 0,
    val totalCards: Int = 0,
    val totalTaps: Int = 0,
    val correctTaps: Int = 0,
    val isComplete: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

object QuizSessionCache {
    var hardestCards: List<Card> = emptyList()
    var accuracy: Float = 0f
}

class QuizViewModel(private val repository: DeckRepository) : ViewModel() {

    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state

    private val sessionCards: MutableList<Card> = mutableListOf()
    private val clearedIds: MutableSet<String> = mutableSetOf()
    private val retryMap: MutableMap<String, Int> = mutableMapOf()

    fun loadSession(deckId: String) {
        viewModelScope.launch {
            _state.value = QuizState(isLoading = true)
            try {
                val cards = repository.getCards(deckId)
                sessionCards.clear()
                sessionCards.addAll(cards)
                clearedIds.clear()
                retryMap.clear()
                _state.value = QuizState(totalCards = cards.size, isLoading = false)
                pickNext()
            } catch (e: Exception) {
                _state.value = QuizState(isLoading = false, error = e.message)
            }
        }
    }

    fun flip() {
        _state.value = _state.value.copy(isFlipped = !_state.value.isFlipped)
    }

    fun markCorrect(deckId: String) {
        val card = _state.value.currentCard ?: return
        applyResult(card, correct = true)
        clearedIds.add(card.id)
        val correctTaps = _state.value.correctTaps + 1
        val totalTaps = _state.value.totalTaps + 1
        if (clearedIds.size == sessionCards.size) {
            finishSession(deckId, correctTaps, totalTaps)
        } else {
            _state.value = _state.value.copy(
                clearedCount = clearedIds.size,
                correctTaps = correctTaps,
                totalTaps = totalTaps,
                isFlipped = false
            )
            pickNext()
        }
    }

    fun markWrong() {
        val card = _state.value.currentCard ?: return
        applyResult(card, correct = false)
        retryMap[card.id] = (retryMap[card.id] ?: 0) + 1
        _state.value = _state.value.copy(totalTaps = _state.value.totalTaps + 1, isFlipped = false)
        pickNext()
    }

    private fun applyResult(card: Card, correct: Boolean) {
        val idx = sessionCards.indexOfFirst { it.id == card.id }
        if (idx == -1) return
        val updated = if (correct) {
            val nc = card.correctCount + 1
            card.copy(correctCount = nc, correctnessScore = nc.toFloat() / (nc + card.incorrectCount))
        } else {
            val ni = card.incorrectCount + 1
            card.copy(incorrectCount = ni, correctnessScore = card.correctCount.toFloat() / (card.correctCount + ni))
        }
        sessionCards[idx] = updated
    }

    private fun pickNext() {
        val available = sessionCards.filter { it.id !in clearedIds }
        if (available.isEmpty()) return
        val weights = available.map { (1f - it.correctnessScore).coerceAtLeast(0.01f) }
        val total = weights.sum()
        val r = Random.nextFloat() * total
        var cumulative = 0f
        var picked = available.last()
        for (i in available.indices) {
            cumulative += weights[i]
            if (r <= cumulative) { picked = available[i]; break }
        }
        _state.value = _state.value.copy(currentCard = picked)
    }

    private fun finishSession(deckId: String, correctTaps: Int, totalTaps: Int) {
        val accuracy = if (totalTaps == 0) 0f else correctTaps.toFloat() / totalTaps
        QuizSessionCache.accuracy = accuracy
        QuizSessionCache.hardestCards = retryMap.entries
            .sortedByDescending { it.value }
            .take(5)
            .mapNotNull { (id, _) -> sessionCards.find { it.id == id } }

        _state.value = _state.value.copy(
            clearedCount = sessionCards.size,
            correctTaps = correctTaps,
            totalTaps = totalTaps,
            isComplete = true,
            isFlipped = false
        )
        viewModelScope.launch {
            try { repository.batchUpdateCards(deckId, sessionCards) } catch (_: Exception) {}
        }
    }

    companion object {
        fun factory(repository: DeckRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>) = QuizViewModel(repository) as T
        }
    }
}
