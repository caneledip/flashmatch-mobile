package com.flashmatch.mobile.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object CreateDeck : Screen("create_deck")
    object DeckDetail : Screen("deck_detail/{deckId}") {
        fun createRoute(deckId: String) = "deck_detail/$deckId"
    }
    object EditDeck : Screen("edit_deck/{deckId}") {
        fun createRoute(deckId: String) = "edit_deck/$deckId"
    }
    object Quiz : Screen("quiz/{deckId}") {
        fun createRoute(deckId: String) = "quiz/$deckId"
    }
    object Result : Screen("result/{deckId}/{accuracy}") {
        fun createRoute(deckId: String, accuracy: Float) = "result/$deckId/$accuracy"
    }
}
