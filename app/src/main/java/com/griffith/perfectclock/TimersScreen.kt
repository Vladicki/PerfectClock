package com.griffith.perfectclock

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.util.UUID

// Placeholder for Timer data class
data class Timer(
    val id: String,
    var hour: Int,
    var minute: Int,
    var second: Int,
    var isEnabled: Boolean = true
) {
    fun getTimeString(): String {
        return String.format("%02d:%02d:%02d", hour, minute, second)
    }
}

// Placeholder for TimerStorage
class TimerStorage(context: Context) {
    fun saveTimers(timers: List<Timer>) {
        // TODO: Implement actual storage using SharedPreferences or DataStore
    }

    fun loadTimers(): MutableList<Timer> {
        // TODO: Implement actual loading
        return mutableListOf()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TimersScreen(gridConfig: GridLayoutConfig) {
    val context = LocalContext.current
    val timerStorage = remember { TimerStorage(context) }

    var timers by remember { mutableStateOf(timerStorage.loadTimers()) }

    var showDialog by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState() // Using TimePickerState for simplicity, though timers usually have seconds

    LaunchedEffect(Unit) {
        // Reload timers if they change (e.g., from settings)
        timers = timerStorage.loadTimers()
    }

    Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Timers Screen", fontSize = 24.sp, modifier = Modifier.padding(16.dp))
        
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
                        onToggle = {
                            val updatedTimers = timers.toMutableList()
                            val index = updatedTimers.indexOf(timer)
                            if (index != -1) {
                                updatedTimers[index] = timer.copy(isEnabled = it)
                                timers = updatedTimers
                                timerStorage.saveTimers(timers)
                            }
                        },
                        onDelete = {
                            timers = timers.toMutableList().apply { remove(timer) }
                            timerStorage.saveTimers(timers)
                        },
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
                        // For simplicity, not adding "use once" for timers yet
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val newTimer = Timer(
                            id = UUID.randomUUID().toString(),
                            hour = timePickerState.hour,
                            minute = timePickerState.minute,
                            second = 0 // Default to 0 seconds for now
                        )
                        timers = timers.toMutableList().apply { add(newTimer) }
                        timerStorage.saveTimers(timers)
                        showDialog = false
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
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimerItem(timer: Timer, onToggle: (Boolean) -> Unit, onDelete: () -> Unit, showEdges: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp) // Adjust height as needed for grid
            .then(if (showEdges) Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(8.dp)) else Modifier)
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
                Text(text = "Enabled") // Placeholder for timer status
                Switch(checked = timer.isEnabled, onCheckedChange = onToggle)
            }
        }
    }
}
