package com.griffith.perfectclock.Timers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.griffith.perfectclock.R
import android.util.Log
import com.griffith.perfectclock.Alarms.AlarmReceiver

class TimerReceiver: BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val ACTION_DISMISS_TIMER = "com.griffith.perfectclock.DISMISS_TIMER"
        private const val TAG = "TimerReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive called. Action: ${intent.action}")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val timerId = intent.getStringExtra("EXTRA_TIMER_ID")
        val duration = intent.getIntExtra("EXTRA_TIMER_DURATION", 0)
        val label = intent.getStringExtra("EXTRA_TIMER_LABEL")

        if (timerId == null) {
            Log.e(TAG, "TimerId is null.")
            return
        }

        when (intent.action) {
            ACTION_DISMISS_TIMER -> {
                Log.d(TAG, "Received DISMISS_TIMER action for ID: $timerId")
                notificationManager.cancel(timerId.hashCode())
            }
            else -> {
                Log.d(TAG, "Timer finished: ID: $timerId")

                createNotificationChannel(context, notificationManager)

                val dismissIntent = Intent(context, TimerReceiver::class.java).apply {
                    action = ACTION_DISMISS_TIMER
                    putExtra("EXTRA_TIMER_ID", timerId)
                }
                val dismissPendingIntent = PendingIntent.getBroadcast(
                    context,
                    timerId.hashCode(),
                    dismissIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val contentText = if (label.isNullOrEmpty()) "Your timer for ${formatDuration(duration)} is done." else "Your timer '$label' for ${formatDuration(duration)} is done."

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Timer Finished")
                    .setContentText(contentText)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)
                    .addAction(0, "Dismiss", dismissPendingIntent)
                    .build()

                notificationManager.notify(timerId.hashCode(), notification)
                Log.d(TAG, "Notification shown for ID: $timerId")
            }
        }
    }

    private fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timers",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for timer notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, secs)
            minutes > 0 -> String.format("%d:%02d", minutes, secs)
            else -> String.format("%d seconds", secs)
        }
    }
}
