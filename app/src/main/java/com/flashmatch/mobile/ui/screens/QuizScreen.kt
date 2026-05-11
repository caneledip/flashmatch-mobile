package com.flashmatch.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.flashmatch.mobile.navigation.Screen
import com.flashmatch.mobile.ui.components.FlashCard
import com.flashmatch.mobile.ui.components.QuizProgressBar
import com.flashmatch.mobile.ui.theme.Correct
import com.flashmatch.mobile.ui.theme.LocalDarkTheme
import com.flashmatch.mobile.ui.theme.LocalToggleTheme
import com.flashmatch.mobile.ui.theme.Wrong
import com.flashmatch.mobile.util.SoundManager
import com.flashmatch.mobile.viewmodel.QuizViewModel
import kotlinx.coroutines.delay

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "$m:${s.toString().padStart(2, '0')}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(navController: NavController, deckId: String, viewModel: QuizViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDark = LocalDarkTheme.current
    val toggleTheme = LocalToggleTheme.current

    val context = androidx.compose.ui.platform.LocalContext.current
    val soundManager = remember { SoundManager(context) }
    DisposableEffect(Unit) { onDispose { soundManager.release() } }

    LaunchedEffect(deckId) { viewModel.loadSession(deckId) }

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            soundManager.playComplete()
            delay(450L)
            val accuracy = if (state.totalTaps == 0) 0f
                           else state.correctTaps.toFloat() / state.totalTaps
            navController.navigate(Screen.Result.createRoute(deckId, accuracy)) {
                popUpTo(Screen.Quiz.createRoute(deckId)) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Quiz", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = formatTime(state.elapsedSeconds),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = toggleTheme) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
                state.error != null -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) { Text("Go Back") }
                }
                state.totalCards == 0 -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No cards in this deck",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) { Text("Go Back") }
                }
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    QuizProgressBar(cleared = state.clearedCount, total = state.totalCards)

                    state.currentCard?.let { card ->
                        FlashCard(
                            card = card,
                            isFlipped = state.isFlipped,
                            onFlip = {
                                soundManager.playFlip()
                                viewModel.flip()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (state.isFlipped) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        soundManager.playWrong()
                                        viewModel.markWrong()
                                    },
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Wrong),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Wrong", fontWeight = FontWeight.SemiBold)
                                }
                                Button(
                                    onClick = {
                                        soundManager.playCorrect()
                                        viewModel.markCorrect(deckId)
                                    },
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Correct),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Correct", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    soundManager.playFlip()
                                    viewModel.flip()
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("Flip Card", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        Text(
                            text = "Session taps: ${state.totalTaps}  •  Correct: ${state.correctTaps}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}
