package com.flashmatch.mobile

import com.flashmatch.mobile.data.model.Deck
import org.junit.Assert.*
import org.junit.Test

/** Feature 4 (completion badge) and Feature 5 (time record) — Deck model tests. */
class DeckTest {

    @Test
    fun isCompleted_defaultsToFalse() {
        assertFalse(Deck().isCompleted)
    }

    @Test
    fun bestTime_defaultsToZero() {
        assertEquals(0L, Deck().bestTime)
    }

    @Test
    fun isCompleted_canBeSetToTrue() {
        assertTrue(Deck(isCompleted = true).isCompleted)
    }

    @Test
    fun bestTime_canBeSet() {
        assertEquals(90L, Deck(bestTime = 90L).bestTime)
    }

    @Test
    fun deck_withBothNewFields_retainsExistingFields() {
        val deck = Deck(
            id = "abc",
            name = "Vocab",
            cardCount = 5,
            isCompleted = true,
            bestTime = 42L
        )
        assertEquals("abc", deck.id)
        assertEquals("Vocab", deck.name)
        assertEquals(5, deck.cardCount)
        assertTrue(deck.isCompleted)
        assertEquals(42L, deck.bestTime)
    }

    @Test
    fun deck_copy_updatesIsCompleted() {
        val original = Deck(isCompleted = false)
        val updated = original.copy(isCompleted = true)
        assertTrue(updated.isCompleted)
        assertFalse(original.isCompleted)
    }

    @Test
    fun deck_copy_updatesBestTime() {
        val original = Deck(bestTime = 0L)
        val updated = original.copy(bestTime = 60L)
        assertEquals(60L, updated.bestTime)
        assertEquals(0L, original.bestTime)
    }
}
