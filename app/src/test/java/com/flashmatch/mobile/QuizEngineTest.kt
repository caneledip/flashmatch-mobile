package com.flashmatch.mobile

import com.flashmatch.mobile.data.model.Card
import com.flashmatch.mobile.viewmodel.QuizEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class QuizEngineTest {

    private lateinit var engine: QuizEngine

    private val sampleCards = listOf(
        Card(id = "1", front = "Apple",  back = "A fruit"),
        Card(id = "2", front = "Banana", back = "Yellow fruit"),
        Card(id = "3", front = "Cherry", back = "Red fruit")
    )

    @Before
    fun setUp() {
        engine = QuizEngine()
        engine.startSession(sampleCards)
    }

    // --- Session initialisation ---

    @Test
    fun startSession_setsCorrectTotal() {
        assertEquals(3, engine.total)
    }

    @Test
    fun startSession_clearedIsZero() {
        assertEquals(0, engine.cleared)
    }

    @Test
    fun startSession_isNotComplete() {
        assertFalse(engine.isComplete)
    }

    @Test
    fun startSession_pickNextReturnsACard() {
        assertNotNull(engine.pickNext())
    }

    // --- markCorrect ---

    @Test
    fun markCorrect_incrementsCorrectCount() {
        val card = sampleCards[0]
        val updated = engine.markCorrect(card)
        assertEquals(1, updated.correctCount)
    }

    @Test
    fun markCorrect_increasesCorrectnessScore() {
        val card = sampleCards[0] // new card: score = 0.5
        val updated = engine.markCorrect(card)
        // correctCount=1, incorrectCount=0 → score = 1/1 = 1.0
        assertEquals(1.0f, updated.correctnessScore, 0.001f)
    }

    @Test
    fun markCorrect_addsCardToCleared() {
        val card = sampleCards[0]
        engine.markCorrect(card)
        assertEquals(1, engine.cleared)
    }

    @Test
    fun markCorrect_allCards_completesSession() {
        sampleCards.forEach { engine.markCorrect(it) }
        assertTrue(engine.isComplete)
    }

    // --- markWrong ---

    @Test
    fun markWrong_incrementsIncorrectCount() {
        val card = sampleCards[0]
        val updated = engine.markWrong(card)
        assertEquals(1, updated.incorrectCount)
    }

    @Test
    fun markWrong_doesNotClearCard() {
        val card = sampleCards[0]
        engine.markWrong(card)
        assertEquals(0, engine.cleared)
    }

    @Test
    fun markWrong_doesNotCompleteSession() {
        sampleCards.forEach { engine.markWrong(it) }
        assertFalse(engine.isComplete)
    }

    @Test
    fun markWrong_newCard_decreasesCorrectnessScore() {
        val card = sampleCards[0] // new card: score = 0.5, correctCount = 0
        val updated = engine.markWrong(card)
        // correctCount=0, incorrectCount=1 → score = 0/1 = 0.0
        assertEquals(0.0f, updated.correctnessScore, 0.001f)
    }

    // --- Session completion ---

    @Test
    fun sessionEnds_onlyWhenAllCardsCleared() {
        // Mark 2 out of 3 correct — not done yet
        engine.markCorrect(sampleCards[0])
        engine.markCorrect(sampleCards[1])
        assertFalse(engine.isComplete)

        // Mark last one correct — now done
        engine.markCorrect(sampleCards[2])
        assertTrue(engine.isComplete)
    }

    @Test
    fun wrongAnswer_doesNotEndSession_evenIfAllOthersCleared() {
        engine.markCorrect(sampleCards[0])
        engine.markCorrect(sampleCards[1])
        engine.markWrong(sampleCards[2])  // wrong — should NOT clear
        assertFalse(engine.isComplete)
    }

    // --- pickNext ---

    @Test
    fun pickNext_returnsNull_whenAllCleared() {
        sampleCards.forEach { engine.markCorrect(it) }
        assertNull(engine.pickNext())
    }

    @Test
    fun pickNext_neverReturnsCleared_card() {
        engine.markCorrect(sampleCards[0])
        engine.markCorrect(sampleCards[1])
        // Only card 3 remains
        repeat(20) {
            val next = engine.pickNext()
            assertNotNull(next)
            assertNotEquals("1", next!!.id)
            assertNotEquals("2", next.id)
        }
    }

    // --- Hardest cards ---

    @Test
    fun hardestCards_empty_whenNoRetries() {
        sampleCards.forEach { engine.markCorrect(it) }
        assertTrue(engine.getHardestCards().isEmpty())
    }

    @Test
    fun hardestCards_orderedByRetryCount() {
        engine.markWrong(sampleCards[2]) // 1 retry
        engine.markWrong(sampleCards[2]) // 2 retries
        engine.markWrong(sampleCards[0]) // 1 retry

        val hardest = engine.getHardestCards()
        assertEquals("3", hardest[0].id) // cherry had 2 retries — hardest
        assertEquals("1", hardest[1].id) // apple had 1 retry
    }

    // --- Score formula ---

    @Test
    fun correctnessScore_formula_mixedHistory() {
        val card = Card(id = "x", front = "Q", back = "A", correctCount = 3, incorrectCount = 1)
        val afterCorrect = engine.run {
            startSession(listOf(card))
            markCorrect(card)
        }
        // correctCount = 4, incorrectCount = 1 → score = 4/5 = 0.8
        assertEquals(0.8f, afterCorrect.correctnessScore, 0.001f)
    }

    @Test
    fun emptySession_isNotComplete() {
        engine.startSession(emptyList())
        assertFalse(engine.isComplete)
    }
}
