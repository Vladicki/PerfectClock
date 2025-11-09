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
    var startTime by remember { mutableStateOf(0L) } // time when started or resumed
    var stopTime by remember { mutableStateOf(0L) }  // time when stopped/paused
    var elapsedTime by remember { mutableStateOf(0L) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isActive && isRunning) {
                elapsedTime = System.currentTimeMillis() - startTime
                delay(10L)
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
            // Time display
            Text(
                text = formatTime(elapsedTime),
                fontSize = 60.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Start / Stop button
            Button(
                onClick = {
                    if (isRunning) {
                        // STOP
                        stopTime = System.currentTimeMillis()
                    } else {
                        // START or RESUME
                        if (startTime == 0L) {
                            // first start
                            startTime = System.currentTimeMillis()
                        } else {
                            // resume: shift startTime forward by paused duration
                            val pausedDuration = System.currentTimeMillis() - stopTime
                            startTime += pausedDuration
                        }
                    }
                    isRunning = !isRunning
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFf0b67a) else Color(0xFFbd98eb)
                ),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(if (isRunning) "Stop" else "Start")
            }

            // Reset button
            if (elapsedTime > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        isRunning = false
                        startTime = 0L
                        stopTime = 0L
                        elapsedTime = 0L
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFed6d8b)),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Reset")
                }
            }
        }
    }
}

// Format milliseconds to MM:SS:CS
fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = (totalSeconds / 60) % 60
    val seconds = totalSeconds % 60
    val hundredths = (milliseconds % 1000) / 10
    return String.format("%02d:%02d:%02d", minutes, seconds, hundredths)
}
