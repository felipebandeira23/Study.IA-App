package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.remote.GeminiClient
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class StudyViewModel(private val repository: StudyRepository) : ViewModel() {

    // Reactive streams from local DB
    val allNotes: StateFlow<List<StudyNote>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDecks: StateFlow<List<Deck>> = repository.allDecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlans: StateFlow<List<StudyPlan>> = repository.allPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allContests: StateFlow<List<TrackedContest>> = repository.allContests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<StudySession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI generation states
    private val _summaryState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val summaryState: StateFlow<UiState<String>> = _summaryState.asStateFlow()

    private val _flashcardState = MutableStateFlow<UiState<Int>>(UiState.Idle) // Success outputs inside deckId
    val flashcardState: StateFlow<UiState<Int>> = _flashcardState.asStateFlow()

    private val _planState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val planState: StateFlow<UiState<String>> = _planState.asStateFlow()

    // API check state for warning alerts
    val isApiKeyAvailable: Boolean
        get() = GeminiClient.isApiKeyAvailable()

    // --- Study Notes Actions ---
    fun createSummary(title: String, content: String) {
        viewModelScope.launch {
            _summaryState.value = UiState.Loading
            try {
                if (title.isBlank()) {
                    _summaryState.value = UiState.Error("O título do resumo não pode ser em branco.")
                    return@launch
                }
                val summary = repository.generateSummary(title, content)
                repository.saveNote(title = title, content = content, summary = summary, topic = title)
                _summaryState.value = UiState.Success(summary)
            } catch (e: Exception) {
                _summaryState.value = UiState.Error(e.message ?: "Erro desconhecido ao gerar resumo.")
            }
        }
    }

    fun deleteNote(note: StudyNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun resetSummaryState() {
        _summaryState.value = UiState.Idle
    }

    // --- Decks & Flashcards Actions ---
    fun createFlashcardsDeck(title: String, contentOrTopic: String, count: Int) {
        viewModelScope.launch {
            _flashcardState.value = UiState.Loading
            try {
                if (title.isBlank()) {
                    _flashcardState.value = UiState.Error("O título do deck de flashcards não pode ser vazio.")
                    return@launch
                }
                val cards = repository.generateFlashcards(contentOrTopic, count)
                val deckId = repository.saveDeckWithCards(
                    title = title,
                    topic = contentOrTopic,
                    description = "Gerdado automaticamente com o tema '$title' contendo $count cards.",
                    cards = cards
                )
                _flashcardState.value = UiState.Success(deckId)
            } catch (e: Exception) {
                _flashcardState.value = UiState.Error(e.message ?: "Erro ao gerar os flashcards.")
            }
        }
    }

    fun deleteDeck(deckId: Int) {
        viewModelScope.launch {
            repository.deleteDeck(deckId)
        }
    }

    fun resetFlashcardState() {
        _flashcardState.value = UiState.Idle
    }

    fun getFlashcardsForDeck(deckId: Int): Flow<List<Flashcard>> {
        return repository.getFlashcardsForDeck(deckId)
    }

    // --- Study Plan Actions ---
    fun createStudyPlan(topic: String, durationDays: Int, level: String, contestId: Int?) {
        viewModelScope.launch {
            _planState.value = UiState.Loading
            try {
                if (topic.isBlank()) {
                    _planState.value = UiState.Error("O tema do plano de estudo é obrigatório.")
                    return@launch
                }
                val contest = if (contestId != null && contestId > 0) {
                    allContests.value.find { it.id == contestId }
                } else null

                val planJsonContent = repository.generateStudyPlan(topic, durationDays, level, contest)
                repository.savePlan(
                    topic = topic,
                    durationDays = durationDays,
                    level = level,
                    content = planJsonContent
                )
                _planState.value = UiState.Success(planJsonContent)
            } catch (e: Exception) {
                _planState.value = UiState.Error(e.message ?: "Erro ao gerar o plano de estudos.")
            }
        }
    }

    fun deletePlan(planId: Int) {
        viewModelScope.launch {
            repository.deletePlan(planId)
        }
    }

    fun resetPlanState() {
        _planState.value = UiState.Idle
    }

    // --- Tracked Contests Actions ---
    fun addTrackedContest(name: String, organizer: String, examDate: String, editalText: String, notes: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.saveContest(
                    name = name,
                    organizer = organizer,
                    examDate = examDate,
                    editalText = editalText,
                    notes = notes
                )
            }
        }
    }

    fun deleteTrackedContest(id: Int) {
        viewModelScope.launch {
            repository.deleteContest(id)
        }
    }

    // --- Session Stats Recording ---
    fun saveSessionStats(deckId: Int, cardsReviewed: Int, correctAnswers: Int) {
        viewModelScope.launch {
            repository.recordSession(deckId, cardsReviewed, correctAnswers)
        }
    }

    // Companion factory for manual dependency injection helper
    companion object {
        fun provideFactory(repository: StudyRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StudyViewModel(repository) as T
                }
            }
    }
}
