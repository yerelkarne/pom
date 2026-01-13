package com.pomodorofocus.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pomodorofocus.app.StatsState
import com.pomodorofocus.app.R
import androidx.compose.ui.res.stringResource

@Composable
fun StatsScreen(statsState: StatsState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = stringResource(R.string.stats_today_count), style = MaterialTheme.typography.titleLarge)
        Text(
            text = "${statsState.todayCount} pomodoro",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(text = stringResource(R.string.stats_today_focus) + ": ${statsState.todaySeconds / 60} dk")

        Text(
            text = stringResource(R.string.stats_last_7),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            items(statsState.lastSevenDays.toList()) { (date, seconds) ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = date, style = MaterialTheme.typography.bodyLarge)
                        Text(text = "${seconds / 60} dk", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
