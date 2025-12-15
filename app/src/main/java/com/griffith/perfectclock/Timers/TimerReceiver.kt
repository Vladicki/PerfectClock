package com.griffith.perfectclock.Timers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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

                // In a real app, you would create a notification channel here,
                // similar to the AlarmReceiver. For simplicity, we assume it's created elsewhere.

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

                val notification = NotificationCompat.Builder(context, AlarmReceiver.CHANNEL_ID) // Re-using alarm channel for now
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Timer Finished")
                    .setContentText("Your timer is done.")
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
}
