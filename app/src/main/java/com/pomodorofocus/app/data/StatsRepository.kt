package com.pomodorofocus.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StatsRepository(private val context: Context, private val settingsRepository: SettingsRepository) {
    private object Keys {
        val sessions = stringPreferencesKey("completed_sessions")
    }

    private val dataStore = settingsRepository.dataStore()

    data class Session(val timestamp: Long, val durationSeconds: Long)

    fun sessionsFlow(): Flow<List<Session>> = dataStore.data.map { prefs ->
        parseSessions(prefs[Keys.sessions] ?: "")
    }

    suspend fun addSession(session: Session) {
        dataStore.edit { prefs ->
            val current = parseSessions(prefs[Keys.sessions] ?: "")
            val updated = (current + session).takeLast(200)
            prefs[Keys.sessions] = serializeSessions(updated)
        }
    }

    fun todayStats(sessions: List<Session>): Pair<Int, Long> {
        val today = LocalDate.now()
        val todays = sessions.filter { session ->
            LocalDate.ofInstant(Instant.ofEpochSecond(session.timestamp), ZoneId.systemDefault()) == today
        }
        val count = todays.size
        val totalSeconds = todays.sumOf { it.durationSeconds }
        return count to totalSeconds
    }

    fun lastSevenDays(sessions: List<Session>): Map<LocalDate, Long> {
        val today = LocalDate.now()
        return (0..6).associate { offset ->
            val date = today.minusDays(offset.toLong())
            val total = sessions.filter { session ->
                LocalDate.ofInstant(Instant.ofEpochSecond(session.timestamp), ZoneId.systemDefault()) == date
            }.sumOf { it.durationSeconds }
            date to total
        }.toSortedMap()
    }

    private fun parseSessions(raw: String): List<Session> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size != 2) return@mapNotNull null
            val timestamp = parts[0].toLongOrNull() ?: return@mapNotNull null
            val duration = parts[1].toLongOrNull() ?: return@mapNotNull null
            Session(timestamp, duration)
        }
    }

    private fun serializeSessions(sessions: List<Session>): String {
        return sessions.joinToString(";") { "${it.timestamp}|${it.durationSeconds}" }
    }
}
