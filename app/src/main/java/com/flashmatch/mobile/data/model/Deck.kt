package com.flashmatch.mobile.data.model

import com.google.firebase.firestore.DocumentId

data class Deck(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val cardCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val bestTime: Long = 0L,
    val color: String = ""
)
