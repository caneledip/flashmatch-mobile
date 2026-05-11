package com.flashmatch.mobile.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.flashmatch.mobile.auth.AuthViewModel
import com.flashmatch.mobile.data.repository.DeckRepository
import com.flashmatch.mobile.ui.screens.*
import com.flashmatch.mobile.viewmodel.CreateDeckViewModel
import com.flashmatch.mobile.viewmodel.DeckDetailViewModel
import com.flashmatch.mobile.viewmodel.HomeViewModel
import com.flashmatch.mobile.viewmodel.QuizViewModel

@Composable
fun FlashMatchNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val repository = remember { DeckRepository() }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(Screen.Home.route) {
            val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository))
            HomeScreen(navController = navController, viewModel = vm, authViewModel = authViewModel)
        }

        composable(Screen.CreateDeck.route) {
            val vm: CreateDeckViewModel = viewModel(factory = CreateDeckViewModel.factory(repository))
            CreateDeckScreen(navController = navController, viewModel = vm)
        }

        composable(
            route = Screen.DeckDetail.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStack ->
            val deckId = backStack.arguments?.getString("deckId") ?: ""
            val vm: DeckDetailViewModel = viewModel(factory = DeckDetailViewModel.factory(repository))
            DeckDetailScreen(navController = navController, deckId = deckId, viewModel = vm)
        }

        composable(
            route = Screen.EditDeck.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStack ->
            val deckId = backStack.arguments?.getString("deckId") ?: ""
            val loadVm: DeckDetailViewModel = viewModel(factory = DeckDetailViewModel.factory(repository))
            val saveVm: CreateDeckViewModel = viewModel(factory = CreateDeckViewModel.factory(repository))
            EditDeckScreen(
                navController = navController,
                deckId = deckId,
                loadViewModel = loadVm,
                saveViewModel = saveVm
            )
        }

        composable(
            route = Screen.Quiz.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStack ->
            val deckId = backStack.arguments?.getString("deckId") ?: ""
            val vm: QuizViewModel = viewModel(factory = QuizViewModel.factory(repository))
            QuizScreen(navController = navController, deckId = deckId, viewModel = vm)
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("deckId") { type = NavType.StringType },
                navArgument("accuracy") { type = NavType.FloatType }
            )
        ) { backStack ->
            val deckId = backStack.arguments?.getString("deckId") ?: ""
            val accuracy = backStack.arguments?.getFloat("accuracy") ?: 0f
            ResultScreen(navController = navController, deckId = deckId, accuracy = accuracy)
        }
    }
}
