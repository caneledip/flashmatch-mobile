package com.flashmatch.mobile.data.repository

import com.flashmatch.mobile.data.model.Card
import com.flashmatch.mobile.data.model.Deck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DeckRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val uid: String get() = auth.currentUser?.uid ?: error("User not authenticated")

    private fun decksRef() = firestore.collection("users").document(uid).collection("decks")
    private fun cardsRef(deckId: String) = decksRef().document(deckId).collection("cards")

    fun observeDecks(): Flow<List<Deck>> = callbackFlow {
        val listener = decksRef().addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val decks = snapshot?.documents?.mapNotNull { it.toObject(Deck::class.java) } ?: emptyList()
            trySend(decks)
        }
        awaitClose { listener.remove() }
    }

    suspend fun getDeck(deckId: String): Deck? =
        decksRef().document(deckId).get().await().toObject(Deck::class.java)

    suspend fun createDeck(name: String, description: String): String {
        val ref = decksRef().document()
        val deck = Deck(id = ref.id, name = name, description = description)
        ref.set(deck).await()
        return ref.id
    }

    suspend fun updateDeck(deck: Deck) {
        decksRef().document(deck.id).set(deck).await()
    }

    suspend fun deleteDeck(deckId: String) {
        val cards = cardsRef(deckId).get().await()
        val batch = firestore.batch()
        cards.documents.forEach { batch.delete(it.reference) }
        batch.delete(decksRef().document(deckId))
        batch.commit().await()
    }

    fun observeCards(deckId: String): Flow<List<Card>> = callbackFlow {
        val listener = cardsRef(deckId).addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val cards = snapshot?.documents?.mapNotNull { it.toObject(Card::class.java) } ?: emptyList()
            trySend(cards)
        }
        awaitClose { listener.remove() }
    }

    suspend fun getCards(deckId: String): List<Card> =
        cardsRef(deckId).get().await().documents.mapNotNull { it.toObject(Card::class.java) }

    suspend fun addCard(deckId: String, front: String, back: String): String {
        val ref = cardsRef(deckId).document()
        val card = Card(id = ref.id, front = front, back = back)
        ref.set(card).await()
        refreshCardCount(deckId)
        return ref.id
    }

    suspend fun updateCard(deckId: String, card: Card) {
        cardsRef(deckId).document(card.id).set(card).await()
    }

    suspend fun deleteCard(deckId: String, cardId: String) {
        cardsRef(deckId).document(cardId).delete().await()
        refreshCardCount(deckId)
    }

    suspend fun batchUpdateCards(deckId: String, cards: List<Card>) {
        if (cards.isEmpty()) return
        val batch = firestore.batch()
        cards.forEach { card -> batch.set(cardsRef(deckId).document(card.id), card) }
        batch.commit().await()
    }

    private suspend fun refreshCardCount(deckId: String) {
        val count = cardsRef(deckId).get().await().size()
        decksRef().document(deckId).update("cardCount", count).await()
    }
}
