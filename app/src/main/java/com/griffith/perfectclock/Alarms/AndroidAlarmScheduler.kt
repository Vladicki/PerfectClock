package com.griffith.perfectclock.Alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class AndroidAlarmScheduler(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val TAG = "AlarmScheduler"

    override fun schedule(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", alarm.message)
            putExtra("EXTRA_ALARM_ID", alarm.id)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(), // Use hash code of the alarm ID for request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = LocalDateTime.now()
        val alarmDateTime = LocalDateTime.of(LocalDate.now(), alarm.time)

        // If the alarm time is in the past for today, schedule it for tomorrow
        val triggerDateTime = if (alarmDateTime.isBefore(now)) {
            alarmDateTime.plusDays(1)
        } else {
            alarmDateTime
        }

        val triggerTime = triggerDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000L

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                Log.e(TAG, "Cannot schedule exact alarms. Missing SCHEDULE_EXACT_ALARM permission.")
                // Optionally, inform the user or request permission
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
        Log.d(TAG, "Alarm scheduled: ID=${alarm.id}, Time=${triggerDateTime}, Message=${alarm.message}")
    }

    override fun cancel(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", alarm.message)
            putExtra("EXTRA_ALARM_ID", alarm.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Alarm cancelled: ID=${alarm.id}")
    }

    override fun snooze(alarm: Alarm) {
        // Cancel the current alarm
        cancel(alarm)

        // Schedule a new alarm for 5 minutes from now
        val snoozeTime = System.currentTimeMillis() + 5 * 60 * 1000
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", alarm.message)
            putExtra("EXTRA_ALARM_ID", alarm.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingIntent
                )
            } else {
                Log.e(TAG, "Cannot schedule exact alarms for snooze. Missing SCHEDULE_EXACT_ALARM permission.")
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                snoozeTime,
                pendingIntent
            )
        }
        Log.d(TAG, "Alarm snoozed: ID=${alarm.id} for 5 minutes.")
    }
}
