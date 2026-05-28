package com.example.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            // Addition Form Header
            item {
                Text(
                    text = "Cadastrar ou Acompanhar Edital:",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Os dados descritos aqui servirão de sugestão de contexto histórico para a inteligência artificial desenhar seu plano de estudos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Text fields
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
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Anotações do Edital ou Tópicos importantes") },
                    placeholder = { Text("ex: Focar em Direito Tributário e Contabilidade pública...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("contest_notes_input"),
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
                                editalText = nameInput,
                                notes = notesInput
                            )
                            // Reset input
                            nameInput = ""
                            organizerInput = ""
                            examDateInput = ""
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

            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // List of registered concursos
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
    onDelete: () -> Unit
) {
    var showConfirmDelete by remember { mutableStateOf(false) }

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
                IconButton(onClick = { showConfirmDelete = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir concurso",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Excluir Concurso?") },
            text = { Text("Deseja parar de acompanhar o concurso '${contest.name}'? Isso também o removerá das opções rápidas de planejamento de estudo.") },
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
