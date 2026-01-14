package com.leosoft.pomodoro

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leosoft.pomodoro.alarm.NotificationHelper
import com.leosoft.pomodoro.alarm.PomodoroAlarmReceiver
import com.leosoft.pomodoro.data.SettingsRepository
import com.leosoft.pomodoro.data.SettingsState
import com.leosoft.pomodoro.data.TimerEngine
import com.leosoft.pomodoro.data.TimerMode
import com.leosoft.pomodoro.data.TimerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max

sealed class TimerAction {
    object Start : TimerAction()
    object Pause : TimerAction()
    object Resume : TimerAction()
    object Stop : TimerAction()
    data class SetCustomMinutes(val minutes: Int) : TimerAction()
    data class SwitchMode(val mode: TimerMode) : TimerAction()
    data class SetLockedMode(val locked: Boolean) : TimerAction()
}

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)
    private val timerEngine = TimerEngine(application, settingsRepository)

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState

    private val settingsState = settingsRepository.settingsFlow
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), SettingsState())

    private var tickerJob: Job? = null

    init {
        viewModelScope.launch {
            timerEngine.timerStateFlow().collectLatest { state ->
                _timerState.value = state
            }
        }
        startTicker()
    }

    fun handleTimerAction(action: TimerAction) {
        when (action) {
            TimerAction.Start -> startTimer()
            TimerAction.Pause -> pauseTimer()
            TimerAction.Resume -> resumeTimer()
            TimerAction.Stop -> stopTimer()
            is TimerAction.SetCustomMinutes -> setCustomMinutes(action.minutes)
            is TimerAction.SwitchMode -> switchMode(action.mode)
            is TimerAction.SetLockedMode -> setLockedMode(action.locked)
        }
    }

    private fun startTimer() {
        val current = _timerState.value
        viewModelScope.launch {
            timerEngine.start(current.mode, current.remainingSeconds, current.cycleCount)
            scheduleAlarm(current.remainingSeconds)
        }
    }

    private fun pauseTimer() {
        val current = _timerState.value
        viewModelScope.launch {
            timerEngine.pause(current.remainingSeconds)
            cancelAlarm()
        }
    }

    private fun resumeTimer() {
        val current = _timerState.value
        viewModelScope.launch {
            timerEngine.resume(current.remainingSeconds)
            scheduleAlarm(current.remainingSeconds)
        }
    }

    private fun stopTimer() {
        viewModelScope.launch {
            timerEngine.stop()
            cancelAlarm()
            val updated = _timerState.value.copy(isRunning = false)
            _timerState.value = updated
        }
    }

    private fun setCustomMinutes(minutes: Int) {
        viewModelScope.launch {
            val seconds = minutes * 60L
            timerEngine.updateMode(TimerMode.FOCUS, seconds)
        }
    }

    private fun switchMode(mode: TimerMode) {
        val settings = settingsState.value
        val totalSeconds = when (mode) {
            TimerMode.FOCUS -> settings.focusSeconds
            TimerMode.SHORT_BREAK -> settings.shortBreakSeconds
            TimerMode.LONG_BREAK -> settings.longBreakSeconds
        }
        viewModelScope.launch {
            timerEngine.updateMode(mode, totalSeconds)
        }
    }

    private fun setLockedMode(locked: Boolean) {
        viewModelScope.launch {
            timerEngine.setLockedMode(locked)
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _timerState.value
                if (current.isRunning && current.startEpochMillis > 0L) {
                    val elapsed = (System.currentTimeMillis() - current.startEpochMillis) / 1000
                    val remaining = max(current.totalSeconds - elapsed, 0)
                    val updated = current.copy(remainingSeconds = remaining)
                    _timerState.value = updated
                    if (remaining == 0L) {
                        handleTimerFinished(updated)
                    }
                }
            }
        }
    }

    private fun handleTimerFinished(state: TimerState) {
        viewModelScope.launch {
            NotificationHelper.showTimerDoneNotification(getApplication(), state.mode)
            cancelAlarm()
            if (state.mode == TimerMode.FOCUS) {
                timerEngine.incrementCycle()
            }
            val settings = settingsState.value
            if (settings.autoNext) {
                val nextMode = nextMode(state, settings)
                val nextSeconds = when (nextMode) {
                    TimerMode.FOCUS -> settings.focusSeconds
                    TimerMode.SHORT_BREAK -> settings.shortBreakSeconds
                    TimerMode.LONG_BREAK -> settings.longBreakSeconds
                }
                timerEngine.updateMode(nextMode, nextSeconds)
                timerEngine.start(nextMode, nextSeconds, state.cycleCount)
                scheduleAlarm(nextSeconds)
            } else {
                timerEngine.stop()
            }
        }
    }

    private fun nextMode(state: TimerState, settings: SettingsState): TimerMode {
        return when (state.mode) {
            TimerMode.FOCUS -> {
                val nextCount = state.cycleCount + 1
                if (nextCount % settings.longBreakInterval == 0) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK
            }
            TimerMode.SHORT_BREAK, TimerMode.LONG_BREAK -> TimerMode.FOCUS
        }
    }

    private fun scheduleAlarm(seconds: Long) {
        val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMillis = System.currentTimeMillis() + seconds * 1000
        val pendingIntent = alarmIntent()
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    private fun cancelAlarm() {
        val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(alarmIntent())
    }

    private fun alarmIntent(): PendingIntent {
        val intent = Intent(getApplication(), PomodoroAlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            getApplication(),
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
