package com.pomodorofocus.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pomodorofocus.app.SettingsAction
import com.pomodorofocus.app.R
import com.pomodorofocus.app.data.SettingsState
import androidx.compose.ui.res.stringResource

@Composable
fun SettingsScreen(settingsState: SettingsState, onSettingsAction: (SettingsAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        DurationSettingRow(
            label = stringResource(R.string.settings_focus),
            value = settingsState.focusMinutes,
            onValueChange = { onSettingsAction(SettingsAction.UpdateFocus(it)) }
        )
        DurationSettingRow(
            label = stringResource(R.string.settings_short_break),
            value = settingsState.shortBreakMinutes,
            onValueChange = { onSettingsAction(SettingsAction.UpdateShortBreak(it)) }
        )
        DurationSettingRow(
            label = stringResource(R.string.settings_long_break),
            value = settingsState.longBreakMinutes,
            onValueChange = { onSettingsAction(SettingsAction.UpdateLongBreak(it)) }
        )
        DurationSettingRow(
            label = stringResource(R.string.settings_long_break_interval),
            value = settingsState.longBreakInterval,
            onValueChange = { onSettingsAction(SettingsAction.UpdateLongBreakInterval(it)) }
        )

        SwitchRow(
            label = stringResource(R.string.settings_auto_next),
            checked = settingsState.autoNext,
            onCheckedChange = { onSettingsAction(SettingsAction.UpdateAutoNext(it)) }
        )
        SwitchRow(
            label = stringResource(R.string.settings_sound),
            checked = settingsState.soundEnabled,
            onCheckedChange = { onSettingsAction(SettingsAction.UpdateSound(it)) }
        )
        SwitchRow(
            label = stringResource(R.string.settings_vibration),
            checked = settingsState.vibrationEnabled,
            onCheckedChange = { onSettingsAction(SettingsAction.UpdateVibration(it)) }
        )

        Text(
            text = stringResource(R.string.settings_background_note),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun DurationSettingRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            value = text,
            onValueChange = {
                text = it
                it.toIntOrNull()?.let { number -> onValueChange(number) }
            },
            modifier = Modifier.width(100.dp)
        )
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
