package com.pomodorofocus.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgeDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pomodorofocus.app.R
import com.pomodorofocus.app.TimerAction
import com.pomodorofocus.app.data.DEFAULT_BG_RES
import com.pomodorofocus.app.data.TimerMode
import com.pomodorofocus.app.data.TimerState
import com.pomodorofocus.app.ui.components.AdBanner
import com.pomodorofocus.app.ui.components.CircularCountdownRing
import com.pomodorofocus.app.ui.components.RotaryNumberPicker
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    timerState: TimerState,
    onTimerAction: (TimerAction) -> Unit,
    onRecordSession: (Long) -> Unit
) {
    val context = LocalContext.current
    val formattedTime = formatTime(timerState.remainingSeconds)
    val progress = if (timerState.totalSeconds > 0) {
        timerState.remainingSeconds.toFloat() / timerState.totalSeconds.toFloat()
    } else 0f

    var showPicker by remember { mutableStateOf(false) }
    var showLockDialog by remember { mutableStateOf(false) }

    LaunchedEffect(timerState.remainingSeconds, timerState.mode) {
        if (timerState.mode == TimerMode.FOCUS && timerState.remainingSeconds == 0L) {
            onRecordSession(timerState.totalSeconds)
        }
    }

    LaunchedEffect(timerState.remainingSeconds, timerState.lockedMode) {
        if (timerState.remainingSeconds == 0L && timerState.lockedMode) {
            requestScreenPinning(context, false, onTimerAction)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val bgResId = remember(DEFAULT_BG_RES) {
            context.resources.getIdentifier(DEFAULT_BG_RES, "drawable", context.packageName)
        }
        if (bgResId != 0) {
            Image(
                painter = painterResource(id = bgResId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = timerState.mode.name.replace("_", " "),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { showLockDialog = true }) {
                    Text(text = context.getString(R.string.screen_lock))
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    CircularCountdownRing(
                        progress = progress,
                        modifier = Modifier.size(260.dp)
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.clickable { showPicker = true }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (timerState.lockedMode) {
                    Badge(
                        containerColor = BadgeDefaults.containerColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .padding(4.dp)
                    ) {
                        Text(text = context.getString(R.string.locked_mode))
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = {
                        if (!timerState.isRunning && timerState.remainingSeconds == timerState.totalSeconds) {
                            onTimerAction(TimerAction.Start)
                        } else if (timerState.isRunning) {
                            onTimerAction(TimerAction.Pause)
                        } else {
                            onTimerAction(TimerAction.Resume)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    val label = when {
                        timerState.isRunning -> context.getString(R.string.pause)
                        timerState.remainingSeconds == timerState.totalSeconds -> context.getString(R.string.start_focus)
                        else -> context.getString(R.string.resume)
                    }
                    Text(text = label, color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { onTimerAction(TimerAction.Stop) }) {
                    Text(text = context.getString(R.string.finish))
                }
                Spacer(modifier = Modifier.height(16.dp))
                AdBanner(modifier = Modifier.fillMaxWidth())
            }
        }
    }

    if (showPicker) {
        ModalBottomSheet(
            onDismissRequest = { showPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Text(
                text = context.getString(R.string.settings_focus),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            val values = remember { (5..90).toList() }
            RotaryNumberPicker(
                values = values,
                selectedValue = (timerState.totalSeconds / 60).toInt(),
                onSelected = { selected ->
                    onTimerAction(TimerAction.SetCustomMinutes(selected))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            )
        }
    }

    if (showLockDialog) {
        AlertDialog(
            onDismissRequest = { showLockDialog = false },
            title = { Text(text = context.getString(R.string.lock_dialog_title)) },
            text = { Text(text = context.getString(R.string.lock_dialog_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showLockDialog = false
                    requestScreenPinning(context, true, onTimerAction)
                }) {
                    Text(text = context.getString(R.string.lock_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLockDialog = false }) {
                    Text(text = context.getString(R.string.lock_dialog_cancel))
                }
            }
        )
    }
}

private fun formatTime(seconds: Long): String {
    val safeSeconds = max(seconds, 0)
    val minutes = safeSeconds / 60
    val remaining = safeSeconds % 60
    return String.format("%02d:%02d", minutes, remaining)
}

private fun requestScreenPinning(
    context: Context,
    enable: Boolean,
    onTimerAction: (TimerAction) -> Unit
) {
    val activity = context as? Activity ?: return
    if (enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            activity.startLockTask()
            onTimerAction(TimerAction.SetLockedMode(true))
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.stopLockTask()
            onTimerAction(TimerAction.SetLockedMode(false))
        }
    }
}
