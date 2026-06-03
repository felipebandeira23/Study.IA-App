package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.Flashcard
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: StudyViewModel,
    deckId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val decks by viewModel.allDecks.collectAsState()
    val deck = remember(deckId, decks) { decks.find { it.id == deckId } }

    var dueCards by remember { mutableStateOf<List<Flashcard>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load only due cards on entry
    LaunchedEffect(deckId) {
        isLoading = true
        dueCards = viewModel.getDueFlashcardsForDeck(deckId)
        isLoading = false
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(deck?.title ?: "Revisão com SRS") },
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
            when {
                isLoading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Carregando flashcards pendentes...")
                    }
                }

                dueCards.isEmpty() -> {
                    // No cards due today
                    NoDueCardsBlock(deckTitle = deck?.title ?: "", onBack = onBack)
                }

                isFinished || currentIndex >= dueCards.size -> {
                    ReviewSummaryBlock(
                        deckTitle = deck?.title ?: "Revisão Concluída",
                        correctCount = correctCount,
                        totalCount = dueCards.size,
                        onFinish = {
                            viewModel.saveSessionStats(deckId, dueCards.size, correctCount)
                            onBack()
                        }
                    )
                }

                else -> {
                    val currentCard = dueCards[currentIndex]

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Progress Indicator
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Card ${currentIndex + 1} de ${dueCards.size}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { currentIndex.toFloat() / dueCards.size },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                            )
                        }

                        // Flashcard
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 32.dp)
                                .clickable { isFlipped = !isFlipped }
                                .testTag("flashcard_body"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isFlipped)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
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

                        // Answer Controls
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
                                Text(
                                    text = "Como foi sua memória?",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                // 3-quality SRS rating buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SrsQualityButton(
                                        label = "Difícil",
                                        sublabel = "Revisar em breve",
                                        quality = 1,
                                        containerColor = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.weight(1f),
                                        testTag = "button_answer_hard"
                                    ) { quality ->
                                        viewModel.reviewCard(currentCard, quality)
                                        if (currentIndex + 1 < dueCards.size) {
                                            currentIndex++; isFlipped = false
                                        } else { isFinished = true }
                                    }
                                    SrsQualityButton(
                                        label = "Bom",
                                        sublabel = "Alguns dias",
                                        quality = 3,
                                        containerColor = BentoPrimary,
                                        modifier = Modifier.weight(1f),
                                        testTag = "button_answer_good"
                                    ) { quality ->
                                        viewModel.reviewCard(currentCard, quality)
                                        correctCount++
                                        if (currentIndex + 1 < dueCards.size) {
                                            currentIndex++; isFlipped = false
                                        } else { isFinished = true }
                                    }
                                    SrsQualityButton(
                                        label = "Fácil",
                                        sublabel = "Mais de 1 semana",
                                        quality = 5,
                                        containerColor = Color(0xFF2E7D32),
                                        modifier = Modifier.weight(1f),
                                        testTag = "button_answer_easy"
                                    ) { quality ->
                                        viewModel.reviewCard(currentCard, quality)
                                        correctCount++
                                        if (currentIndex + 1 < dueCards.size) {
                                            currentIndex++; isFlipped = false
                                        } else { isFinished = true }
                                    }
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
private fun SrsQualityButton(
    label: String,
    sublabel: String,
    quality: Int,
    containerColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: (Int) -> Unit
) {
    Button(
        onClick = { onClick(quality) },
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        modifier = modifier.height(60.dp).testTag(testTag)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(sublabel, fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun NoDueCardsBlock(deckTitle: String, onBack: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BentoPrimaryLight)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎉", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tudo em dia!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = BentoPrimaryDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (deckTitle.isNotBlank()) "Nenhum card do deck \"$deckTitle\" está pendente para hoje."
                       else "Nenhum card pendente para hoje.",
                fontSize = 14.sp,
                color = BentoTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "O algoritmo de repetição espaçada agendou os próximos cards automaticamente. Volte amanhã!",
                fontSize = 12.sp,
                color = BentoTextMuted.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
            ) {
                Text("Voltar aos Decks")
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
        modifier = Modifier.fillMaxWidth().testTag("review_summary_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
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
            Text(text = "Sua Taxa de Fixação:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("review_finish_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Salvar Progresso e Concluir")
            }
        }
    }
}
