package com.flashmatch.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.flashmatch.mobile.data.model.Card
import com.flashmatch.mobile.ui.components.DeckColorPicker
import com.flashmatch.mobile.ui.components.deckSwatches
import com.flashmatch.mobile.ui.theme.LocalDarkTheme
import com.flashmatch.mobile.ui.theme.LocalToggleTheme
import com.flashmatch.mobile.viewmodel.CardDraft
import com.flashmatch.mobile.viewmodel.CreateDeckViewModel
import com.flashmatch.mobile.viewmodel.DeckDetailViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDeckScreen(
    navController: NavController,
    deckId: String,
    loadViewModel: DeckDetailViewModel,
    saveViewModel: CreateDeckViewModel
) {
    val deck by loadViewModel.deck.collectAsStateWithLifecycle()
    val loadedCards by loadViewModel.cards.collectAsStateWithLifecycle()
    val isLoading by loadViewModel.isLoading.collectAsStateWithLifecycle()
    val isSaving by saveViewModel.isSaving.collectAsStateWithLifecycle()
    val savedDeckId by saveViewModel.savedDeckId.collectAsStateWithLifecycle()
    val error by saveViewModel.error.collectAsStateWithLifecycle()

    var deckName by remember { mutableStateOf("") }
    var deckDesc by remember { mutableStateOf("") }
    var deckColor by remember { mutableStateOf(deckSwatches.first()) }
    var existingCards by remember { mutableStateOf<List<Card>>(emptyList()) }
    var newCards by remember { mutableStateOf<List<CardDraft>>(emptyList()) }
    var deletedCardIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var initialized by remember { mutableStateOf(false) }
    val isDark = LocalDarkTheme.current
    val toggleTheme = LocalToggleTheme.current

    LaunchedEffect(deckId) { loadViewModel.load(deckId) }

    LaunchedEffect(deck, loadedCards, isLoading) {
        if (!isLoading && !initialized && deck != null) {
            deckName = deck!!.name
            deckDesc = deck!!.description
            deckColor = deck!!.color.ifEmpty { deckSwatches.first() }
            existingCards = loadedCards
            initialized = true
        }
    }

    LaunchedEffect(savedDeckId) {
        if (savedDeckId != null) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Deck", fontWeight = FontWeight.SemiBold) },
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
                    TextButton(
                        onClick = {
                            saveViewModel.updateDeck(
                                deckId = deckId,
                                name = deckName,
                                description = deckDesc,
                                color = deckColor,
                                updatedExisting = existingCards,
                                newCards = newCards,
                                deletedCardIds = deletedCardIds
                            )
                        },
                        enabled = !isSaving && initialized
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Save", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (!initialized) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    label = { Text("Deck Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }
            item {
                OutlinedTextField(
                    value = deckDesc,
                    onValueChange = { deckDesc = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
            item {
                DeckColorPicker(selected = deckColor, onSelect = { deckColor = it })
            }
            if (existingCards.isNotEmpty()) {
                item {
                    Text(
                        text = "Existing Cards",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                itemsIndexed(existingCards, key = { _, c -> c.id }) { index, card ->
                    CardEditorItem(
                        index = index,
                        front = card.front,
                        back = card.back,
                        onFrontChange = { v ->
                            existingCards = existingCards.toMutableList().also { it[index] = it[index].copy(front = v) }
                        },
                        onBackChange = { v ->
                            existingCards = existingCards.toMutableList().also { it[index] = it[index].copy(back = v) }
                        },
                        onRemove = {
                            deletedCardIds = deletedCardIds + card.id
                            existingCards = existingCards.toMutableList().also { it.removeAt(index) }
                        }
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Cards  (${newCards.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    FilledTonalButton(
                        onClick = { newCards = newCards + CardDraft(id = UUID.randomUUID().toString()) }
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Card")
                    }
                }
            }
            itemsIndexed(newCards, key = { _, c -> c.id }) { index, card ->
                CardEditorItem(
                    index = existingCards.size + index,
                    front = card.front,
                    back = card.back,
                    onFrontChange = { v -> newCards = newCards.toMutableList().also { it[index] = it[index].copy(front = v) } },
                    onBackChange = { v -> newCards = newCards.toMutableList().also { it[index] = it[index].copy(back = v) } },
                    onRemove = { newCards = newCards.toMutableList().also { it.removeAt(index) } }
                )
            }
            if (error != null) {
                item {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
