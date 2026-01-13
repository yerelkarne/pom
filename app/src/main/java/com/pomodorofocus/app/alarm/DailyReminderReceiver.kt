package com.pomodorofocus.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        NotificationHelper.showDailyReminderNotification(context)
        DailyReminderScheduler.scheduleDailyReminder(context)
    }
}
