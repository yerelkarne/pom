package com.leosoft.pomodoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leosoft.pomodoro.data.AdManager
import com.leosoft.pomodoro.ui.PomodoroAppRoot
import com.leosoft.pomodoro.ui.theme.PomodoroTheme

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val timerViewModel: TimerViewModel by viewModels()
    private val adManager by lazy { AdManager(this.application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PomodoroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val settingsState by mainViewModel.settingsState.collectAsStateWithLifecycle()
                    val timerState by timerViewModel.timerState.collectAsStateWithLifecycle()
                    val statsState by mainViewModel.statsState.collectAsStateWithLifecycle()
                    PomodoroAppRoot(
                        settingsState = settingsState,
                        timerState = timerState,
                        statsState = statsState,
                        onSettingsAction = mainViewModel::handleSettingsAction,
                        onTimerAction = timerViewModel::handleTimerAction,
                        onPermissionResult = mainViewModel::onNotificationPermissionResult,
                        onRecordSession = mainViewModel::recordSession
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        adManager.showAppOpenAd(this)
    }
}
