package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.remote.GeminiClient
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Calendar

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class StudyViewModel(
    private val repository: StudyRepository,
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

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

    val allFlashcards: StateFlow<List<Flashcard>> = repository.allFlashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cards reviewed today (based on sessions with date >= start of today)
    val cardsReviewedToday: StateFlow<Int> = repository.allSessions
        .map { sessions ->
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            sessions.filter { it.date >= todayStart }.sumOf { it.cardsReviewed }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Daily card goal from DataStore (default 20)
    val dailyCardGoal: StateFlow<Int> = prefsRepository.dailyCardGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)

    // User profile fields from DataStore
    val userName: StateFlow<String> = prefsRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val userUf: StateFlow<String> = prefsRepository.ufState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val academicBackground: StateFlow<String> = prefsRepository.academicBackground
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val areasOfInterestJson: StateFlow<String> = prefsRepository.areasOfInterest
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "[]")
    val hasCompletedProfile: StateFlow<Boolean> = prefsRepository.hasCompletedProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Contest news feed
    private val _contestNewsState = MutableStateFlow<UiState<List<ContestNewsItem>>>(UiState.Idle)
    val contestNewsState: StateFlow<UiState<List<ContestNewsItem>>> = _contestNewsState.asStateFlow()

    // Contest auto-fill
    private val _autoFillState = MutableStateFlow<UiState<ContestAutoFill>>(UiState.Idle)
    val autoFillState: StateFlow<UiState<ContestAutoFill>> = _autoFillState.asStateFlow()

    private var lastNewsRefreshMs = 0L

    // UI generation states
    private val _summaryState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val summaryState: StateFlow<UiState<String>> = _summaryState.asStateFlow()

    private val _flashcardState = MutableStateFlow<UiState<Int>>(UiState.Idle)
    val flashcardState: StateFlow<UiState<Int>> = _flashcardState.asStateFlow()

    private val _planState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val planState: StateFlow<UiState<String>> = _planState.asStateFlow()

    // State for "Criar Flashcards" from a saved note
    private val _fromNoteFlashcardState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val fromNoteFlashcardState: StateFlow<UiState<Unit>> = _fromNoteFlashcardState.asStateFlow()
    private val _fromNoteFlashcardNoteId = MutableStateFlow<Int?>(null)
    val fromNoteFlashcardNoteId: StateFlow<Int?> = _fromNoteFlashcardNoteId.asStateFlow()

    // State for Q&A on a note
    private val _qaState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val qaState: StateFlow<UiState<String>> = _qaState.asStateFlow()

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
        viewModelScope.launch { repository.deleteNote(note) }
    }

    fun resetSummaryState() {
        _summaryState.value = UiState.Idle
    }

    // Generates flashcards directly from a saved note (uses summary + content as source)
    fun generateFlashcardsFromNote(note: StudyNote) {
        viewModelScope.launch {
            _fromNoteFlashcardNoteId.value = note.id
            _fromNoteFlashcardState.value = UiState.Loading
            try {
                val content = buildString {
                    if (note.summary.isNotBlank()) append(note.summary)
                    if (note.content.isNotBlank()) {
                        if (isNotEmpty()) append("\n\n")
                        append(note.content)
                    }
                }
                val cards = repository.generateFlashcards(content, 5)
                repository.saveDeckWithCards(
                    title = note.title,
                    topic = note.topic.ifBlank { note.title },
                    description = "Gerado automaticamente a partir do resumo '${note.title}'",
                    cards = cards
                )
                _fromNoteFlashcardState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _fromNoteFlashcardState.value = UiState.Error(e.message ?: "Erro ao gerar flashcards.")
            }
        }
    }

    fun resetFromNoteFlashcardState() {
        _fromNoteFlashcardState.value = UiState.Idle
        _fromNoteFlashcardNoteId.value = null
    }

    // Q&A: asks the AI a question using the note content as context
    fun askAboutNote(question: String, noteContent: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _qaState.value = UiState.Loading
            try {
                val answer = repository.askAboutNote(question, noteContent)
                _qaState.value = UiState.Success(answer)
            } catch (e: Exception) {
                _qaState.value = UiState.Error(e.message ?: "Erro ao processar a pergunta.")
            }
        }
    }

    fun resetQaState() {
        _qaState.value = UiState.Idle
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
                    description = "Gerado automaticamente com o tema '$title' contendo $count cards.",
                    cards = cards
                )
                _flashcardState.value = UiState.Success(deckId)
            } catch (e: Exception) {
                _flashcardState.value = UiState.Error(e.message ?: "Erro ao gerar os flashcards.")
            }
        }
    }

    fun deleteDeck(deckId: Int) {
        viewModelScope.launch { repository.deleteDeck(deckId) }
    }

    fun resetFlashcardState() {
        _flashcardState.value = UiState.Idle
    }

    fun getFlashcardsForDeck(deckId: Int): Flow<List<Flashcard>> =
        repository.getFlashcardsForDeck(deckId)

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
        viewModelScope.launch { repository.deletePlan(planId) }
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
            repository.deleteEditalTopicsForContest(id)
        }
    }

    // --- Edital Topics Actions ---
    fun getEditalTopicsForContest(contestId: Int): Flow<List<EditalTopic>> =
        repository.getEditalTopicsForContest(contestId)

    fun addEditalTopic(contestId: Int, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { repository.insertEditalTopic(contestId, title) }
    }

    fun updateEditalTopicStatus(topicId: Int, status: Int) {
        viewModelScope.launch { repository.updateEditalTopicStatus(topicId, status) }
    }

    // --- Daily Goal ---
    fun setDailyCardGoal(goal: Int) {
        if (goal <= 0) return
        viewModelScope.launch { prefsRepository.setDailyCardGoal(goal) }
    }

    // --- User Profile ---
    fun saveProfile(name: String, academicBg: String, areasJson: String, uf: String) {
        viewModelScope.launch {
            prefsRepository.saveProfile(name, academicBg, areasJson, uf)
            refreshContestNews(force = true)
        }
    }

    fun updateUf(uf: String) {
        viewModelScope.launch { prefsRepository.updateUf(uf) }
    }

    /** Parses the stored JSON array of area keys into a readable list. */
    fun areasFromJson(json: String): List<String> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Exception) { emptyList() }
    }

    // --- Contest News Feed ---
    fun refreshContestNews(force: Boolean = false) {
        val now = System.currentTimeMillis()
        val thirtyMinutes = 30 * 60 * 1000L
        if (!force && now - lastNewsRefreshMs < thirtyMinutes && _contestNewsState.value is UiState.Success) return

        viewModelScope.launch {
            _contestNewsState.value = UiState.Loading
            try {
                val uf    = userUf.value
                val areas = areasFromJson(areasOfInterestJson.value)
                val news  = repository.fetchContestNews(uf, areas)
                _contestNewsState.value = UiState.Success(news)
                lastNewsRefreshMs = System.currentTimeMillis()
            } catch (e: Exception) {
                _contestNewsState.value = UiState.Error(e.message ?: "Erro ao buscar concursos")
            }
        }
    }

    // --- Contest Auto-Fill ---
    fun autoFillContest(contestName: String) {
        if (contestName.isBlank()) return
        viewModelScope.launch {
            _autoFillState.value = UiState.Loading
            try {
                val result = repository.fetchContestAutoFill(contestName)
                _autoFillState.value = UiState.Success(result)
            } catch (e: Exception) {
                _autoFillState.value = UiState.Error(e.message ?: "Erro ao buscar dados do concurso")
            }
        }
    }

    fun resetAutoFillState() { _autoFillState.value = UiState.Idle }

    // --- Session Stats Recording ---
    fun saveSessionStats(deckId: Int, cardsReviewed: Int, correctAnswers: Int) {
        viewModelScope.launch {
            repository.recordSession(deckId, cardsReviewed, correctAnswers)
        }
    }

    companion object {
        fun provideFactory(
            repository: StudyRepository,
            prefsRepository: UserPreferencesRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StudyViewModel(repository, prefsRepository) as T
                }
            }
    }
}
