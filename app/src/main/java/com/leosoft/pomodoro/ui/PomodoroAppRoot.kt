package com.leosoft.pomodoro.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.leosoft.pomodoro.SettingsAction
import com.leosoft.pomodoro.StatsState
import com.leosoft.pomodoro.TimerAction
import com.leosoft.pomodoro.data.SettingsState
import com.leosoft.pomodoro.data.TimerState
import com.leosoft.pomodoro.ui.screens.HomeScreen
import com.leosoft.pomodoro.ui.screens.PermissionOnboarding
import com.leosoft.pomodoro.ui.screens.SettingsScreen
import com.leosoft.pomodoro.ui.screens.StatsScreen

private enum class BottomNavItem(val route: String, val label: String) {
    Home("home", "Ana Sayfa"),
    Stats("stats", "İstatistik"),
    Settings("settings", "Ayarlar")
}

@Composable
fun PomodoroAppRoot(
    settingsState: SettingsState,
    timerState: TimerState,
    statsState: StatsState,
    onSettingsAction: (SettingsAction) -> Unit,
    onTimerAction: (TimerAction) -> Unit,
    onPermissionResult: (Boolean) -> Unit,
    onRecordSession: (Long) -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    var showedPermissionOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onPermissionResult(granted)
    }

    LaunchedEffect(settingsState.notificationPermissionGranted) {
        if (!settingsState.notificationPermissionGranted && !showedPermissionOnce) {
            showedPermissionOnce = true
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    timerState = timerState,
                    onTimerAction = onTimerAction,
                    onRecordSession = onRecordSession
                )
            }
            composable(BottomNavItem.Stats.route) {
                StatsScreen(statsState = statsState)
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen(settingsState = settingsState, onSettingsAction = onSettingsAction)
            }
            composable("permission") {
                PermissionOnboarding(onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                })
            }
        }
    }

    if (!settingsState.notificationPermissionGranted && showedPermissionOnce) {
        PermissionOnboarding(onRequestPermission = {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        })
    }
}

@Composable
private fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar {
        BottomNavItem.values().forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { navController.navigate(item.route) },
                label = { Text(item.label) },
                icon = {}
            )
        }
    }
}
