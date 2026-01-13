package com.pomodorofocus.app.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.pomodorofocus.app.MainActivity
import com.pomodorofocus.app.R
import com.pomodorofocus.app.data.TimerMode

object NotificationHelper {
    const val CHANNEL_ID = "pomodoro_channel"
    private const val NOTIFICATION_ID = 2001
    private const val DAILY_NOTIFICATION_ID = 2002

    fun showTimerDoneNotification(context: Context, mode: TimerMode?) {
        val title = context.getString(R.string.notification_title)
        val body = when (mode) {
            TimerMode.FOCUS -> context.getString(R.string.notification_focus_done)
            TimerMode.SHORT_BREAK -> context.getString(R.string.notification_short_break_done)
            TimerMode.LONG_BREAK -> context.getString(R.string.notification_long_break_done)
            null -> context.getString(R.string.notification_generic)
        }
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun showDailyReminderNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.daily_reminder))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(DAILY_NOTIFICATION_ID, notification)
    }
}
