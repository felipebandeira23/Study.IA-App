package com.example.ui.screen

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.StudyViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.Locale

import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDark
import com.example.ui.theme.BentoPrimaryLight
import com.example.ui.theme.BentoSoftViolet
import com.example.ui.theme.BentoTextMuted

// ── Constants ────────────────────────────────────────────────────────────────

private val ACADEMIC_OPTIONS = listOf(
    "Ensino Médio",
    "Ensino Técnico",
    "Superior (Cursando)",
    "Superior Completo",
    "Pós-Graduação / MBA",
    "Mestrado / Doutorado"
)

val AREAS_OF_INTEREST = listOf(
    "ti"            to "💻 Tecnologia da Informação",
    "juridico"      to "⚖️ Jurídico / Advocacia",
    "saude"         to "🏥 Saúde",
    "fiscal"        to "💰 Fiscal / Tributário",
    "militar"       to "🎖️ Militar / Segurança Nacional",
    "educacao"      to "📚 Educação / Magistério",
    "administrativo" to "🗂️ Administrativo",
    "seguranca"     to "🚔 Segurança Pública",
    "bancario"      to "🏦 Bancário / Financeiro"
)

val ESTADOS_BRASIL = listOf(
    "AC" to "Acre", "AL" to "Alagoas", "AP" to "Amapá", "AM" to "Amazonas",
    "BA" to "Bahia", "CE" to "Ceará", "DF" to "Distrito Federal", "ES" to "Espírito Santo",
    "GO" to "Goiás", "MA" to "Maranhão", "MT" to "Mato Grosso", "MS" to "Mato Grosso do Sul",
    "MG" to "Minas Gerais", "PA" to "Pará", "PB" to "Paraíba", "PR" to "Paraná",
    "PE" to "Pernambuco", "PI" to "Piauí", "RJ" to "Rio de Janeiro", "RN" to "Rio Grande do Norte",
    "RS" to "Rio Grande do Sul", "RO" to "Rondônia", "RR" to "Roraima", "SC" to "Santa Catarina",
    "SP" to "São Paulo", "SE" to "Sergipe", "TO" to "Tocantins"
)

private val STATE_NAME_TO_UF: Map<String, String> = ESTADOS_BRASIL.associate { (uf, name) -> name to uf }

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    // Load current values from ViewModel
    val savedName       by viewModel.userName.collectAsState()
    val savedAcademic   by viewModel.academicBackground.collectAsState()
    val savedAreasJson  by viewModel.areasOfInterestJson.collectAsState()
    val savedUf         by viewModel.userUf.collectAsState()

    // Local editable state
    var nameInput           by remember(savedName)     { mutableStateOf(savedName) }
    var selectedAcademic    by remember(savedAcademic) { mutableStateOf(savedAcademic.ifBlank { ACADEMIC_OPTIONS[0] }) }
    var selectedAreas       by remember(savedAreasJson) {
        mutableStateOf(
            try {
                val arr = JSONArray(savedAreasJson)
                (0 until arr.length()).map { arr.getString(it) }.toSet()
            } catch (_: Exception) { emptySet() }
        )
    }
    var selectedUf          by remember(savedUf) { mutableStateOf(savedUf) }
    var gpsLoading          by remember { mutableStateOf(false) }
    var showUfDropdown      by remember { mutableStateOf(false) }
    var showAcademicDropdown by remember { mutableStateOf(false) }
    var saveSuccess         by remember { mutableStateOf(false) }

    val locationPermission = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @SuppressLint("MissingPermission")
    fun fetchGpsLocation() {
        gpsLoading = true
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val geocoder = Geocoder(context, Locale.forLanguageTag("pt-BR"))
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                                    val stateName = addresses.firstOrNull()?.adminArea ?: ""
                                    val uf = STATE_NAME_TO_UF[stateName] ?: stateName.take(2).uppercase()
                                    scope.launch { selectedUf = uf; gpsLoading = false }
                                }
                            } else {
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                val stateName = addresses?.firstOrNull()?.adminArea ?: ""
                                val uf = STATE_NAME_TO_UF[stateName] ?: stateName.take(2).uppercase()
                                withContext(Dispatchers.Main) { selectedUf = uf; gpsLoading = false }
                            }
                        } catch (_: Exception) {
                            withContext(Dispatchers.Main) { gpsLoading = false }
                        }
                    }
                } else {
                    gpsLoading = false
                }
            }
            .addOnFailureListener { gpsLoading = false }
    }

    LaunchedEffect(locationPermission.status) {
        if (locationPermission.status.isGranted) fetchGpsLocation()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // ── Nome ──────────────────────────────────────────────────────────
            item {
                ProfileSection(title = "Nome") {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Como devemos te chamar?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }

            // ── Formação acadêmica ────────────────────────────────────────────
            item {
                ProfileSection(title = "Formação Acadêmica") {
                    ExposedDropdownMenuBox(
                        expanded = showAcademicDropdown,
                        onExpandedChange = { showAcademicDropdown = !showAcademicDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedAcademic,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Nível de escolaridade") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAcademicDropdown) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = showAcademicDropdown,
                            onDismissRequest = { showAcademicDropdown = false }
                        ) {
                            ACADEMIC_OPTIONS.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { selectedAcademic = option; showAcademicDropdown = false }
                                )
                            }
                        }
                    }
                }
            }

            // ── Estado (UF) ──────────────────────────────────────────────────
            item {
                ProfileSection(title = "Estado de Interesse") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = showUfDropdown,
                            onExpandedChange = { showUfDropdown = !showUfDropdown },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = if (selectedUf.isBlank()) "Selecione…"
                                        else "$selectedUf — ${ESTADOS_BRASIL.find { it.first == selectedUf }?.second ?: ""}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Seu estado") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUfDropdown) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = showUfDropdown,
                                onDismissRequest = { showUfDropdown = false }
                            ) {
                                ESTADOS_BRASIL.forEach { (uf, name) ->
                                    DropdownMenuItem(
                                        text = { Text("$uf — $name") },
                                        onClick = { selectedUf = uf; showUfDropdown = false }
                                    )
                                }
                            }
                        }
                        // GPS button
                        FilledTonalIconButton(
                            onClick = {
                                if (locationPermission.status.isGranted) fetchGpsLocation()
                                else locationPermission.launchPermissionRequest()
                            }
                        ) {
                            if (gpsLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.MyLocation, contentDescription = "Detectar estado via GPS")
                            }
                        }
                    }
                    Text(
                        text = "Toque no ícone GPS para detectar automaticamente",
                        fontSize = 11.sp,
                        color = BentoTextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // ── Áreas de interesse ───────────────────────────────────────────
            item {
                ProfileSection(title = "Áreas de Interesse para Concurso") {
                    Text(
                        text = "Selecione todas que se aplicam ao seu perfil:",
                        fontSize = 13.sp,
                        color = BentoTextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AREAS_OF_INTEREST.forEach { (key, label) ->
                            val selected = key in selectedAreas
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedAreas = if (selected)
                                        selectedAreas - key
                                    else
                                        selectedAreas + key
                                },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BentoPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // ── Salvar ───────────────────────────────────────────────────────
            item {
                if (saveSuccess) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                            Text("Perfil salvo! O feed de concursos será atualizado.", color = Color(0xFF1B5E20), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Button(
                    onClick = {
                        val areasJson = JSONArray(selectedAreas.toList()).toString()
                        viewModel.saveProfile(
                            name         = nameInput.trim(),
                            academicBg   = selectedAcademic,
                            areasJson    = areasJson,
                            uf           = selectedUf
                        )
                        saveSuccess = true
                    },
                    enabled = nameInput.isNotBlank() && selectedUf.isNotBlank() && selectedAreas.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar Perfil", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BentoPrimaryDark,
            letterSpacing = 0.5.sp
        )
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}
