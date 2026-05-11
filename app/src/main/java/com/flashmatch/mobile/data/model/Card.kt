package com.flashmatch.mobile.data.model

import com.google.firebase.firestore.DocumentId

data class Card(
    @DocumentId val id: String = "",
    val front: String = "",
    val back: String = "",
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val correctnessScore: Float = 0.5f
)
