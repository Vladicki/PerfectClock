package com.griffith.perfectclock

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun StopwatchesScreen() {
    var isRunning by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(0L) }
    var pauseOffset by remember { mutableStateOf(0L) }
    var elapsedTime by remember { mutableStateOf(0L) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isActive) {
                delay(1L) // Update every 1ms for millisecond accuracy
                elapsedTime = pauseOffset + (System.currentTimeMillis() - startTime)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatTime(elapsedTime),
                fontSize = 60.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = {
                    if (isRunning) {
                        pauseOffset += System.currentTimeMillis() - startTime // Accumulate paused time
                    } else {
                        startTime = System.currentTimeMillis() // Set new start time for this segment
                    }
                    isRunning = !isRunning
                },
                colors = if (isRunning) ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)) else ButtonDefaults.buttonColors(),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(text = if (isRunning) "Stop" else "Start")
            }

            if (elapsedTime > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        isRunning = false
                        startTime = 0L
                        pauseOffset = 0L
                        elapsedTime = L
                    },
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(text = "Reset")
                }
            }
        }
    }
}

fun formatTime(milliseconds: Long): String {
    val minutes = (milliseconds / (1000 * 60)) % 60
    val seconds = (milliseconds / 1000) % 60
    val ms = (milliseconds % 1000) / 10 // Two digits for milliseconds
    return String.format("%02d:%02d:%02d", minutes, seconds, ms)
}