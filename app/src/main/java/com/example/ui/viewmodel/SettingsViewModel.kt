package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.remote.GeminiClient
import com.example.worker.DailyStudyReminderWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val prefsRepository: UserPreferencesRepository
) : AndroidViewModel(application) {

    val apiKey: StateFlow<String> = prefsRepository.apiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userName: StateFlow<String> = prefsRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val isDarkMode: StateFlow<Boolean> = prefsRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hasCompletedOnboarding: StateFlow<Boolean> = prefsRepository.hasCompletedOnboarding
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val remindersEnabled: StateFlow<Boolean> = prefsRepository.remindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val reminderHour: StateFlow<Int> = prefsRepository.reminderHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)

    val reminderMinute: StateFlow<Int> = prefsRepository.reminderMinute
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _testConnectionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val testConnectionState: StateFlow<UiState<String>> = _testConnectionState.asStateFlow()

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            prefsRepository.saveApiKey(key)
            GeminiClient.runtimeApiKey = key.trim().takeIf { it.isNotBlank() }
        }
    }

    fun saveUserName(name: String) {
        viewModelScope.launch { prefsRepository.saveUserName(name) }
    }

    fun saveDarkMode(enabled: Boolean) {
        viewModelScope.launch { prefsRepository.saveDarkMode(enabled) }
    }

    fun markOnboardingComplete() {
        viewModelScope.launch { prefsRepository.markOnboardingComplete() }
    }

    fun testApiConnection(key: String) {
        viewModelScope.launch {
            _testConnectionState.value = UiState.Loading
            try {
                val trimmed = key.trim()
                if (trimmed.isBlank()) {
                    _testConnectionState.value = UiState.Error("Insira uma chave de API válida.")
                    return@launch
                }
                GeminiClient.runtimeApiKey = trimmed
                GeminiClient.fetchContent("Responda apenas: OK")
                prefsRepository.saveApiKey(trimmed)
                _testConnectionState.value = UiState.Success("Conexão bem-sucedida! IA Gemini ativa.")
            } catch (e: Exception) {
                _testConnectionState.value = UiState.Error("Falha na conexão: ${e.message}")
            }
        }
    }

    fun resetTestState() {
        _testConnectionState.value = UiState.Idle
    }

    fun saveReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            prefsRepository.saveReminderSettings(enabled, hour, minute)
            val context = getApplication<Application>()
            if (enabled) {
                DailyStudyReminderWorker.schedule(context, hour, minute)
            } else {
                DailyStudyReminderWorker.cancel(context)
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch { prefsRepository.clearAllPreferences() }
    }

    companion object {
        fun provideFactory(
            application: Application,
            prefsRepository: UserPreferencesRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(application, prefsRepository) as T
                }
            }
    }
}
