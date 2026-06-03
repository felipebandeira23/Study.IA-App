package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    val apiKey: Flow<String> = context.dataStore.data.map { it[Keys.API_KEY] ?: "" }
    val userName: Flow<String> = context.dataStore.data.map { it[Keys.USER_NAME] ?: "" }
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[Keys.DARK_MODE] ?: false }
    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { it[Keys.API_KEY] = key }
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { it[Keys.USER_NAME] = name }
    }

    suspend fun saveDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun markOnboardingComplete() {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = true }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { it.clear() }
    }

    private object Keys {
        val API_KEY = stringPreferencesKey("gemini_api_key")
        val USER_NAME = stringPreferencesKey("user_name")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }
}
