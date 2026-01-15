package com.leosoft.pomodoro

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leosoft.pomodoro.data.SettingsRepository
import com.leosoft.pomodoro.data.SettingsState
import com.leosoft.pomodoro.data.StatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class SettingsAction {
    data class UpdateFocus(val minutes: Int) : SettingsAction()
    data class UpdateShortBreak(val minutes: Int) : SettingsAction()
    data class UpdateLongBreak(val minutes: Int) : SettingsAction()
    data class UpdateLongBreakInterval(val count: Int) : SettingsAction()
    data class UpdateAutoNext(val enabled: Boolean) : SettingsAction()
    data class UpdateSound(val enabled: Boolean) : SettingsAction()
    data class UpdateVibration(val enabled: Boolean) : SettingsAction()
}

data class StatsState(
    val todayCount: Int = 0,
    val todaySeconds: Long = 0,
    val lastSevenDays: Map<String, Long> = emptyMap()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)
    private val statsRepository = StatsRepository(application, settingsRepository)

    private val notificationGranted = MutableStateFlow(checkNotificationPermission())

    val settingsState: StateFlow<SettingsState> = settingsRepository.settingsFlow
        .combine(notificationGranted) { settings, granted ->
            settings.copy(notificationPermissionGranted = granted)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    val statsState: StateFlow<StatsState> = statsRepository.sessionsFlow()
        .map { sessions ->
            val (count, seconds) = statsRepository.todayStats(sessions)
            val lastSeven = statsRepository.lastSevenDays(sessions).mapKeys { it.key.toString() }
            StatsState(count, seconds, lastSeven)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsState())

    fun handleSettingsAction(action: SettingsAction) {
        viewModelScope.launch {
            when (action) {
                is SettingsAction.UpdateFocus -> settingsRepository.updateFocusMinutes(action.minutes)
                is SettingsAction.UpdateShortBreak -> settingsRepository.updateShortBreakMinutes(action.minutes)
                is SettingsAction.UpdateLongBreak -> settingsRepository.updateLongBreakMinutes(action.minutes)
                is SettingsAction.UpdateLongBreakInterval -> settingsRepository.updateLongBreakInterval(action.count)
                is SettingsAction.UpdateAutoNext -> settingsRepository.updateAutoNext(action.enabled)
                is SettingsAction.UpdateSound -> settingsRepository.updateSoundEnabled(action.enabled)
                is SettingsAction.UpdateVibration -> settingsRepository.updateVibrationEnabled(action.enabled)
            }
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        notificationGranted.value = granted
    }

    fun recordSession(durationSeconds: Long) {
        viewModelScope.launch {
            statsRepository.addSession(
                StatsRepository.Session(
                    timestamp = System.currentTimeMillis() / 1000,
                    durationSeconds = durationSeconds
                )
            )
        }
    }

    private fun checkNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        val status = ContextCompat.checkSelfPermission(
            getApplication(),
            android.Manifest.permission.POST_NOTIFICATIONS
        )
        return status == PackageManager.PERMISSION_GRANTED
    }
}
