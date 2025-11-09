package com.griffith.perfectclock

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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AlarmsScreen(gridConfig: GridLayoutConfig) {
    val context = LocalContext.current
    val alarmStorage = remember { AlarmStorage(context) }

    var alarms by remember { mutableStateOf(alarmStorage.loadAlarms()) }

    var showDialog by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()
    var useOnce by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Reload alarms if they change (e.g., from settings)
        alarms = alarmStorage.loadAlarms()
    }

    Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Alarms Screen", fontSize = 24.sp, modifier = Modifier.padding(16.dp))
        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridConfig.columns),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {                items(alarms) { alarm ->
                    AlarmItem(
                        alarm = alarm,
                        onToggle = {
                            val updatedAlarms = alarms.toMutableList()
                            val index = updatedAlarms.indexOf(alarm)
                            if (index != -1) {
                                updatedAlarms[index] = alarm.copy(isEnabled = it)
                                alarms = updatedAlarms
                                alarmStorage.saveAlarms(alarms)
                            }
                        },
                        onDelete = {
                            alarms = alarms.toMutableList().apply { remove(alarm) }
                            alarmStorage.saveAlarms(alarms)
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
            Icon(Icons.Filled.Add, "Add new alarm.")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add New Alarm") },
                text = {
                    Column {
                        TimeInput(state = timePickerState)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = useOnce, onCheckedChange = { useOnce = it })
                            Text("Use Once")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val newAlarm = Alarm(
                            id = UUID.randomUUID().toString(),
                            hour = timePickerState.hour,
                            minute = timePickerState.minute,
                            useOnce = useOnce
                        )
                        alarms = alarms.toMutableList().apply { add(newAlarm) }
                        alarmStorage.saveAlarms(alarms)
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
fun AlarmItem(alarm: Alarm, onToggle: (Boolean) -> Unit, onDelete: () -> Unit, showEdges: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp) // Adjust height as needed for 6x4 grid
            .then(if (showEdges) Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(8.dp)) else Modifier)
            .combinedClickable(
                onClick = { /* TODO: Implement edit alarm */ },
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
            Text(text = alarm.getTimeString(), fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (alarm.useOnce) "Once" else "Repeat")
                Switch(checked = alarm.isEnabled, onCheckedChange = onToggle)
            }
        }
    }
}
