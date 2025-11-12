package com.griffith.perfectclock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

data class Lap(val lapNumber: Int, val lapDuration: Long, val lapFinish: Long)

@Composable
fun StopwatchScreen() {
    var isRunning by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(0L) }
    var stopTime by remember { mutableStateOf(0L) }
    var elapsedTime by remember { mutableStateOf(0L) }
    var lastLapTime by remember { mutableStateOf(0L) }
    
    // State to force recomposition for the current lap display
    var lapUpdateTick by remember { mutableStateOf(0L) } 
    
    val laps = remember { mutableStateListOf<Lap>() }
    var currentLap by remember { mutableStateOf(Lap(1, 0, 0)) }

    // Define standard and 1.5x height
    val standardHeight = 48.dp
    val elevatedHeight = standardHeight * 1.5f // 1.5 times height

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isActive && isRunning) {
                val currentTime = System.currentTimeMillis()
                elapsedTime = currentTime - startTime
                
                // DYNAMIC UPDATE: Update currentLap by creating a NEW Lap object
                currentLap = currentLap.copy(
                    lapDuration = currentTime - lastLapTime,
                    lapFinish = elapsedTime
                )
                
                lapUpdateTick++ 
                
                delay(10L) // Update every 10 milliseconds
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Time display
        Text(
            text = formatTime(elapsedTime), 
            fontSize = 60.sp,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
        )
        
        // Buttons Row 1: Start/Stop (2x width, 1.5x height) and Reset (1x width, 1.5x height)
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 8.dp)
        ) {
            

            Button(
                onClick = {
                    isRunning = false
                    startTime = 0L
                    stopTime = 0L
                    elapsedTime = 0L
                    laps.clear()
                    currentLap = Lap(1, 0, 0)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFed6d8b)),
                modifier = Modifier
                    .weight(1f)
                    .height(elevatedHeight) // height x 1.5
            ) {
                Text("Reset")
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Start/Stop Btn 2x width, 1.5x height
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
                            lastLapTime = startTime
                            currentLap = Lap(1, 0, 0) // Initialize lap
                        } else {
                            // resume: shift startTime forward 
                            val pausedDuration = System.currentTimeMillis() - stopTime
                            startTime += pausedDuration
                            // Adjust lastLapTime
                            lastLapTime += pausedDuration
                        }
                    }
                    isRunning = !isRunning
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFf0b67a) else Color(0xFFbd98eb)
                ),
                modifier = Modifier
                    .weight(2f) 
                    .height(elevatedHeight) // <-- Increased height to 1.5x
            ) {
                Text(if (isRunning) "Stop" else "Start")
            }
        }

        // Lap Button 
        if (currentLap.lapNumber > 0 && (isRunning || elapsedTime > 0)) {
            Button(
                onClick = {
                    val currentTime = System.currentTimeMillis()
                    
                    // Finalize the current lap's values and add it to the list
                    laps.add(currentLap.copy(
                        lapDuration = currentTime - lastLapTime,
                        lapFinish = elapsedTime
                    ))
                    
                    // Start new lap
                    lastLapTime = currentTime
                    currentLap = Lap(laps.size + 1, 0, 0)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90a4ae)), 
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(standardHeight) 
            ) {
                Text("Lap")
            }
        }

        // Laps Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Lap", fontWeight = FontWeight.Bold)
            Text(text = "Lap Time", fontWeight = FontWeight.Bold)
            Text(text = "Total Time", fontWeight = FontWeight.Bold)
        }

        // Newest lap at the top
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            reverseLayout = false
        ) {
            // Display the dynamically updating current lap first
            if (currentLap.lapNumber > 0) {
                item(key = "current_lap") { 
                    LapRow(currentLap, isRunning = isRunning, isCurrent = true)
                }
            }
            
            // Display finalized laps 
            items(laps.reversed(), key = { it.lapNumber }) { lap ->
                LapRow(lap, isRunning = false, isCurrent = false)
            }
        }
    }
}

@Composable
private fun LapRow(lap: Lap, isRunning: Boolean, isCurrent: Boolean) {
    val backgroundColor = if (isCurrent) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }
    
    val textColor = if (isRunning && isCurrent) Color(0xFFbd98eb) else Color.White 
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Lap ${lap.lapNumber.toString().padStart(2, '0')}", color = textColor)
            Text(text = formatTime(lap.lapDuration), color = textColor, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
            Text(text = formatTime(lap.lapFinish), color = textColor)
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
