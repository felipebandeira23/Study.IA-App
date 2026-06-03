package com.example.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: SettingsViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    var nameInput by remember { mutableStateOf("") }
    var apiKeyInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBg)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> OnboardingWelcomePage()
                1 -> OnboardingNamePage(nameInput = nameInput, onNameChange = { nameInput = it })
                2 -> OnboardingApiKeyPage(apiKeyInput = apiKeyInput, onKeyChange = { apiKeyInput = it })
            }
        }

        // Page indicator dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == index) 24.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (pagerState.currentPage == index) BentoPrimary
                            else BentoBorder
                        )
                )
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Skip / Back
            if (pagerState.currentPage < 2) {
                TextButton(onClick = {
                    viewModel.markOnboardingComplete()
                    onComplete()
                }) {
                    Text("Pular", color = BentoTextMuted, fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.width(80.dp))
            }

            // Next / Finish
            Button(
                onClick = {
                    when (pagerState.currentPage) {
                        0 -> scope.launch { pagerState.animateScrollToPage(1) }
                        1 -> {
                            if (nameInput.isNotBlank()) viewModel.saveUserName(nameInput)
                            scope.launch { pagerState.animateScrollToPage(2) }
                        }
                        2 -> {
                            if (apiKeyInput.isNotBlank()) viewModel.saveApiKey(apiKeyInput)
                            viewModel.markOnboardingComplete()
                            onComplete()
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                modifier = Modifier.height(50.dp).widthIn(min = 140.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage == 2) "Começar a Estudar" else "Continuar",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                if (pagerState.currentPage < 2) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun OnboardingWelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎓", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Study.ia",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = BentoPrimary,
            letterSpacing = (-1).sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Seu assistente pessoal de estudos com Inteligência Artificial",
            fontSize = 16.sp,
            color = BentoTextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(48.dp))

        val features = listOf(
            "📝" to "Resumos inteligentes gerados por IA",
            "🃏" to "Flashcards com repetição espaçada",
            "📅" to "Planos de estudo personalizados",
            "📌" to "Rastreamento de concursos e editais"
        )
        features.forEach { (emoji, text) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(emoji, fontSize = 20.sp)
                Text(text, fontSize = 14.sp, color = BentoTextDark, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun OnboardingNamePage(nameInput: String, onNameChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(BentoPrimaryLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = BentoPrimaryDark, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Como devemos te chamar?",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = BentoPrimaryDark,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Seu nome aparecerá no painel e personalizará sua experiência.",
            fontSize = 14.sp,
            color = BentoTextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(36.dp))
        OutlinedTextField(
            value = nameInput,
            onValueChange = onNameChange,
            label = { Text("Seu nome") },
            placeholder = { Text("ex: Felipe") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BentoPrimary,
                focusedLabelColor = BentoPrimary
            )
        )
    }
}

@Composable
private fun OnboardingApiKeyPage(apiKeyInput: String, onKeyChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(BentoSoftViolet),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Key, contentDescription = null, tint = BentoPrimaryDark, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Configure a IA Gemini",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = BentoPrimaryDark,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Cole sua chave gratuita do Google Gemini para ativar resumos, flashcards e planos com IA real.",
            fontSize = 14.sp,
            color = BentoTextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = BentoPrimaryLight,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Obtenha gratuitamente em aistudio.google.com",
                fontSize = 12.sp,
                color = BentoPrimaryDark,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        OutlinedTextField(
            value = apiKeyInput,
            onValueChange = onKeyChange,
            label = { Text("Gemini API Key") },
            placeholder = { Text("AIza...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BentoPrimary,
                focusedLabelColor = BentoPrimary
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Pode pular agora e configurar depois em Configurações → Chave de API",
            fontSize = 12.sp,
            color = BentoTextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}
