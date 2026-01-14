package com.leosoft.pomodoro.data

import kotlin.time.Duration.Companion.minutes

const val DEFAULT_BG_RES = "bg_pomodoro"

data class SettingsState(
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val longBreakInterval: Int = 4,
    val autoNext: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val notificationPermissionGranted: Boolean = true
) {
    val focusSeconds: Long = focusMinutes.minutes.inWholeSeconds
    val shortBreakSeconds: Long = shortBreakMinutes.minutes.inWholeSeconds
    val longBreakSeconds: Long = longBreakMinutes.minutes.inWholeSeconds
}
