package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TrackedContest
import com.example.data.model.StudyPlan
import com.example.ui.viewmodel.StudyViewModel

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: StudyViewModel,
    onNavigateToNotes: () -> Unit,
    onNavigateToDecks: () -> Unit,
    onNavigateToPlans: () -> Unit,
    onNavigateToContests: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    displayName: String = "",
    modifier: Modifier = Modifier
) {
    val notes by viewModel.allNotes.collectAsState()
    val decks by viewModel.allDecks.collectAsState()
    val plans by viewModel.allPlans.collectAsState()
    val contests by viewModel.allContests.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()
    val dailyStreak by viewModel.dailyStreak.collectAsState()

    val totalReviewedCards = sessions.sumOf { it.cardsReviewed }
    val totalCorrectReviews = sessions.sumOf { it.correctAnswers }
    val successRate = if (totalReviewedCards > 0) {
        (totalCorrectReviews.toFloat() / totalReviewedCards * 100).toInt()
    } else 0

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
            // Header Composable matching the Bento HTML design
            item {
                BentoHeader(displayName = displayName, onSettingsClick = onNavigateToSettings)
            }

            // API key notice alert
            item {
                ApiKeyStatusBanner(
                    isAvailable = viewModel.isApiKeyAvailable,
                    onConfigureClick = onNavigateToSettings
                )
            }

            // Large Hero Bento Card: Active Goal or Start Study Plan Campaign block
            item {
                ActiveGoalHeroCard(
                    latestPlan = plans.lastOrNull(),
                    sessionsSize = sessions.size,
                    onClick = onNavigateToPlans
                )
            }

            // 2x2 Bento Stat block representing different categories & colors
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
                            value = "$dailyStreak ${if (dailyStreak == 1) "dia" else "dias"}",
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
            item {
                TextButton(
                    onClick = onNavigateToAnalytics,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nav_analytics_button")
                ) {
                    Text(
                        "Ver Análise Completa →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )
                }
            }

            // Section Header: Ferramentas de Estudo
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

            // Tool Interactive Bento Block 1: Resumos Inteligentes (Primary Solid theme)
            item {
                BentoInteractiveToolCard(
                    title = "Resumos Inteligentes",
                    description = "Gere explicações, conceitos complexos e sínteses com apoio acadêmico inteligente.",
                    icon = Icons.Default.StickyNote2,
                    containerColor = BentoPrimary,
                    contentColor = Color.White,
                    iconContainerColor = Color.White.copy(alpha = 0.2f),
                    arrowBgColor = Color.White,
                    arrowColor = BentoPrimary,
                    onClick = onNavigateToNotes,
                    testTag = "nav_notes_card"
                )
            }

            // Tool Interactive Bento Block 2: Flashcards (Lavender theme)
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

            // Tool Interactive Bento Block 3: Planos de Estudo (Pink theme)
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

            // Section: Concursos Rastreados
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
                                text = "Ao cadastrar um edital, os planos de estudo automáticos usarão os tópicos dele para acelerar suas metas com precisão.",
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

            item {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

@Composable
fun BentoHeader(displayName: String = "", onSettingsClick: () -> Unit = {}) {
    val initials = remember(displayName) {
        val clean = displayName.trim()
        when {
            clean.length >= 2 -> clean.take(2).uppercase()
            clean.length == 1 -> clean.uppercase()
            else -> "FB"
        }
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(BentoGrayishViolet, RoundedCornerShape(12.dp))
                    .testTag("nav_settings_button")
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Configurações",
                    tint = BentoPrimaryDark,
                    modifier = Modifier.size(18.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(BentoPrimaryLight)
                    .border(
                        width = 1.dp,
                        color = BentoMediumLavender,
                        shape = RoundedCornerShape(22.dp)
                    ),
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
}

@Composable
fun ApiKeyStatusBanner(isAvailable: Boolean, onConfigureClick: () -> Unit = {}) {
    if (!isAvailable) {
        Surface(
            color = BentoPink,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Chave indisponível",
                        tint = BentoPrimaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Modo Educativo Local Ativo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = BentoPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Nenhuma chave API configurada. Os módulos usarão geradores locais de modo off-line.",
                        fontSize = 11.sp,
                        color = BentoPrimaryDark.copy(alpha = 0.75f),
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        onClick = onConfigureClick,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(
                            text = "Configurar Chave API →",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BentoPrimaryDark
                        )
                    }
                }
            }
        }
    } else {
        Surface(
            color = Color(0xFFE8F5E9),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = "Chave ativa",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "IA Conectada em Nuvem",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Modelos Gemini API ativos para sintetizar resumos e decks avançados.",
                        fontSize = 11.sp,
                        color = Color(0xFF1B5E20).copy(alpha = 0.75f),
                        lineHeight = 15.sp
                    )
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("bento_hero_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = BentoMediumLavender
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
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
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = BentoPrimaryDark,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        letterSpacing = 1.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (latestPlan != null) {
                Text(
                    text = latestPlan.topic,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = BentoPrimaryDark,
                    lineHeight = 30.sp,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.35f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressPercent / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(BentoPrimaryDark)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$progressPercent% Progresso",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BentoPrimaryDark
                    )
                    Text(
                        text = "Foco: ${latestPlan.level}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BentoPrimaryDark
                    )
                }
            } else {
                Text(
                    text = "Monte seu Cronograma de Estudos Diário com IA",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = BentoPrimaryDark,
                    lineHeight = 28.sp,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "A inteligência artificial analisa o edital e formata o conteúdo perfeito dividindo seus objetivos dia a dia.",
                    fontSize = 13.sp,
                    color = BentoPrimaryDark.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "Toque para configurar seu plano personalizado  →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = BentoPrimaryDark
                    )
                }
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
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        ),
        elevation = if (borderColor != null) CardDefaults.elevatedCardElevation(defaultElevation = 0.dp) else CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        val maybeBorder = if (borderColor != null) {
            Modifier.border(1.dp, borderColor, RoundedCornerShape(24.dp))
        } else {
            Modifier
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(maybeBorder)
                .padding(18.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = contentColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = contentColor.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(arrowBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "→",
                    color = arrowColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TrackedContestItem(contest: TrackedContest) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
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
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BentoPrimaryLight.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📌", fontSize = 18.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contest.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = BentoPrimaryDark
                        )
                        if (contest.organizer.isNotBlank()) {
                            Text(
                                text = "Banca: ${contest.organizer}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                if (contest.examDate.isNotBlank()) {
                    Surface(
                        color = BentoPrimaryLight,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Prova: ${contest.examDate}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontWeight = FontWeight.ExtraBold,
                            color = BentoPrimaryDark
                        )
                    }
                }
            }
            if (contest.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = BentoBorder.copy(alpha = 0.3f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = contest.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoTextMuted,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
