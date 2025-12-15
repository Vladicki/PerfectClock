package com.griffith.perfectclock.Alarms

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.griffith.perfectclock.Alarms.Alarm
import com.griffith.perfectclock.Alarms.AlarmDialog
import com.griffith.perfectclock.Alarms.AlarmStorage
import com.griffith.perfectclock.Alarms.AndroidAlarmScheduler
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import com.griffith.perfectclock.ui.theme.PerfectClockTheme

class AlarmPopupActivity : ComponentActivity() {

    private var ringtone: Ringtone? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure activity shows over lock screen and turns on screen
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "Alarm"
        val alarmId = intent.getStringExtra("EXTRA_ALARM_ID") ?: return
        val ringtoneUriString = intent.getStringExtra("EXTRA_RINGTONE_URI")

        val ringtoneUri = ringtoneUriString?.let { Uri.parse(it) } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtone = RingtoneManager.getRingtone(this, ringtoneUri)
        ringtone?.play()

        setContent {
            val alarmScheduler = AndroidAlarmScheduler(this)
            val alarmStorage = AlarmStorage(this)
            val alarms = alarmStorage.loadAlarms()
            val alarm = alarms.find { it.id == alarmId }

            if (alarm != null) {
                AlarmDialog(
                    alarmTime = alarm.getTimeString(),
                    message = message,
                    onDismiss = {
                        ringtone?.stop()
                        val updatedAlarms = alarms.toMutableList()
                        val index = updatedAlarms.indexOfFirst { it.id == alarm.id }
                        if (index != -1) {
                            updatedAlarms[index] = alarm.copy(isEnabled = false)
                            alarmStorage.saveAlarms(updatedAlarms)
                        }
                        alarmScheduler.cancel(alarm)
                        finish()
                    },
                    onSnooze = {
                        ringtone?.stop()
                        alarmScheduler.snooze(alarm)
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtone?.stop()
    }
}
