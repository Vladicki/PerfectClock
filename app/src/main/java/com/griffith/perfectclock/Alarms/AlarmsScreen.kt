package com.griffith.perfectclock

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.griffith.perfectclock.components.GridHighlight
import com.griffith.perfectclock.components.GridBackground

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griffith.perfectclock.components.ItemCard
import java.time.LocalTime // Added import for LocalTime
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AlarmsScreen(
    gridConfig: GridLayoutConfig,
    alarms: List<Alarm>,
    onUpdateAlarm: (Alarm) -> Unit,
    onAddAlarm: (Alarm) -> Unit,
    onDeleteAlarm: (Alarm) -> Unit
) {
    val context = LocalContext.current
    val alarmStorage = remember { AlarmStorage(context) }

    var showDialog by remember { mutableStateOf(false) }
    var isAnyTimerDragging by remember { mutableStateOf(false) }
    var gridContainerOffset by remember { mutableStateOf(Offset.Zero) }
    // Removed timePickerState, useOnce, usingDial, haptic from here

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground(gridConfig = gridConfig, isDragging = isAnyTimerDragging)

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .onGloballyPositioned {
                    gridContainerOffset = it.localToWindow(Offset.Zero)
                }
        ) {
            val cellWidth = maxWidth / gridConfig.columns
            val cellHeight = maxHeight / gridConfig.rows

            GridHighlight(gridConfig, cellWidth, cellHeight, gridContainerOffset)

            alarms.forEach { alarm ->
                val timerModifier = Modifier
                    .offset(
                        x = (alarm.x * cellWidth.value).dp,
                        y = (alarm.y * cellHeight.value).dp
                    )
                    .size(
                        width = (alarm.width * cellWidth.value).dp,
                        height = (alarm.height * cellHeight.value).dp
                    )

                Box(modifier = timerModifier.padding(4.dp)) {
                    AlarmItem(
                        alarm = alarm,
                        alarms = alarms,
                        gridConfig = gridConfig,
                        cellWidth = cellWidth,
                        cellHeight = cellHeight,
                        onUpdateAlarm = onUpdateAlarm,
                        onDelete = { onDeleteAlarm(alarm) },
                        showEdges = gridConfig.showEdges,
                        isAnyTimerDragging = isAnyTimerDragging,
                        onDraggingChange = { isAnyTimerDragging = it },
                        gridContainerOffset = gridContainerOffset
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
            AddAlarmDialog(
                onDismissRequest = { showDialog = false },
                onAddAlarm = onAddAlarm,
                gridConfig = gridConfig,
                alarms = alarms
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlarmItem(
    alarm: Alarm,
    alarms: List<Alarm>,
    gridConfig: GridLayoutConfig,
    cellWidth: Dp,
    cellHeight: Dp,
    onUpdateAlarm: (Alarm) -> Unit,
    onDelete: () -> Unit,
    showEdges: Boolean,
    isAnyTimerDragging: Boolean,
    onDraggingChange: (Boolean) -> Unit,
    gridContainerOffset: Offset
) {
    ItemCard(
        item = alarm,
        items = alarms,
        gridConfig = gridConfig,
        cellWidth = cellWidth,
        cellHeight = cellHeight,
        onUpdateItem = { onUpdateAlarm(it as Alarm) },
        onDraggingChange = onDraggingChange,
        isDragging = isAnyTimerDragging,
        showEdges = showEdges,
        containerColor = if (alarm.isEnabled) MaterialTheme.colorScheme.primaryContainer else Color.DarkGray,
        modifier = Modifier.fillMaxSize(),
        gridContainerOffset = gridContainerOffset
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                    Switch(
                        checked = alarm.isEnabled,
                        onCheckedChange = { onUpdateAlarm(alarm.copy(isEnabled = it)) }
                    )
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Alarm",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
