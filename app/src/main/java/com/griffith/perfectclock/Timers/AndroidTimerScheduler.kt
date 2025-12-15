package com.griffith.perfectclock.Timers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.os.Build
import com.griffith.perfectclock.Alarms.AlarmScheduler
import com.griffith.perfectclock.Timer

class AndroidTimerScheduler(
    private val context: Context
) : TimerScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val TAG = "TimerScheduler"

    override fun schedule(timer: Timer, triggerAtMillis: Long) {
        val intent = Intent(context, TimerReceiver::class.java).apply {
            putExtra("EXTRA_TIMER_ID", timer.id)
            putExtra("EXTRA_TIMER_DURATION", timer.initialSeconds)
            putExtra("EXTRA_TIMER_LABEL", timer.label)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timer.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                Log.e(TAG, "Cannot schedule exact timers. Missing SCHEDULE_EXACT_ALARM permission.")
                // Optionally, inform the user or request permission
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
        Log.d(TAG, "Timer scheduled: ID=${timer.id}, triggerAtMillis=$triggerAtMillis")
    }

    override fun cancel(timer: Timer) {
        val intent = Intent(context, TimerReceiver::class.java).apply {
            putExtra("EXTRA_TIMER_ID", timer.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timer.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Timer cancelled: ID=${timer.id}")
    }
}
