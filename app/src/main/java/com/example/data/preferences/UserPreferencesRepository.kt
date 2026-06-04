package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val DAILY_CARD_GOAL        = intPreferencesKey("daily_card_goal")
        val USER_NAME              = stringPreferencesKey("user_name")
        val ACADEMIC_BACKGROUND    = stringPreferencesKey("academic_background")
        val AREAS_OF_INTEREST      = stringPreferencesKey("areas_of_interest")   // JSON array string
        val UF_STATE               = stringPreferencesKey("uf_state")             // e.g. "RJ"
        val HAS_COMPLETED_PROFILE  = booleanPreferencesKey("has_completed_profile")
    }

    // ── Readers ────────────────────────────────────────────────────────────────

    val dailyCardGoal: Flow<Int> = context.dataStore.data.map { it[DAILY_CARD_GOAL] ?: 20 }
    val userName: Flow<String>   = context.dataStore.data.map { it[USER_NAME] ?: "" }
    val academicBackground: Flow<String> = context.dataStore.data.map { it[ACADEMIC_BACKGROUND] ?: "" }
    val areasOfInterest: Flow<String>    = context.dataStore.data.map { it[AREAS_OF_INTEREST] ?: "[]" }
    val ufState: Flow<String>            = context.dataStore.data.map { it[UF_STATE] ?: "" }
    val hasCompletedProfile: Flow<Boolean> = context.dataStore.data.map { it[HAS_COMPLETED_PROFILE] ?: false }

    // ── Writers ────────────────────────────────────────────────────────────────

    suspend fun setDailyCardGoal(goal: Int) = context.dataStore.edit { it[DAILY_CARD_GOAL] = goal }

    suspend fun saveProfile(
        name: String,
        academicBackground: String,
        areasOfInterestJson: String,
        uf: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[USER_NAME]           = name
            prefs[ACADEMIC_BACKGROUND] = academicBackground
            prefs[AREAS_OF_INTEREST]   = areasOfInterestJson
            prefs[UF_STATE]            = uf
            prefs[HAS_COMPLETED_PROFILE] = true
        }
    }

    suspend fun updateUf(uf: String) = context.dataStore.edit { it[UF_STATE] = uf }
}
