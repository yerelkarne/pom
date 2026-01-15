package com.leosoft.pomodoro

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.android.gms.ads.MobileAds
import com.leosoft.pomodoro.alarm.DailyReminderScheduler
import com.leosoft.pomodoro.alarm.NotificationHelper

class PomodoroApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        createNotificationChannel()
        DailyReminderScheduler.scheduleDailyReminder(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationHelper.CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
