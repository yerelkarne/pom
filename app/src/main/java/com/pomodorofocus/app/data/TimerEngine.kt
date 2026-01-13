package com.pomodorofocus.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.max

class TimerEngine(private val context: Context, private val settingsRepository: SettingsRepository) {
    private object Keys {
        val mode = stringPreferencesKey("timer_mode")
        val isRunning = booleanPreferencesKey("timer_running")
        val startEpoch = longPreferencesKey("timer_start_epoch")
        val totalSeconds = longPreferencesKey("timer_total_seconds")
        val remainingSeconds = longPreferencesKey("timer_remaining_seconds")
        val cycleCount = intPreferencesKey("timer_cycle_count")
        val lockedMode = booleanPreferencesKey("timer_locked_mode")
    }

    private val dataStore = settingsRepository.dataStore()

    fun timerStateFlow(): Flow<TimerState> {
        return dataStore.data.map { prefs ->
            val mode = TimerMode.valueOf(prefs[Keys.mode] ?: TimerMode.FOCUS.name)
            val totalSeconds = prefs[Keys.totalSeconds] ?: 25 * 60L
            val running = prefs[Keys.isRunning] ?: false
            val startEpoch = prefs[Keys.startEpoch] ?: 0L
            val remaining = if (running && startEpoch > 0L) {
                val elapsed = (System.currentTimeMillis() - startEpoch) / 1000
                max(totalSeconds - elapsed, 0)
            } else {
                prefs[Keys.remainingSeconds] ?: totalSeconds
            }
            TimerState(
                mode = mode,
                isRunning = running,
                totalSeconds = totalSeconds,
                remainingSeconds = remaining,
                cycleCount = prefs[Keys.cycleCount] ?: 0,
                lockedMode = prefs[Keys.lockedMode] ?: false,
                startEpochMillis = startEpoch
            )
        }
    }

    suspend fun start(mode: TimerMode, totalSeconds: Long, cycleCount: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.mode] = mode.name
            prefs[Keys.isRunning] = true
            prefs[Keys.startEpoch] = System.currentTimeMillis()
            prefs[Keys.totalSeconds] = totalSeconds
            prefs[Keys.remainingSeconds] = totalSeconds
            prefs[Keys.cycleCount] = cycleCount
        }
    }

    suspend fun pause(currentRemaining: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.isRunning] = false
            prefs[Keys.remainingSeconds] = currentRemaining
        }
    }

    suspend fun resume(currentRemaining: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.isRunning] = true
            prefs[Keys.startEpoch] = System.currentTimeMillis()
            prefs[Keys.totalSeconds] = currentRemaining
            prefs[Keys.remainingSeconds] = currentRemaining
        }
    }

    suspend fun updateMode(mode: TimerMode, totalSeconds: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.mode] = mode.name
            prefs[Keys.totalSeconds] = totalSeconds
            prefs[Keys.remainingSeconds] = totalSeconds
            prefs[Keys.isRunning] = false
            prefs[Keys.startEpoch] = 0L
        }
    }

    suspend fun incrementCycle() {
        dataStore.edit { prefs ->
            val current = prefs[Keys.cycleCount] ?: 0
            prefs[Keys.cycleCount] = current + 1
        }
    }

    suspend fun resetCycle() {
        dataStore.edit { prefs ->
            prefs[Keys.cycleCount] = 0
        }
    }

    suspend fun setLockedMode(locked: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.lockedMode] = locked
        }
    }

    suspend fun stop() {
        dataStore.edit { prefs ->
            prefs[Keys.isRunning] = false
            prefs[Keys.startEpoch] = 0L
        }
    }
}
