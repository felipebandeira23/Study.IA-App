package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Deck
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    onNavigateToReview: (Int) -> Unit, // passes deck ID
    modifier: Modifier = Modifier
) {
    val decks by viewModel.allDecks.collectAsState()
    val flashcardState by viewModel.flashcardState.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Criar, 1 = Meus Decks
    var deckTitleInput by remember { mutableStateOf("") }
    var deckContentInput by remember { mutableStateOf("") }
    var cardCountSelected by remember { mutableFloatStateOf(5f) } // Default 5 cards

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetFlashcardState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flashcards Automatizados 🧠") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("flashcards_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Header
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Gere Decks", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_create_deck")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Decks de Estudo (${decks.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_saved_decks")
                )
            }

            when (activeTab) {
                0 -> {
                    // Form to create decks of flashcards
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Transforme qualquer tema em flashcards automáticos de recordação ativa rápida:",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = deckTitleInput,
                                onValueChange = { deckTitleInput = it },
                                label = { Text("Nome do Deck (ex: Constituição Federal, Pomodoro)") },
                                placeholder = { Text("Defina um título chamativo...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("deck_title_input"),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = deckContentInput,
                                onValueChange = { deckContentInput = it },
                                label = { Text("Tema ou conteúdo teórico base") },
                                placeholder = { Text("Cole tópicos de um edital público, anotações de aula, resoluções de leis, ou descreva o assunto livremente...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .testTag("deck_content_input"),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        item {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Quantidade de cards no deck:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${cardCountSelected.toInt()} cards",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = cardCountSelected,
                                    onValueChange = { cardCountSelected = it },
                                    valueRange = 3f..15f,
                                    steps = 11, // Covers integers from 3 to 15
                                    modifier = Modifier.testTag("deck_count_slider")
                                )
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    if (deckTitleInput.isNotBlank()) {
                                        val contentText = if (deckContentInput.isNotBlank()) deckContentInput else deckTitleInput
                                        viewModel.createFlashcardsDeck(
                                            title = deckTitleInput,
                                            contentOrTopic = contentText,
                                            count = cardCountSelected.toInt()
                                        )
                                    }
                                },
                                enabled = deckTitleInput.isNotBlank() && flashcardState !is UiState.Loading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("generate_deck_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (flashcardState is UiState.Loading) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Gerando seus flashcards de estudo...")
                                } else {
                                    Icon(Icons.Default.School, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gerar Decks com Inteligência Artificial")
                                }
                            }
                        }

                        item {
                            DeckOutputBlock(
                                state = flashcardState,
                                onSaveSuccess = { generatedId ->
                                    deckTitleInput = ""
                                    deckContentInput = ""
                                    viewModel.resetFlashcardState()
                                    // Automatically navigate directly to review the game
                                    onNavigateToReview(generatedId)
                                }
                            )
                        }
                    }
                }
                1 -> {
                    // List of decks
                    if (decks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Nenhum deck de flashcards gerado.",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Preencha as especificações teóricas na outra guia e assista a IA criar um jogo de perguntas e respostas dinâmico e focado.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(decks) { deck ->
                                val dueCount by viewModel.getDueCountForDeck(deck.id)
                                    .collectAsState(initial = 0)
                                DeckListItem(
                                    deck = deck,
                                    dueCount = dueCount,
                                    onPlay = { onNavigateToReview(deck.id) },
                                    onDelete = { viewModel.deleteDeck(deck.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeckOutputBlock(
    state: UiState<Int>,
    onSaveSuccess: (Int) -> Unit
) {
    AnimatedVisibility(
        visible = state !is UiState.Idle,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        when (state) {
            is UiState.Loading -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "A IA está processando seu material educacional para formular perguntas inteligentes e respostas precisas. Isso pode demorar alguns instantes...",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            is UiState.Success -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Deck Gerado com Sucesso!",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onSaveSuccess(state.data) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("Iniciar Revisão Imediata")
                        }
                    }
                }
            }
            is UiState.Error -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Falha ao Estruturar Deck",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            UiState.Idle -> {}
        }
    }
}

@Composable
fun DeckListItem(
    deck: Deck,
    dueCount: Int = 0,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    var showConfirmDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("deck_item_${deck.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = deck.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (dueCount > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "$dueCount pendente${if (dueCount > 1) "s" else ""}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = deck.topic,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = { showConfirmDelete = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir deck",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Style,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (dueCount > 0) "Cards pendentes para revisar" else "Revisão em dia ✓",
                        fontSize = 12.sp,
                        color = if (dueCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onPlay,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("deck_play_button_${deck.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (dueCount > 0) MaterialTheme.colorScheme.primary
                                         else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (dueCount > 0) MaterialTheme.colorScheme.onPrimary
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Revisar")
                }
            }
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Excluir Deck?") },
            text = { Text("Isso excluirá permanentemente o deck '${deck.title}' e todos os seus cards. Deseja prosseguir?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showConfirmDelete = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
