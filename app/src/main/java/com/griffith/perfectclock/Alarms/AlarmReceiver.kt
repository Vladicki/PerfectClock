import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.griffith.perfectclock.R
import android.util.Log
import com.griffith.perfectclock.AlarmPopupActivity

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
        if (context == null || alarmId == null) {
            Log.e(TAG, "Context or alarmId is null. Context: $context, AlarmId: $alarmId")
            return
        }

        when (intent.action) {
            ACTION_STOP_ALARM -> {
                Log.d(TAG, "Received STOP_ALARM action for ID: $alarmId")
                notificationManager.cancel(alarmId.hashCode())
                // Optionally stop any playing sound/vibration here if it were started in a service
            }
            else -> {
                // This is the alarm trigger
                Log.d(TAG, "Alarm triggered: $message for ID: $alarmId")

                createNotificationChannel(context, notificationManager)

                // Start AlarmPopupActivity
                val popupIntent = Intent(context, AlarmPopupActivity::class.java).apply {
                    putExtra("EXTRA_MESSAGE", message)
                    putExtra("EXTRA_ALARM_ID", alarmId)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                context.startActivity(popupIntent)
                Log.d(TAG, "Started AlarmPopupActivity for ID: $alarmId")

                val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

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

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground) // Use an appropriate icon
                    .setContentTitle("Alarm")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setSound(alarmSound)
                    .setAutoCancel(true) // Dismiss notification when clicked
                    .addAction(0, "Stop", stopPendingIntent) // Add stop button
                    .build()

                notificationManager.notify(alarmId.hashCode(), notification)
                Log.d(TAG, "Notification shown for ID: $alarmId")
            }
        }
    }

    private fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
        // ... (existing code)
    }
}

