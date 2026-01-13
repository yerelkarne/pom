package com.pomodorofocus.app.data

import kotlin.time.Duration.Companion.minutes

enum class TimerMode {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK
}

data class TimerState(
    val mode: TimerMode = TimerMode.FOCUS,
    val isRunning: Boolean = false,
    val totalSeconds: Long = 25.minutes.inWholeSeconds,
    val remainingSeconds: Long = 25.minutes.inWholeSeconds,
    val cycleCount: Int = 0,
    val lockedMode: Boolean = false,
    val startEpochMillis: Long = 0L
)
