package com.griffith.perfectclock.Alarms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AlarmDialog(
    alarmTime: String,
    message: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Don't dismiss on outside click */ },
        title = { Text(text = "Alarm - $alarmTime") },
        text = { Text(text = message) },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onSnooze) {
                    Text("Snooze")
                }
                Button(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        }
    )
}
