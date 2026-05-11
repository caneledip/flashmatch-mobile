package com.flashmatch.mobile.viewmodel

import com.flashmatch.mobile.data.model.Card
import kotlin.random.Random

class QuizEngine {

    private val sessionCards: MutableList<Card> = mutableListOf()
    private val clearedIds: MutableSet<String> = mutableSetOf()
    private val retryMap: MutableMap<String, Int> = mutableMapOf()

    val cleared: Int get() = clearedIds.size
    val total: Int get() = sessionCards.size
    val isComplete: Boolean get() = sessionCards.isNotEmpty() && clearedIds.size == sessionCards.size

    fun startSession(cards: List<Card>) {
        sessionCards.clear()
        sessionCards.addAll(cards)
        clearedIds.clear()
        retryMap.clear()
    }

    fun markCorrect(card: Card): Card {
        val updated = applyResult(card, correct = true)
        clearedIds.add(card.id)
        return updated
    }

    fun markWrong(card: Card): Card {
        retryMap[card.id] = (retryMap[card.id] ?: 0) + 1
        return applyResult(card, correct = false)
    }

    fun pickNext(): Card? {
        val available = sessionCards.filter { it.id !in clearedIds }
        if (available.isEmpty()) return null
        val weights = available.map { (1f - it.correctnessScore).coerceAtLeast(0.01f) }
        val total = weights.sum()
        val r = Random.nextFloat() * total
        var cumulative = 0f
        for (i in available.indices) {
            cumulative += weights[i]
            if (r <= cumulative) return available[i]
        }
        return available.last()
    }

    fun getHardestCards(limit: Int = 5): List<Card> =
        retryMap.entries
            .sortedByDescending { it.value }
            .take(limit)
            .mapNotNull { (id, _) -> sessionCards.find { it.id == id } }

    fun getSessionCards(): List<Card> = sessionCards.toList()

    private fun applyResult(card: Card, correct: Boolean): Card {
        val idx = sessionCards.indexOfFirst { it.id == card.id }
        val updated = if (correct) {
            val nc = card.correctCount + 1
            card.copy(correctCount = nc, correctnessScore = nc.toFloat() / (nc + card.incorrectCount))
        } else {
            val ni = card.incorrectCount + 1
            card.copy(incorrectCount = ni, correctnessScore = card.correctCount.toFloat() / (card.correctCount + ni))
        }
        if (idx != -1) sessionCards[idx] = updated
        return updated
    }
}
