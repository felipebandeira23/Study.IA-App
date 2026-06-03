package com.example.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.allSessions.collectAsState()
    val dailyStreak by viewModel.dailyStreak.collectAsState()
    val deckStats by viewModel.deckStats.collectAsState()
    val weeklyActivity by viewModel.weeklyActivity.collectAsState()
    val accuracyHistory by viewModel.sessionAccuracyHistory.collectAsState()

    val totalReviews = sessions.sumOf { it.cardsReviewed }
    val totalSessions = sessions.size
    val overallAccuracy = if (totalReviews > 0) {
        sessions.sumOf { it.correctAnswers } * 100 / totalReviews
    } else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Análise e Progresso", fontWeight = FontWeight.Black, color = BentoPrimaryDark) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("analytics_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BentoBg)
            )
        },
        containerColor = BentoBg,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // KPI grid
            item {
                AnalyticsSectionHeader(emoji = "📊", title = "Visão Geral")
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiTile("Total de Revisões", totalReviews.toString(), BentoSoftViolet, modifier = Modifier.weight(1f))
                    KpiTile("Sessões", totalSessions.toString(), BentoSoftBlue, modifier = Modifier.weight(1f))
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiTile("Precisão Geral", "$overallAccuracy%", BentoSoftGreen, modifier = Modifier.weight(1f))
                    KpiTile("Streak Atual", "$dailyStreak dias", BentoPink, modifier = Modifier.weight(1f))
                }
            }

            // Accuracy history bar chart
            if (accuracyHistory.isNotEmpty()) {
                item {
                    AnalyticsSectionHeader(emoji = "📈", title = "Histórico de Precisão (últimas sessões)")
                }
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AccuracyBarChart(
                            data = accuracyHistory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(16.dp)
                        )
                    }
                }
            }

            // Activity heat-map (28 days)
            if (weeklyActivity.isNotEmpty()) {
                item {
                    AnalyticsSectionHeader(emoji = "🗓", title = "Atividade (últimos 28 dias)")
                }
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    ) {
                        ActivityHeatmap(
                            activity = weeklyActivity,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }

            // Per-deck stats
            if (deckStats.isNotEmpty()) {
                item {
                    AnalyticsSectionHeader(emoji = "🃏", title = "Estatísticas por Deck")
                }
                items(deckStats) { stat ->
                    DeckStatRow(stat)
                }
            }

            if (sessions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📊", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Nenhuma sessão de revisão ainda",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = BentoTextDark
                            )
                            Text(
                                "Revise seus flashcards para ver estatísticas aqui.",
                                fontSize = 13.sp,
                                color = BentoTextMuted
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun KpiTile(label: String, value: String, bgColor: Color, modifier: Modifier = Modifier) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = BentoPrimaryDark)
            Text(label, fontSize = 12.sp, color = BentoTextMuted, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun AccuracyBarChart(data: List<Pair<Long, Int>>, modifier: Modifier = Modifier) {
    val barColor = BentoPrimary
    val labelColor = BentoTextMuted
    val dateFormat = SimpleDateFormat("d/M", Locale.getDefault())

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        val barCount = data.size
        val maxVal = 100f
        val barWidth = (size.width - (barCount - 1) * 4.dp.toPx()) / barCount
        val chartHeight = size.height - 20.dp.toPx()

        data.forEachIndexed { i, (_, accuracy) ->
            val barHeight = (accuracy / maxVal) * chartHeight
            val x = i * (barWidth + 4.dp.toPx())
            val y = chartHeight - barHeight
            drawRoundRect(
                color = barColor.copy(alpha = 0.85f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
        // Baseline
        drawLine(
            color = labelColor,
            start = Offset(0f, chartHeight),
            end = Offset(size.width, chartHeight),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
private fun ActivityHeatmap(activity: Map<Long, Int>, modifier: Modifier = Modifier) {
    val today = run {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    val days = (27 downTo 0).map { offset -> today - TimeUnit.DAYS.toMillis(offset.toLong()) }
    val maxActivity = activity.values.maxOrNull()?.takeIf { it > 0 } ?: 1

    Column(modifier = modifier) {
        val rows = days.chunked(7)
        rows.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                week.forEach { day ->
                    val count = activity[day] ?: 0
                    val alpha = if (count == 0) 0.08f else 0.2f + (count.toFloat() / maxActivity) * 0.8f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(BentoPrimary.copy(alpha = alpha))
                    )
                }
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("28 dias atrás", fontSize = 10.sp, color = BentoTextMuted)
            Text("Hoje", fontSize = 10.sp, color = BentoTextMuted)
        }
    }
}

@Composable
private fun DeckStatRow(stat: StudyViewModel.DeckStats) {
    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stat.deckName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BentoTextDark)
                Text(
                    "Última revisão: ${dateFormat.format(Date(stat.lastReviewAt))}",
                    fontSize = 11.sp,
                    color = BentoTextMuted
                )
            }
            Surface(
                color = BentoSoftViolet,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "${stat.totalReviews} revisões",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoPrimaryDark,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsSectionHeader(emoji: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    ) {
        Text(emoji, fontSize = 18.sp)
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = BentoPrimaryDark,
            letterSpacing = (-0.3).sp
        )
    }
}
