package com.griffith.perfectclock

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShakeItOffDialog(
    onShakeDismiss: () -> Unit,
    onManualDismiss: () -> Unit
) {
    val context = LocalContext.current
    val shakeDetector = remember { ShakeDetector(context) { onShakeDismiss() } }

    DisposableEffect(Unit) {
        shakeDetector.start()
        onDispose {
            shakeDetector.stop()
        }
    }

    Dialog(
        onDismissRequest = onManualDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Shake It Off!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Shake your phone to dismiss the alarm/timer.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onManualDismiss) {
                    Text("Dismiss Manually")
                }
            }
        }
    }
}
