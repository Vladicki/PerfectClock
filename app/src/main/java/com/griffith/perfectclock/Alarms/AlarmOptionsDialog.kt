package com.griffith.perfectclock.Alarms

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.griffith.perfectclock.GridLayoutConfig
import java.time.LocalTime
import java.util.UUID
import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.ui.platform.LocalContext

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmOptionsDialog(
    onDismissRequest: () -> Unit,
    onUpdateAlarm: (Alarm) -> Unit,
    alarm: Alarm
) {
    val timePickerState = rememberTimePickerState(initialHour = alarm.time.hour, initialMinute = alarm.time.minute)
    var useOnce by remember { mutableStateOf(alarm.useOnce) }
    var usingDial by remember { mutableStateOf(true) }
    var vibrate by remember { mutableStateOf(alarm.vibrate) }
    var ringtoneUri by remember { mutableStateOf(alarm.ringtoneUri?.let { Uri.parse(it) }) }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val ringtoneTitle by remember(ringtoneUri) {
        derivedStateOf {
            if (ringtoneUri == null) {
                val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                RingtoneManager.getRingtone(context, defaultRingtoneUri)?.getTitle(context) ?: "Default"
            } else {
                RingtoneManager.getRingtone(context, ringtoneUri)?.getTitle(context) ?: "Unknown Ringtone"
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                ringtoneUri = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Edit Alarm") },
        text = {
            Column {
                Spacer(modifier = Modifier.height(12.dp))

                // Dial or Input picker
                if (usingDial) {
                    var lastHour by remember { mutableStateOf(timePickerState.hour) }
                    var lastMinute by remember { mutableStateOf(timePickerState.minute) }

                    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                        if (lastHour != timePickerState.hour || lastMinute != timePickerState.minute) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            lastHour = timePickerState.hour
                            lastMinute = timePickerState.minute
                        }
                    }
                    TimePicker(state = timePickerState)
                } else {
                    TimeInput(state = timePickerState)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = !useOnce, onCheckedChange = { useOnce = !it })
                    Text("Use Once")
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vibrate")
                    Switch(checked = vibrate, onCheckedChange = { vibrate = it })
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(onClick = {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                    launcher.launch(intent)
                }) {
                    Text("Ringtone")
                }
                Text(text = ringtoneTitle)
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 6.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle keyboard/dial
                IconButton(onClick = { usingDial = !usingDial }) {
                    Icon(
                        imageVector = if (usingDial) Icons.Default.Keyboard else Icons.Default.AccessTime,
                        contentDescription = "Toggle Input Mode"
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Cancel
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }

                    TextButton(onClick = {
                        onUpdateAlarm(
                            alarm.copy(
                                time = LocalTime.of(timePickerState.hour, timePickerState.minute),
                                useOnce = useOnce,
                                ringtoneUri = ringtoneUri.toString(),
                                vibrate = vibrate
                            )
                        )
                        onDismissRequest()
                    }) {
                        Text("Save")
                    }
                }
            }
        },
        dismissButton = {
            // TextButton(onClick = { showDialog = false }) {
            //     Text("Cancel")
            // }
        }
    )
}
