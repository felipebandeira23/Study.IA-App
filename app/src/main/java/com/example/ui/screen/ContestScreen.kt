package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EditalTopic
import com.example.data.model.TrackedContest
import com.example.ui.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contests by viewModel.allContests.collectAsState()

    var nameInput by remember { mutableStateOf("") }
    var organizerInput by remember { mutableStateOf("") }
    var examDateInput by remember { mutableStateOf("") }
    var editalInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapear Concursos 📌") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("contests_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Cadastrar ou Acompanhar Edital:",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Os dados descritos aqui servem de contexto para a IA gerar seu plano de estudos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nome do Concurso ou Cargo desejado") },
                    placeholder = { Text("ex: Auditor Federal TCU / Analista Serpro") },
                    modifier = Modifier.fillMaxWidth().testTag("contest_name_input"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = organizerInput,
                        onValueChange = { organizerInput = it },
                        label = { Text("Banca Organizadora") },
                        placeholder = { Text("ex: FGV / Cebraspe") },
                        modifier = Modifier.weight(1f).testTag("contest_organizer_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = examDateInput,
                        onValueChange = { examDateInput = it },
                        label = { Text("Data Prevista") },
                        placeholder = { Text("ex: 22/11/2026") },
                        modifier = Modifier.weight(1f).testTag("contest_date_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = editalInput,
                    onValueChange = { editalInput = it },
                    label = { Text("Conteúdo do Edital (opcional)") },
                    placeholder = { Text("Cole aqui o conteúdo programático ou tópicos do edital...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Anotações adicionais") },
                    placeholder = { Text("ex: Focar em Direito Tributário e Contabilidade pública...") },
                    modifier = Modifier.fillMaxWidth().height(80.dp).testTag("contest_notes_input"),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            viewModel.addTrackedContest(
                                name = nameInput,
                                organizer = organizerInput,
                                examDate = examDateInput,
                                editalText = editalInput,
                                notes = notesInput
                            )
                            nameInput = ""
                            organizerInput = ""
                            examDateInput = ""
                            editalInput = ""
                            notesInput = ""
                        }
                    },
                    enabled = nameInput.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("contest_save_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar e Acompanhar Concurso")
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                Text(
                    text = "Seus Concursos Registrados (${contests.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (contests.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nenhum edital cadastrado.",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(contests) { contest ->
                    TrackedContestListItem(
                        contest = contest,
                        viewModel = viewModel,
                        onDelete = { viewModel.deleteTrackedContest(contest.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TrackedContestListItem(
    contest: TrackedContest,
    viewModel: StudyViewModel,
    onDelete: () -> Unit
) {
    var showConfirmDelete by remember { mutableStateOf(false) }
    var showTopics by remember { mutableStateOf(false) }
    var newTopicInput by remember { mutableStateOf("") }

    val topics by viewModel.getEditalTopicsForContest(contest.id).collectAsState(initial = emptyList())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("contest_item_${contest.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contest.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (contest.organizer.isNotBlank()) {
                        Text(
                            text = "Banca: ${contest.organizer}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (contest.examDate.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Prova em: ${contest.examDate}",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                Row {
                    IconButton(onClick = { showTopics = !showTopics }) {
                        Icon(
                            imageVector = if (showTopics) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Ver tópicos",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showConfirmDelete = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir concurso", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (contest.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = contest.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Edital topics checklist (collapsible)
            AnimatedVisibility(
                visible = showTopics,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📋 Tópicos do Edital (${topics.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val dominados = topics.count { it.status == 2 }
                        if (topics.isNotEmpty()) {
                            Text(
                                text = "$dominados/${topics.size} dominados",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (topics.isEmpty()) {
                        Text(
                            text = "Nenhum tópico adicionado. Adicione abaixo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                    } else {
                        topics.forEach { topic ->
                            EditalTopicRow(
                                topic = topic,
                                onStatusChange = { newStatus ->
                                    viewModel.updateEditalTopicStatus(topic.id, newStatus)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Add topic row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTopicInput,
                            onValueChange = { newTopicInput = it },
                            placeholder = { Text("Novo tópico (ex: Direito Constitucional)", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        IconButton(
                            onClick = {
                                if (newTopicInput.isNotBlank()) {
                                    viewModel.addEditalTopic(contest.id, newTopicInput.trim())
                                    newTopicInput = ""
                                }
                            },
                            enabled = newTopicInput.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar tópico", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Excluir Concurso?") },
            text = { Text("Deseja parar de acompanhar o concurso '${contest.name}'? Os tópicos do edital também serão removidos.") },
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
                TextButton(onClick = { showConfirmDelete = false }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditalTopicRow(
    topic: EditalTopic,
    onStatusChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status indicator dot
        val dotColor = when (topic.status) {
            0 -> Color(0xFFBDBDBD) // Pendente — grey
            1 -> Color(0xFFFFA726) // Em revisão — orange
            2 -> Color(0xFF4CAF50) // Dominado — green
            else -> Color(0xFFBDBDBD)
        }
        Surface(
            modifier = Modifier.size(10.dp),
            shape = RoundedCornerShape(5.dp),
            color = dotColor
        ) {}

        Text(
            text = topic.title,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            color = if (topic.status == 2) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.onSurface
        )

        // Compact status selector
        val statusLabels = listOf("Pendente", "Revisando", "Dominado")
        val statusColors = listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            Color(0xFFFFF3E0),
            Color(0xFFE8F5E9)
        )
        val statusTextColors = listOf(
            MaterialTheme.colorScheme.onSurfaceVariant,
            Color(0xFFE65100),
            Color(0xFF1B5E20)
        )

        Surface(
            color = statusColors[topic.status.coerceIn(0, 2)],
            shape = RoundedCornerShape(6.dp),
            onClick = { onStatusChange((topic.status + 1) % 3) }
        ) {
            Text(
                text = statusLabels[topic.status.coerceIn(0, 2)],
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = statusTextColors[topic.status.coerceIn(0, 2)],
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
