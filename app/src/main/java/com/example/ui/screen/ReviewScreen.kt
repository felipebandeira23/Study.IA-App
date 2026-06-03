package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Deck
import com.example.data.model.Flashcard
import com.example.ui.viewmodel.StudyViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: StudyViewModel,
    deckId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val decks by viewModel.allDecks.collectAsState()
    val deck = remember(deckId, decks) {
        if (deckId == -1) null else decks.find { it.id == deckId }
    }
    val isAllDecksMode = deckId == -1

    // Read flashcards reactively — deckId == -1 loads all cards from all decks
    val cardsFlow = remember(deckId) {
        if (isAllDecksMode) viewModel.allFlashcards
        else viewModel.getFlashcardsForDeck(deckId)
    }
    val cards by cardsFlow.collectAsState(initial = emptyList())

    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAllDecksMode) "Revisão Geral 🎯" else deck?.title ?: "Jogo de Revisão") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("review_close_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (cards.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Carregando seus flashcards de revisão...")
                }
            } else if (isFinished || currentIndex >= cards.size) {
                // Summary Screen
                ReviewSummaryBlock(
                    deckTitle = if (isAllDecksMode) "Revisão Geral — Todos os Decks" else deck?.title ?: "Revisão Concluída",
                    correctCount = correctCount,
                    totalCount = cards.size,
                    onFinish = {
                        viewModel.saveSessionStats(
                            deckId = if (isAllDecksMode) 0 else deckId,
                            cardsReviewed = cards.size,
                            correctAnswers = correctCount
                        )
                        onBack()
                    }
                )
            } else {
                val currentCard = cards[currentIndex]

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Progress Indicator
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Card ${currentIndex + 1} de ${cards.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (currentIndex.toFloat()) / cards.size },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        )
                    }

                    // Tactile Flashcard Card
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 32.dp)
                            .clickable { isFlipped = !isFlipped }
                            .testTag("flashcard_body"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFlipped) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            }
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cached,
                                    contentDescription = "Toque para virar",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (isFlipped) "Resposta" else "Pergunta",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (isFlipped) currentCard.back else currentCard.front,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 28.sp
                                )
                            }
                        }
                    }

                    // Bottom Answer Controls
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!isFlipped) {
                            Text(
                                text = "💡 Toque no card para revelar a resposta",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Errei (Incorrect) Button
                                Button(
                                    onClick = {
                                        if (currentIndex + 1 < cards.size) {
                                            currentIndex++
                                            isFlipped = false
                                        } else {
                                            isFinished = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("button_answer_wrong")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Não sei")
                                }

                                // Acertei (Correct) Button
                                Button(
                                    onClick = {
                                        correctCount++
                                        if (currentIndex + 1 < cards.size) {
                                            currentIndex++
                                            isFlipped = false
                                        } else {
                                            isFinished = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("button_answer_correct")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Eu sabia")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewSummaryBlock(
    deckTitle: String,
    correctCount: Int,
    totalCount: Int,
    onFinish: () -> Unit
) {
    val rate = if (totalCount > 0) (correctCount.toFloat() / totalCount * 100).toInt() else 0
    val feedbackText = when {
        rate >= 90 -> "Desempenho Excelente! Memória dominada! 🌟"
        rate >= 70 -> "Bom trabalho! Continue assim para fixar totalmente! 👍"
        else -> "Bons estudos! Que tal revisar este deck novamente para fixar? 📚"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("review_summary_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "✅ Revisão Concluída!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = deckTitle,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Sua Taxa de Fixação:",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$rate%",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Você acertou $correctCount de $totalCount cards",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = feedbackText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("review_finish_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Salvar Progresso e Concluir")
            }
        }
    }
}
