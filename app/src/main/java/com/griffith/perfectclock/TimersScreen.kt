package com.griffith.perfectclock

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.sp
import com.griffith.perfectclock.Timer
import com.griffith.perfectclock.TimerStorage
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimersScreen(
    gridConfig: GridLayoutConfig,
    timers: List<Timer>,
    onUpdateTimer: (Timer) -> Unit,
    onAddTimer: (Timer) -> Unit,
    onDeleteTimer: (Timer) -> Unit
) {
    val context = LocalContext.current
    val timerStorage = remember { TimerStorage(context) }

    var showDialog by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()
    var inputSeconds by remember { mutableStateOf("00") }

    LaunchedEffect(Unit) {
        // Reload timers if they change (e.g., from settings)
        // No need to reload gridConfig here as it's passed as a parameter
    }

    Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (timers.isEmpty()) {
                            Text(text = "Timers Screen", fontSize = 24.sp, modifier = Modifier.padding(16.dp))
                        }
        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridConfig.columns),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {                items(timers) { timer ->
                    TimerItem(
                        timer = timer,
                        onToggle = onUpdateTimer, // Use the new onUpdateTimer callback
                        onDelete = { onDeleteTimer(timer) }, // Use the new onDeleteTimer callback
                        showEdges = gridConfig.showEdges
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, "Add new timer.")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add New Timer") },
                text = {
                    Column {
                        TimeInput(state = timePickerState)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputSeconds,
                                onValueChange = { if (it.length <= 2) inputSeconds = it },
                                label = { Text("Seconds") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(90.dp) // Adjust width as needed
                            )
                            Text("s", modifier = Modifier.padding(start = 4.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        val selectedHours = timePickerState.hour
                        val selectedMinutes = timePickerState.minute
                        val selectedSeconds = inputSeconds.toIntOrNull() ?: 0

                        Text(
                            text = buildAnnotatedString {
                                append("Selected Time: ")
                                withStyle(style = SpanStyle(fontSize = 18.sp)) {
                                    append(String.format("%02d", selectedHours))
                                }
                                withStyle(style = SpanStyle(fontSize = 9.sp)) {
                                    append("h")
                                }
                                withStyle(style = SpanStyle(fontSize = 18.sp)) {
                                    append(String.format(":%02d", selectedMinutes))
                                }
                                withStyle(style = SpanStyle(fontSize = 9.sp)) {
                                    append("m")
                                }
                                withStyle(style = SpanStyle(fontSize = 18.sp)) {
                                    append(String.format(":%02d", selectedSeconds))
                                }
                                withStyle(style = SpanStyle(fontSize = 9.sp)) {
                                    append("s")
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        var useOnce by remember { mutableStateOf(true) }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = useOnce, onCheckedChange = { useOnce = it })
                            Text("Use Once")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val hours = timePickerState.hour
                        val minutes = timePickerState.minute
                        val seconds = inputSeconds.toIntOrNull() ?: 0
                        val totalSeconds = hours * 3600 + minutes * 60 + seconds

                        if (totalSeconds > 0) {
                            val useOnce = true
                            val newTimer = Timer(
                                id = UUID.randomUUID().toString(),
                                initialSeconds = totalSeconds,
                                remainingSeconds = totalSeconds,
                                isRunning = true, // Timer starts running on creation
                                isFinished = false,
                                useOnce = useOnce // Use the value from the checkbox
                            )
                            onAddTimer(newTimer) // Use the new onAddTimer callback
                            showDialog = false
                        }
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        val showGlobalDismissButton = timers.any { it.isFinished && !it.isDismissed }
        if (showGlobalDismissButton) {
            Button(
                onClick = {
                    val updatedTimers = timers.toMutableList()
                    val timersToProcess = updatedTimers.filter { it.isFinished && !it.isDismissed }
                    timersToProcess.forEach { timerToDismiss ->
                        val index = updatedTimers.indexOfFirst { it.id == timerToDismiss.id }
                        if (index != -1) {
                            if (timerToDismiss.useOnce) {
                                onDeleteTimer(timerToDismiss) // Use the new onDeleteTimer callback
                            } else {
                                onUpdateTimer(timerToDismiss.copy( // Use the new onUpdateTimer callback
                                    remainingSeconds = timerToDismiss.initialSeconds,
                                    isRunning = false,
                                    isFinished = false,
                                    isDismissed = true
                                ))
                            }
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFed6d8b))
            ) {
                Text("DISMISS")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimerItem(timer: Timer, onToggle: (Timer) -> Unit, onDelete: () -> Unit, showEdges: Boolean) {
    var currentRemainingSeconds by remember(timer.id) { mutableStateOf(timer.remainingSeconds) }
    var isRunning by remember(timer.id) { mutableStateOf(timer.isRunning) }
    var isFinished by remember(timer.id) { mutableStateOf(timer.isFinished) }
    var isDismissed by remember(timer.id) { mutableStateOf(timer.isDismissed) }

    LaunchedEffect(isRunning) {
        if (isRunning && currentRemainingSeconds > 0) {
            while (currentRemainingSeconds > 0 && isRunning) {
                delay(1000L)
                currentRemainingSeconds--
                onToggle(timer.copy(remainingSeconds = currentRemainingSeconds, isRunning = true))
            }
            if (currentRemainingSeconds == 0) {
                isFinished = true
                isRunning = false
                onToggle(timer.copy(remainingSeconds = 0, isRunning = false, isFinished = true, isDismissed = false)) // Set isDismissed to false when finished
                // TODO: Vibrate and play sound
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp) // Adjust height as needed for grid
            .then(if (showEdges) Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(8.dp)) else Modifier)
            .background(if (isFinished && !isDismissed) Color(0xFFed6d8b) else MaterialTheme.colorScheme.surface)
            .combinedClickable(
                onClick = { /* TODO: Implement edit timer */ },
                onLongClick = { onDelete() }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = timer.getTimeString(), fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Switch(checked = isRunning, onCheckedChange = {
                    isRunning = it
                    onToggle(timer.copy(isRunning = it, remainingSeconds = currentRemainingSeconds))
                })
            }
        }
    }
}
