package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyNote
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.allNotes.collectAsState()
    val summaryState by viewModel.summaryState.collectAsState()
    val fromNoteState by viewModel.fromNoteFlashcardState.collectAsState()
    val fromNoteNoteId by viewModel.fromNoteFlashcardNoteId.collectAsState()
    val qaState by viewModel.qaState.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) }
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    var selectedNote by remember { mutableStateOf<StudyNote?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when flashcards from a note are successfully created
    LaunchedEffect(fromNoteState) {
        if (fromNoteState is UiState.Success) {
            snackbarHostState.showSnackbar("✅ Deck criado! Acesse em Flashcards.")
            viewModel.resetFromNoteFlashcardState()
        } else if (fromNoteState is UiState.Error) {
            snackbarHostState.showSnackbar("❌ ${(fromNoteState as UiState.Error).message}")
            viewModel.resetFromNoteFlashcardState()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetSummaryState()
            viewModel.resetQaState()
            viewModel.resetFromNoteFlashcardState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumos Inteligentes 📝") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("notes_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    text = { Text("Gere Resumo", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_generate_note")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Resumos Salvos (${notes.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_saved_notes")
                )
            }

            when (activeTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Gere resumos por inteligência artificial em segundos:",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = titleInput,
                                onValueChange = { titleInput = it },
                                label = { Text("Tema do Resumo (ex: Direito Administrativo, Kotlin)") },
                                placeholder = { Text("Digite o assunto principal...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("note_title_input"),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = contentInput,
                                onValueChange = { contentInput = it },
                                label = { Text("Texto base para resumir (Opcional)") },
                                placeholder = { Text("Cole artigos, anotações de aula, ou deixe em branco...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .testTag("note_content_input"),
                                shape = RoundedCornerShape(8.dp),
                                maxLines = 15
                            )
                        }

                        item {
                            Button(
                                onClick = {
                                    if (titleInput.isNotBlank()) {
                                        viewModel.createSummary(titleInput, contentInput)
                                    }
                                },
                                enabled = titleInput.isNotBlank() && summaryState !is UiState.Loading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("generate_note_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (summaryState is UiState.Loading) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Analisando e gerando resumo...")
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gerar Resumo Completo")
                                }
                            }
                        }

                        item {
                            SummaryOutputBlock(
                                state = summaryState,
                                onSaveSuccess = {
                                    titleInput = ""
                                    contentInput = ""
                                    activeTab = 1
                                    viewModel.resetSummaryState()
                                }
                            )
                        }
                    }
                }
                1 -> {
                    if (notes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.StickyNote2,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Nenhum resumo salvo ainda.",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Preencha o formulário anterior e use o Gemini para criar seus primeiros resumos.",
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
                            items(notes) { note ->
                                SavedNoteCard(
                                    note = note,
                                    onClick = { selectedNote = note },
                                    onDelete = { viewModel.deleteNote(note) },
                                    onCreateFlashcards = { viewModel.generateFlashcardsFromNote(note) },
                                    isCreatingFlashcards = fromNoteNoteId == note.id && fromNoteState is UiState.Loading
                                )
                            }
                        }
                    }
                }
            }
        }

        // Note detail dialog with Q&A
        selectedNote?.let { note ->
            var qaQuestion by remember(note.id) { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = {
                    selectedNote = null
                    viewModel.resetQaState()
                },
                title = { Text(note.title, fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 560.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Resumo da Inteligência Artificial:",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        item {
                            SelectionContainer {
                                Text(
                                    text = note.summary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                        if (note.content.isNotBlank()) {
                            item { HorizontalDivider() }
                            item {
                                Text(
                                    text = "Texto Base Original:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            item {
                                Text(
                                    text = note.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                )
                            }
                        }

                        // Q&A section
                        item { HorizontalDivider() }
                        item {
                            Text(
                                text = "💬 Pergunte ao Resumo",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = qaQuestion,
                                    onValueChange = { qaQuestion = it },
                                    placeholder = { Text("Ex: Quais são as exceções?", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                IconButton(
                                    onClick = {
                                        viewModel.askAboutNote(
                                            question = qaQuestion,
                                            noteContent = note.summary + if (note.content.isNotBlank()) "\n\n${note.content}" else ""
                                        )
                                    },
                                    enabled = qaQuestion.isNotBlank() && qaState !is UiState.Loading
                                ) {
                                    if (qaState is UiState.Loading) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Perguntar",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                        when (val state = qaState) {
                            is UiState.Success -> {
                                item {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = state.data,
                                            style = MaterialTheme.typography.bodySmall,
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }
                            is UiState.Error -> {
                                item {
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        selectedNote = null
                        viewModel.resetQaState()
                    }) {
                        Text("Fechar")
                    }
                }
            )
        }
    }
}

@Composable
fun SummaryOutputBlock(
    state: UiState<String>,
    onSaveSuccess: () -> Unit
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
                            text = "Aguardando o modelo Gemini. Isso pode demorar alguns segundos...",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            is UiState.Success -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Resumo Gerado e Salvo!",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = state.data,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp,
                            maxLines = 8,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onSaveSuccess,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Acessar Meus Resumos")
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
                                text = "Falha na Geração",
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedNoteCard(
    note: StudyNote,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onCreateFlashcards: () -> Unit,
    isCreatingFlashcards: Boolean = false
) {
    var showConfirmDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showConfirmDelete = true }
            )
            .testTag("saved_note_${note.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.summary.ifBlank { "Sem resumo." },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row {
                    // Create flashcards from this note
                    IconButton(
                        onClick = onCreateFlashcards,
                        enabled = !isCreatingFlashcards
                    ) {
                        if (isCreatingFlashcards) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Criar Flashcards",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { showConfirmDelete = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Excluir resumo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // Hint about Q&A
            Text(
                text = "Toque para ler completo e fazer perguntas 💬",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Excluir Resumo?") },
            text = { Text("Você tem certeza que deseja deletar permanentemente o resumo de '${note.title}'?") },
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
