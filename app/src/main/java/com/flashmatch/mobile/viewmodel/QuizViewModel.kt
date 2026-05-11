package com.flashmatch.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashmatch.mobile.data.model.Card
import com.flashmatch.mobile.data.repository.DeckRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class QuizState(
    val currentCard: Card? = null,
    val isFlipped: Boolean = false,
    val clearedCount: Int = 0,
    val totalCards: Int = 0,
    val totalTaps: Int = 0,
    val correctTaps: Int = 0,
    val isComplete: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val elapsedSeconds: Int = 0
)

object QuizSessionCache {
    var hardestCards: List<Card> = emptyList()
    var accuracy: Float = 0f
    var elapsedSeconds: Int = 0
    var isNewRecord: Boolean = false
}

class QuizViewModel(private val repository: DeckRepository) : ViewModel() {

    private val engine = QuizEngine()

    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state

    private var timerJob: Job? = null
    private var previousBestTime: Long = 0L

    fun loadSession(deckId: String) {
        viewModelScope.launch {
            _state.value = QuizState(isLoading = true)
            try {
                val cards = repository.getCards(deckId)
                val deck = repository.getDeck(deckId)
                previousBestTime = deck?.bestTime ?: 0L
                engine.startSession(cards)
                _state.value = QuizState(totalCards = engine.total, isLoading = false)
                advanceCard()
                startTimer()
            } catch (e: Exception) {
                _state.value = QuizState(isLoading = false, error = e.message)
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                _state.value = _state.value.copy(elapsedSeconds = _state.value.elapsedSeconds + 1)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun flip() {
        _state.value = _state.value.copy(isFlipped = !_state.value.isFlipped)
    }

    fun markCorrect(deckId: String) {
        val card = _state.value.currentCard ?: return
        engine.markCorrect(card)
        val correctTaps = _state.value.correctTaps + 1
        val totalTaps = _state.value.totalTaps + 1
        if (engine.isComplete) {
            finishSession(deckId, correctTaps, totalTaps)
        } else {
            _state.value = _state.value.copy(
                clearedCount = engine.cleared,
                correctTaps = correctTaps,
                totalTaps = totalTaps,
                isFlipped = false
            )
            advanceCard()
        }
    }

    fun markWrong() {
        val card = _state.value.currentCard ?: return
        engine.markWrong(card)
        _state.value = _state.value.copy(totalTaps = _state.value.totalTaps + 1, isFlipped = false)
        advanceCard()
    }

    private fun advanceCard() {
        _state.value = _state.value.copy(currentCard = engine.pickNext())
    }

    private fun finishSession(deckId: String, correctTaps: Int, totalTaps: Int) {
        stopTimer()
        val elapsed = _state.value.elapsedSeconds
        val accuracy = if (totalTaps == 0) 0f else correctTaps.toFloat() / totalTaps
        val isNewRecord = previousBestTime == 0L || elapsed < previousBestTime.toInt()

        QuizSessionCache.accuracy = accuracy
        QuizSessionCache.hardestCards = engine.getHardestCards()
        QuizSessionCache.elapsedSeconds = elapsed
        QuizSessionCache.isNewRecord = isNewRecord

        _state.value = _state.value.copy(
            clearedCount = engine.total,
            correctTaps = correctTaps,
            totalTaps = totalTaps,
            isComplete = true,
            isFlipped = false
        )
        viewModelScope.launch {
            try {
                repository.batchUpdateCards(deckId, engine.getSessionCards())
                repository.updateDeckStats(deckId, elapsed.toLong(), isNewRecord)
            } catch (_: Exception) {}
        }
    }

    companion object {
        fun factory(repository: DeckRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>) = QuizViewModel(repository) as T
        }
    }
}
