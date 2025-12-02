package com.griffith.perfectclock

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
import com.griffith.perfectclock.ui.theme.PerfectClockTheme

class AlarmPopupActivity : ComponentActivity() {

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

        setContent {
            PerfectClockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShakeItOffDialog(
                        onShakeDismiss = {
                            // Logic to dismiss the alarm, potentially stop sound/vibration
                            // and finish this activity
                            finish()
                        },
                        onManualDismiss = {
                            // Logic to dismiss the alarm, potentially stop sound/vibration
                            // and finish this activity
                            finish()
                        }
                    )
                }
            }
        }
    }
}
