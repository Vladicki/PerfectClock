package com.griffith.perfectclock.Alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.griffith.perfectclock.R
import android.util.Log
import com.griffith.perfectclock.PerfectClockApp
import com.griffith.perfectclock.Alarms.AlarmStorage

class AlarmReceiver: BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "alarm_channel"
        const val ACTION_STOP_ALARM = "com.griffith.perfectclock.STOP_ALARM"
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive called. Action: ${intent?.action}")

        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val message = intent?.getStringExtra("EXTRA_MESSAGE")
        val alarmId = intent?.getStringExtra("EXTRA_ALARM_ID")

        // Ensure context and alarmId are not null
        if (alarmId == null) {
            Log.e(TAG, "AlarmId is null. Context: $context, AlarmId: $alarmId")
            return
        }

        when (intent.action) {
            ACTION_STOP_ALARM -> {
                Log.d(TAG, "Received STOP_ALARM action for ID: $alarmId")
                notificationManager.cancel(alarmId.hashCode())

                // Delete the alarm if it's a "Use Once" alarm
                val alarmStorage = AlarmStorage(context)
                val alarms = alarmStorage.loadAlarms()
                val alarm = alarms.find { it.id == alarmId }
                if (alarm != null && alarm.useOnce) {
                    val updatedAlarms = alarms.filter { it.id != alarmId }
                    alarmStorage.saveAlarms(updatedAlarms)
                    Log.d(TAG, "Deleted 'Use Once' alarm with ID: $alarmId")
                }
            }
            else -> {
                // This is the alarm trigger
                Log.d(TAG, "Alarm triggered: $message for ID: $alarmId")

                val alarmStorage = AlarmStorage(context)
                val alarms = alarmStorage.loadAlarms()
                val alarm = alarms.find { it.id == alarmId }

                if (alarm == null) {
                    Log.e(TAG, "Alarm not found in storage for ID: $alarmId")
                    return
                }

                createNotificationChannel(context, notificationManager)

                // Start AlarmPopupActivity
                val popupIntent = Intent(context, AlarmPopupActivity::class.java).apply {
                    putExtra("EXTRA_MESSAGE", message)
                    putExtra("EXTRA_ALARM_ID", alarmId)
                    putExtra("EXTRA_RINGTONE_URI", alarm.ringtoneUri)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                context.startActivity(popupIntent)
                Log.d(TAG, "Started AlarmPopupActivity for ID: $alarmId")

                // Intent for stopping the alarm
                val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
                    action = ACTION_STOP_ALARM
                    putExtra("EXTRA_ALARM_ID", alarmId) // Pass alarmId to identify which alarm to stop if multiple
                }
                val stopPendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarmId.hashCode(), // Use alarmId hash for uniqueness
                    stopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val defaultAlarmUri = getDefaultAlarmUri()

                val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground) // Use an appropriate icon
                    .setContentTitle("Alarm - ${alarm.getTimeString()}")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(false) // Dismiss notification when clicked
                    .addAction(0, "Dismiss", stopPendingIntent) // Add stop button

                defaultAlarmUri?.let { uri ->
                notificationBuilder.setSound(uri)
                }

                if (alarm.vibrate) {
                    notificationBuilder.setVibrate(longArrayOf(0, 500, 500, 500))
                }

                if (!PerfectClockApp.isAppInForeground) {
                    val notification = notificationBuilder.build()
                    notificationManager.notify(alarmId.hashCode(), notification)
                    Log.d(TAG, "Notification shown for ID: $alarmId")
                }
            }
        }
    }

    private fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultAlarmUri = getDefaultAlarmUri()

        // Define AudioAttributes for the alarm sound
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM) // Key line to ensure it behaves like an alarm
            .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for alarm notifications"
                defaultAlarmUri?.let { uri ->
                setSound(uri, audioAttributes)
            }
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun getDefaultAlarmUri(): Uri? {
    // Get the URI for the default alarm sound
    return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    }

}

