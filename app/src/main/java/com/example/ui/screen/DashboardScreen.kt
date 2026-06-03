package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TrackedContest
import com.example.data.model.StudyPlan
import com.example.ui.viewmodel.StudyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

// Bento styling imports
import com.example.ui.theme.BentoBg
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDark
import com.example.ui.theme.BentoPrimaryLight
import com.example.ui.theme.BentoMediumLavender
import com.example.ui.theme.BentoGrayishViolet
import com.example.ui.theme.BentoSoftViolet
import com.example.ui.theme.BentoPink
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoTextDark
import com.example.ui.theme.BentoTextMuted

/** Returns days until the examDate string ("dd/MM/yyyy"), or null if past/unparseable. */
private fun daysUntilExam(examDateStr: String): Long? {
    if (examDateStr.isBlank()) return null
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
        val target = sdf.parse(examDateStr) ?: return null
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val diff = target.time - todayStart
        if (diff < 0) null else TimeUnit.MILLISECONDS.toDays(diff)
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: StudyViewModel,
    onNavigateToNotes: () -> Unit,
    onNavigateToDecks: () -> Unit,
    onNavigateToPlans: () -> Unit,
    onNavigateToContests: () -> Unit,
    onNavigateToReviewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.allNotes.collectAsState()
    val decks by viewModel.allDecks.collectAsState()
    val plans by viewModel.allPlans.collectAsState()
    val contests by viewModel.allContests.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()
    val allFlashcards by viewModel.allFlashcards.collectAsState()
    val cardsToday by viewModel.cardsReviewedToday.collectAsState()
    val dailyGoal by viewModel.dailyCardGoal.collectAsState()

    val totalReviewedCards = sessions.sumOf { it.cardsReviewed }
    val totalCorrectReviews = sessions.sumOf { it.correctAnswers }
    val successRate = if (totalReviewedCards > 0) {
        (totalCorrectReviews.toFloat() / totalReviewedCards * 100).toInt()
    } else 0

    // Find nearest upcoming contest
    val nearestContest = remember(contests) {
        contests
            .mapNotNull { c -> daysUntilExam(c.examDate)?.let { days -> Pair(c, days) } }
            .minByOrNull { it.second }
    }

    var showDailyGoalDialog by remember { mutableStateOf(false) }
    var dailyGoalInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBg),
        containerColor = BentoBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                BentoHeader(email = "felipe.bandeira1@gmail.com")
            }

            // Countdown banner for nearest upcoming contest
            if (nearestContest != null) {
                item {
                    ContestCountdownBanner(
                        contestName = nearestContest.first.name,
                        daysLeft = nearestContest.second
                    )
                }
            }

            item {
                ApiKeyStatusBanner(isAvailable = viewModel.isApiKeyAvailable)
            }

            item {
                ActiveGoalHeroCard(
                    latestPlan = plans.lastOrNull(),
                    sessionsSize = sessions.size,
                    onClick = onNavigateToPlans
                )
            }

            // Stat tiles 2x2
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BentoStatTile(
                            emoji = "📚",
                            value = totalReviewedCards.toString(),
                            label = "Cards Fixados",
                            containerColor = BentoSoftViolet,
                            textColor = BentoPrimaryDark,
                            modifier = Modifier.weight(1f)
                        )
                        BentoStatTile(
                            emoji = "🔥",
                            value = "${sessions.size} dias",
                            label = "Daily Streak",
                            containerColor = BentoGrayishViolet,
                            textColor = BentoPrimaryDark,
                            borderColor = BentoBorder,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BentoStatTile(
                            emoji = "🎯",
                            value = if (totalReviewedCards > 0) "$successRate%" else "--",
                            label = "Fixação Geral",
                            containerColor = BentoPink,
                            textColor = BentoPrimaryDark,
                            modifier = Modifier.weight(1f)
                        )
                        BentoStatTile(
                            emoji = "🗒️",
                            value = notes.size.toString(),
                            label = "Resumos IA",
                            containerColor = BentoPrimaryLight,
                            textColor = BentoPrimaryDark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Daily goal ring + Revisar Agora
            item {
                DailyGoalReviewCard(
                    cardsToday = cardsToday,
                    dailyGoal = dailyGoal,
                    totalCards = allFlashcards.size,
                    onReviewAll = {
                        if (allFlashcards.isNotEmpty()) onNavigateToReviewAll()
                    },
                    onSetGoal = {
                        dailyGoalInput = dailyGoal.toString()
                        showDailyGoalDialog = true
                    }
                )
            }

            item {
                Text(
                    text = "Acelere com Inteligência Artificial",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = BentoPrimaryDark,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                BentoInteractiveToolCard(
                    title = "Resumos Inteligentes",
                    description = "Gere explicações, conceitos complexos e sínteses com apoio acadêmico inteligente.",
                    icon = Icons.AutoMirrored.Filled.StickyNote2,
                    containerColor = BentoPrimary,
                    contentColor = Color.White,
                    iconContainerColor = Color.White.copy(alpha = 0.2f),
                    arrowBgColor = Color.White,
                    arrowColor = BentoPrimary,
                    onClick = onNavigateToNotes,
                    testTag = "nav_notes_card"
                )
            }

            item {
                BentoInteractiveToolCard(
                    title = "Flashcards Gratificantes",
                    description = "Decks automáticos formulados pela IA para exercícios diários de recordação ativa.",
                    icon = Icons.Default.QuestionAnswer,
                    containerColor = BentoSoftViolet,
                    contentColor = BentoPrimaryDark,
                    iconContainerColor = BentoPrimaryDark.copy(alpha = 0.12f),
                    arrowBgColor = BentoPrimaryDark,
                    arrowColor = Color.White,
                    onClick = onNavigateToDecks,
                    testTag = "nav_flashcards_card"
                )
            }

            item {
                BentoInteractiveToolCard(
                    title = "Plano Dirigido de Estudos",
                    description = "Consolide rotas para os seus editais com cronogramas diários adaptados.",
                    icon = Icons.Default.CalendarToday,
                    containerColor = BentoPink,
                    contentColor = BentoPrimaryDark,
                    iconContainerColor = BentoPrimaryDark.copy(alpha = 0.12f),
                    arrowBgColor = BentoPrimaryDark,
                    arrowColor = Color.White,
                    onClick = onNavigateToPlans,
                    testTag = "nav_plans_card"
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📌 Editais Rastreados",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = BentoPrimaryDark,
                        letterSpacing = (-0.5).sp
                    )
                    IconButton(
                        onClick = onNavigateToContests,
                        modifier = Modifier
                            .testTag("nav_contests_icon")
                            .background(BentoPrimaryLight, RoundedCornerShape(12.dp))
                            .size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Adicionar concurso",
                            tint = BentoPrimaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (contests.isEmpty()) {
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(BentoPrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = BentoPrimaryDark,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Nenhum concurso ou edital acompanhado no momento.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = BentoPrimaryDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ao cadastrar um edital, os planos de estudo automáticos usarão os tópicos dele para acelerar suas metas.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = BentoTextMuted,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToContests,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BentoPrimary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .testTag("nav_contests_empty_button")
                                    .height(44.dp)
                            ) {
                                Text("Cadastrar Edital no Painel")
                            }
                        }
                    }
                }
            } else {
                items(contests) { contest ->
                    TrackedContestItem(contest = contest)
                }
            }

            item { Spacer(modifier = Modifier.height(36.dp)) }
        }
    }

    // Daily goal dialog
    if (showDailyGoalDialog) {
        AlertDialog(
            onDismissRequest = { showDailyGoalDialog = false },
            title = { Text("Meta Diária de Cards", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Quantos cards você quer revisar por dia?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = dailyGoalInput,
                        onValueChange = { dailyGoalInput = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Meta (ex: 20)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val goal = dailyGoalInput.toIntOrNull()
                    if (goal != null && goal > 0) {
                        viewModel.setDailyCardGoal(goal)
                        showDailyGoalDialog = false
                    }
                }) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { showDailyGoalDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun ContestCountdownBanner(
    contestName: String,
    daysLeft: Long
) {
    val (bgColor, textColor, emoji) = when {
        daysLeft <= 7 -> Triple(Color(0xFFFFEBEE), Color(0xFFB71C1C), "🚨")
        daysLeft <= 30 -> Triple(BentoPink, BentoPrimaryDark, "⏰")
        else -> Triple(BentoPrimaryLight, BentoPrimaryDark, "🎯")
    }
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(emoji, fontSize = 22.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (daysLeft == 0L) "Prova HOJE!" else "Faltam $daysLeft ${if (daysLeft == 1L) "dia" else "dias"}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
                Text(
                    text = contestName,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.75f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun DailyGoalReviewCard(
    cardsToday: Int,
    dailyGoal: Int,
    totalCards: Int,
    onReviewAll: () -> Unit,
    onSetGoal: () -> Unit
) {
    val progress = if (dailyGoal > 0) (cardsToday.toFloat() / dailyGoal).coerceIn(0f, 1f) else 0f
    val goalReached = cardsToday >= dailyGoal

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Ring progress
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clickable(onClick = onSetGoal),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 7.dp,
                    color = if (goalReached) Color(0xFF2E7D32) else BentoPrimary,
                    trackColor = BentoPrimaryLight,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$cardsToday",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = BentoPrimaryDark
                    )
                    Text(
                        text = "/$dailyGoal",
                        fontSize = 10.sp,
                        color = BentoTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info + Revisar Agora button
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (goalReached) "Meta diária atingida! 🎉" else "Meta diária de revisão",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = BentoPrimaryDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (totalCards == 0) "Crie flashcards para começar" else "$totalCards cards disponíveis",
                    fontSize = 11.sp,
                    color = BentoTextMuted
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onReviewAll,
                    enabled = totalCards > 0,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BentoPrimary,
                        contentColor = Color.White,
                        disabledContainerColor = BentoPrimaryLight
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp).testTag("review_all_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Revisar Agora", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Tap ring hint
            IconButton(onClick = onSetGoal) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar meta",
                    tint = BentoTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun BentoHeader(email: String) {
    val initials = remember(email) {
        val clean = email.split("@").firstOrNull() ?: "FB"
        if (clean.length >= 2) clean.take(2).uppercase() else "FB"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Study.ia 🎓",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = BentoPrimary,
                letterSpacing = (-0.75).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "PERSONAL LEARNING PATH",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BentoTextMuted,
                letterSpacing = 1.5.sp
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(BentoPrimaryLight)
                .border(1.dp, BentoMediumLavender, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontWeight = FontWeight.Black,
                color = BentoPrimaryDark,
                fontSize = 15.sp,
                letterSpacing = (-0.25).sp
            )
        }
    }
}

@Composable
fun ApiKeyStatusBanner(isAvailable: Boolean) {
    if (!isAvailable) {
        Surface(
            color = BentoPink,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Info, "Chave indisponível", tint = BentoPrimaryDark, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Modo Educativo Local Ativo", fontSize = 14.sp, fontWeight = FontWeight.Black, color = BentoPrimaryDark)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Nenhuma chave API configurada. Os módulos usarão geradores de síntese locais offline.", fontSize = 11.sp, color = BentoPrimaryDark.copy(alpha = 0.75f), lineHeight = 15.sp)
                }
            }
        }
    } else {
        Surface(
            color = Color(0xFFE8F5E9),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CloudQueue, "Chave ativa", tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("IA Conectada em Nuvem", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF1B5E20))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Modelos Gemini API ativos para sintetizar resumos e decks avançados.", fontSize = 11.sp, color = Color(0xFF1B5E20).copy(alpha = 0.75f), lineHeight = 15.sp)
                }
            }
        }
    }
}

@Composable
fun ActiveGoalHeroCard(
    latestPlan: StudyPlan?,
    sessionsSize: Int,
    onClick: () -> Unit
) {
    val progressPercent = remember(sessionsSize) { (40 + (sessionsSize * 15)).coerceIn(40, 95) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag("bento_hero_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = BentoMediumLavender),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(
                        text = if (latestPlan != null) "META DE ESTUDO ATIVA" else "COMECE SUA META",
                        fontSize = 9.sp, fontWeight = FontWeight.Black, color = BentoPrimaryDark,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        letterSpacing = 1.sp
                    )
                }
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) { Text("⚡", fontSize = 18.sp) }
            }
            Spacer(modifier = Modifier.height(28.dp))
            if (latestPlan != null) {
                Text(latestPlan.topic, fontSize = 24.sp, fontWeight = FontWeight.Black, color = BentoPrimaryDark, lineHeight = 30.sp, letterSpacing = (-0.5).sp)
                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.35f))) {
                    Box(modifier = Modifier.fillMaxWidth(progressPercent / 100f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(BentoPrimaryDark))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("$progressPercent% Progresso", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = BentoPrimaryDark)
                    Text("Foco: ${latestPlan.level}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = BentoPrimaryDark)
                }
            } else {
                Text("Monte seu Cronograma de Estudos Diário com IA", fontSize = 22.sp, fontWeight = FontWeight.Black, color = BentoPrimaryDark, lineHeight = 28.sp, letterSpacing = (-0.5).sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("A inteligência artificial analisa o edital e formata o conteúdo perfeito dividindo seus objetivos dia a dia.", fontSize = 13.sp, color = BentoPrimaryDark.copy(alpha = 0.8f), lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(18.dp))
                Text("Toque para configurar seu plano personalizado  →", fontSize = 13.sp, fontWeight = FontWeight.Black, color = BentoPrimaryDark)
            }
        }
    }
}

@Composable
fun BentoStatTile(
    emoji: String,
    value: String,
    label: String,
    containerColor: Color,
    textColor: Color = BentoPrimaryDark,
    borderColor: Color? = null,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = if (borderColor != null) CardDefaults.elevatedCardElevation(defaultElevation = 0.dp) else CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        val maybeBorder = if (borderColor != null) Modifier.border(1.dp, borderColor, RoundedCornerShape(24.dp)) else Modifier
        Column(
            modifier = Modifier.fillMaxWidth().then(maybeBorder).padding(18.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) { Text(emoji, fontSize = 20.sp) }
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = textColor, letterSpacing = (-0.5).sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = textColor.copy(alpha = 0.6f), letterSpacing = 1.sp)
        }
    }
}

@Composable
fun BentoInteractiveToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    iconContainerColor: Color,
    arrowBgColor: Color,
    arrowColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag(testTag),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(iconContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Black, color = contentColor)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(description, fontSize = 12.sp, color = contentColor.copy(alpha = 0.8f), lineHeight = 16.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(arrowBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text("→", color = arrowColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TrackedContestItem(contest: TrackedContest) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(BentoPrimaryLight.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) { Text("📌", fontSize = 18.sp) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(contest.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = BentoPrimaryDark)
                        if (contest.organizer.isNotBlank()) {
                            Text("Banca: ${contest.organizer}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
                if (contest.examDate.isNotBlank()) {
                    Surface(color = BentoPrimaryLight, shape = RoundedCornerShape(12.dp)) {
                        Text("Prova: ${contest.examDate}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontWeight = FontWeight.ExtraBold, color = BentoPrimaryDark)
                    }
                }
            }
            if (contest.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BentoBorder.copy(alpha = 0.3f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(contest.notes, style = MaterialTheme.typography.bodyMedium, color = BentoTextMuted, lineHeight = 20.sp)
            }
        }
    }
}
