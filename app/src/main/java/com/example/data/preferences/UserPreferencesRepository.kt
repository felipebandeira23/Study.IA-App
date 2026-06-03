package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val DAILY_CARD_GOAL = intPreferencesKey("daily_card_goal")
    }

    val dailyCardGoal: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DAILY_CARD_GOAL] ?: 20
    }

    suspend fun setDailyCardGoal(goal: Int) {
        context.dataStore.edit { prefs ->
            prefs[DAILY_CARD_GOAL] = goal
        }
    }
}
