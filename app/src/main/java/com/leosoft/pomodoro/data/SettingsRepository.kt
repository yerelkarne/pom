package com.leosoft.pomodoro.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pomodoro_settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val focusMinutes = intPreferencesKey("focus_minutes")
        val shortBreakMinutes = intPreferencesKey("short_break_minutes")
        val longBreakMinutes = intPreferencesKey("long_break_minutes")
        val longBreakInterval = intPreferencesKey("long_break_interval")
        val autoNext = booleanPreferencesKey("auto_next")
        val soundEnabled = booleanPreferencesKey("sound_enabled")
        val vibrationEnabled = booleanPreferencesKey("vibration_enabled")
    }

    val settingsFlow: Flow<SettingsState> = context.dataStore.data.map { prefs ->
        SettingsState(
            focusMinutes = prefs[Keys.focusMinutes] ?: 25,
            shortBreakMinutes = prefs[Keys.shortBreakMinutes] ?: 5,
            longBreakMinutes = prefs[Keys.longBreakMinutes] ?: 15,
            longBreakInterval = prefs[Keys.longBreakInterval] ?: 4,
            autoNext = prefs[Keys.autoNext] ?: true,
            soundEnabled = prefs[Keys.soundEnabled] ?: true,
            vibrationEnabled = prefs[Keys.vibrationEnabled] ?: true
        )
    }

    suspend fun updateFocusMinutes(value: Int) {
        context.dataStore.edit { it[Keys.focusMinutes] = value }
    }

    suspend fun updateShortBreakMinutes(value: Int) {
        context.dataStore.edit { it[Keys.shortBreakMinutes] = value }
    }

    suspend fun updateLongBreakMinutes(value: Int) {
        context.dataStore.edit { it[Keys.longBreakMinutes] = value }
    }

    suspend fun updateLongBreakInterval(value: Int) {
        context.dataStore.edit { it[Keys.longBreakInterval] = value }
    }

    suspend fun updateAutoNext(value: Boolean) {
        context.dataStore.edit { it[Keys.autoNext] = value }
    }

    suspend fun updateSoundEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.soundEnabled] = value }
    }

    suspend fun updateVibrationEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.vibrationEnabled] = value }
    }

    fun dataStore(): DataStore<Preferences> = context.dataStore
}
