package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val savedApiKey by viewModel.apiKey.collectAsState()
    val savedUserName by viewModel.userName.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val testState by viewModel.testConnectionState.collectAsState()

    var apiKeyInput by remember(savedApiKey) { mutableStateOf(savedApiKey) }
    var userNameInput by remember(savedUserName) { mutableStateOf(savedUserName) }
    var showApiKey by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { viewModel.resetTestState() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Configurações", fontWeight = FontWeight.Black, color = BentoPrimaryDark)
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
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
            // --- Perfil ---
            item {
                SettingsSectionHeader(emoji = "👤", title = "Perfil")
            }
            item {
                OutlinedTextField(
                    value = userNameInput,
                    onValueChange = { userNameInput = it },
                    label = { Text("Seu nome") },
                    placeholder = { Text("Como devemos te chamar?") },
                    modifier = Modifier.fillMaxWidth().testTag("settings_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (userNameInput != savedUserName && userNameInput.isNotBlank()) {
                            IconButton(onClick = { viewModel.saveUserName(userNameInput) }) {
                                Icon(Icons.Default.Check, contentDescription = "Salvar nome", tint = BentoPrimary)
                            }
                        }
                    }
                )
            }

            // --- API Key ---
            item {
                SettingsSectionHeader(emoji = "🔑", title = "Chave de API Gemini")
            }
            item {
                Text(
                    text = "Insira sua chave da API Gemini para ativar as funcionalidades de IA. Obtenha gratuitamente em aistudio.google.com.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextMuted,
                    lineHeight = 18.sp
                )
            }
            item {
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("AIza...") },
                    modifier = Modifier.fillMaxWidth().testTag("settings_api_key_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                if (showApiKey) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (showApiKey) "Ocultar chave" else "Mostrar chave"
                            )
                        }
                    }
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.testApiConnection(apiKeyInput) },
                        enabled = apiKeyInput.isNotBlank() && testState !is UiState.Loading,
                        modifier = Modifier.weight(1f).testTag("settings_test_connection_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (testState is UiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Testar Conexão")
                    }
                    Button(
                        onClick = { viewModel.saveApiKey(apiKeyInput) },
                        enabled = apiKeyInput.isNotBlank() && apiKeyInput != savedApiKey,
                        modifier = Modifier.weight(1f).testTag("settings_save_key_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salvar Chave")
                    }
                }
            }
            item {
                AnimatedVisibility(visible = testState !is UiState.Idle) {
                    when (testState) {
                        is UiState.Success -> Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                                Text((testState as UiState.Success).data, color = Color(0xFF1B5E20), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        is UiState.Error -> Surface(
                            color = BentoPink,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = BentoPrimaryDark, modifier = Modifier.size(20.dp))
                                Text((testState as UiState.Error).message, color = BentoPrimaryDark, fontSize = 13.sp)
                            }
                        }
                        else -> {}
                    }
                }
            }

            // --- Aparência ---
            item {
                SettingsSectionHeader(emoji = "🎨", title = "Aparência")
            }
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BentoSoftViolet),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DarkMode, contentDescription = null, tint = BentoPrimaryDark, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("Tema Escuro", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BentoTextDark)
                                Text("Mudar aparência do aplicativo", fontSize = 11.sp, color = BentoTextMuted)
                            }
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.saveDarkMode(it) },
                            modifier = Modifier.testTag("settings_dark_mode_switch")
                        )
                    }
                }
            }

            // --- Danger Zone ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(emoji = "⚠️", title = "Zona de Risco")
            }
            item {
                OutlinedButton(
                    onClick = { showClearDataDialog = true },
                    modifier = Modifier.fillMaxWidth().testTag("settings_clear_data_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Limpar Preferências e Configurações")
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Limpar Configurações?") },
            text = { Text("Isso irá remover sua chave de API, nome e preferências salvas. Os dados de estudo (resumos, flashcards, planos) não serão afetados.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Limpar") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(emoji: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    ) {
        Text(emoji, fontSize = 18.sp)
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = BentoPrimaryDark,
            letterSpacing = (-0.3).sp
        )
    }
}
