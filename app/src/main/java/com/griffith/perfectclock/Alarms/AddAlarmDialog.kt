package com.griffith.perfectclock

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
import java.time.LocalTime
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmDialog(
    onDismissRequest: () -> Unit,
    onAddAlarm: (Alarm) -> Unit,
    gridConfig: GridLayoutConfig,
    alarms: List<Alarm> // Pass existing alarms to check for available grid spots
) {
    val timePickerState = rememberTimePickerState()
    var useOnce by remember { mutableStateOf(true) }
    var usingDial by remember { mutableStateOf(true) }
    val haptic = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Add Alarm") },
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
                        var newX = 0
                        var newY = 0
                        var found = false

                        for (y in 0 until gridConfig.rows) {
                            for (x in 0 until gridConfig.columns) {
                                if (!alarms.any { it.x == x && it.y == y }) {
                                    newX = x
                                    newY = y
                                    found = true
                                    break
                                }
                            }
                            if (found) break
                        }

                        onAddAlarm(
                            Alarm(
                                id = UUID.randomUUID().toString(),
                                time = LocalTime.of(timePickerState.hour, timePickerState.minute),
                                useOnce = useOnce,
                                x = newX,
                                y = newY
                            )
                        )
                        onDismissRequest()
                    }) {
                        Text("Add")
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
