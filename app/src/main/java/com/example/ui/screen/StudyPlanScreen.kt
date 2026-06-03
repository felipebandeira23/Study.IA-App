package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
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
import com.example.data.model.StudyPlan
import com.example.data.model.TrackedContest
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlanScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val plans by viewModel.allPlans.collectAsState()
    val contests by viewModel.allContests.collectAsState()
    val planCreationState by viewModel.planState.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Gerar, 1 = Roteiros Ativos
    var titleInput by remember { mutableStateOf("") }
    var levelSelected by remember { mutableStateOf("Iniciante") }
    var selectedContestId by remember { mutableStateOf<Int?>(null) }
    var timelineDays by remember { mutableStateOf("7") } // Duration of plan (e.g. 7 or 15 days)

    // Reset UI State on exit
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetPlanState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plano de Estudos 📅") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("plans_back_button")) {
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
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Gere Estratégia", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_create_plan")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Planos Ativos (${plans.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_active_plans")
                )
            }

            when (activeTab) {
                0 -> {
                    // Plan creation form
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "A Inteligência Artificial analisa seus objetivos para desenhar metas realistas passo a passo:",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = titleInput,
                                onValueChange = { titleInput = it },
                                label = { Text("Foco Principal do Plano (ex: Revisão Matemática, Estudo Geral)") },
                                placeholder = { Text("ex: Direito Penal FCC / Inglês do Zero") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("plan_title_input"),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }

                        // Difficulty Level Row Selector
                        item {
                            Column {
                                Text(
                                    text = "Qual seu nível de familiaridade com a matéria?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Iniciante", "Intermediário", "Avançado").forEach { level ->
                                        val isSelected = levelSelected == level
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { levelSelected = level }
                                                .testTag("level_select_$level"),
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            border = if (isSelected) {
                                                androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                            } else null
                                        ) {
                                            Text(
                                                text = level,
                                                modifier = Modifier.padding(vertical = 12.dp),
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Link to an tracked contest (Optional but highly recommended)
                        item {
                            Column {
                                Text(
                                    text = "Vincular a um Concurso Cadastrado (Opcional):",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                if (contests.isEmpty()) {
                                    Text(
                                        text = "Nenhum edital cadastrado no sistema. Cadastre-os para usar contexto histórico inteligente.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                } else {
                                    // Row layout for contests selector
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // "Sem Edital" Option
                                        Surface(
                                            modifier = Modifier
                                                .clickable { selectedContestId = null }
                                                .testTag("contest_select_none"),
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (selectedContestId == null) {
                                                MaterialTheme.colorScheme.secondaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            border = if (selectedContestId == null) {
                                                androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
                                            } else null
                                        ) {
                                            Text(
                                                text = "Livre (Sem Concurso)",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedContestId == null) {
                                                    MaterialTheme.colorScheme.onSecondaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }

                                        // Saved contests options
                                        contests.forEach { contest ->
                                            val isSelected = selectedContestId == contest.id
                                            Surface(
                                                modifier = Modifier
                                                    .clickable { selectedContestId = contest.id }
                                                    .testTag("contest_select_${contest.id}"),
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.secondaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                },
                                                border = if (isSelected) {
                                                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
                                                } else null
                                            ) {
                                                Text(
                                                    text = contest.name,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = if (isSelected) {
                                                        MaterialTheme.colorScheme.onSecondaryContainer
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Timeline duration select
                        item {
                            Column {
                                Text(
                                    text = "Duração Recomendada do Plano:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    listOf("3" to "Exame Curto (3 dias)", "7" to "Intensivo Sete (7 dias)", "15" to "Cronograma Quinzena (15 dias)").forEach { pair ->
                                        val isSelected = timelineDays == pair.first
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { timelineDays = pair.first }
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { timelineDays = pair.first }
                                            )
                                            Text(pair.second, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    if (titleInput.isNotBlank()) {
                                        viewModel.createStudyPlan(
                                            topic = titleInput,
                                            durationDays = timelineDays.toIntOrNull() ?: 7,
                                            level = levelSelected,
                                            contestId = selectedContestId
                                        )
                                    }
                                },
                                enabled = titleInput.isNotBlank() && planCreationState !is UiState.Loading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("generate_plan_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (planCreationState is UiState.Loading) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Construindo plano personalizado...")
                                } else {
                                    Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gerar Estratégia de Estudos Diária")
                                }
                            }
                        }

                        item {
                            PlanOutputSuccessBanner(
                                state = planCreationState,
                                onAccessPlans = {
                                    titleInput = ""
                                    viewModel.resetPlanState()
                                    activeTab = 1 // Switch to active plans catalogue
                                }
                            )
                        }
                    }
                }
                1 -> {
                    // Catalog list
                    if (plans.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Nenhum plano ativo encontrado.",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Defina suas metas na primeira aba e a IA organizará datas e metas em uma trilha visual.",
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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(plans) { plan ->
                                ActivePlanCard(
                                    plan = plan,
                                    onDelete = { viewModel.deletePlan(plan.id) }
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
fun PlanOutputSuccessBanner(
    state: UiState<String>,
    onAccessPlans: () -> Unit
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
                            text = "Consultando roteiros de editais anteriores e formulando cronograma. Aguarde...",
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
                            text = "Plano Gerado com Êxito!",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onAccessPlans,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("Acessar Meus Roteiros de Trilha")
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
                                text = "Falha ao Estruturar Roteiro",
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
fun ActivePlanCard(
    plan: StudyPlan,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plan_item_${plan.id}"),
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
                    Text(
                        text = plan.topic,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = plan.level,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${plan.durationDays} Dias",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                Row {
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expandir"
                        )
                    }
                    IconButton(onClick = { showConfirmDelete = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Deletar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))
                    Text(
                        text = "Trilha Estratégica Diária:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = plan.planContent,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Excluir Plano?") },
            text = { Text("Isso removerá permanentemente o cronograma '${plan.topic}'. Deseja prosseguir?") },
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
