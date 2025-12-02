package com.griffith.perfectclock

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.griffith.perfectclock.Alarm
import com.griffith.perfectclock.AlarmItem
import com.griffith.perfectclock.TimerItem
import com.griffith.perfectclock.components.GridBackground
import com.griffith.perfectclock.components.GridHighlight
import com.griffith.perfectclock.ShakeItOffDialog
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CustomScreen(
    gridConfig: GridLayoutConfig,
    customGridItems: List<GridItem>,
    onUpdateItem: (GridItem) -> Unit,
    onAddItem: (GridItem) -> Unit,
    onDeleteItem: (GridItem) -> Unit
) {
    var isAnyItemDragging by remember { mutableStateOf(false) }
    var gridContainerOffset by remember { mutableStateOf(Offset.Zero) }
    var showShakeItOffDialogForTimer by remember { mutableStateOf<Timer?>(null) } // State for shake it off dialog

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground(gridConfig = gridConfig, isDragging = isAnyItemDragging)

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

            customGridItems.forEach { item ->
                val itemModifier = Modifier
                    .offset(
                        x = (item.x * cellWidth.value).dp,
                        y = (item.y * cellHeight.value).dp
                    )
                    .size(
                        width = (item.width * cellWidth.value).dp,
                        height = (item.height * cellHeight.value).dp
                    )

                Box(modifier = itemModifier.padding(4.dp)) {
                    // Differentiate between Alarm and Timer to render specific UI
                    when (item) {
                        is Alarm -> {
                            val alarmsList = customGridItems.filterIsInstance<Alarm>()
                            AlarmItem(
                                alarm = item,
                                alarms = alarmsList, // Pass only alarms for collision detection
                                gridConfig = gridConfig,
                                cellWidth = cellWidth,
                                cellHeight = cellHeight,
                                onUpdateAlarm = { updatedAlarm -> onUpdateItem(updatedAlarm) },
                                onDelete = { onDeleteItem(item) },
                                showEdges = gridConfig.showEdges,
                                isAnyTimerDragging = isAnyItemDragging, // Reuse this state
                                onDraggingChange = { isAnyItemDragging = it },
                                gridContainerOffset = gridContainerOffset
                            )
                        }
                        is Timer -> {
                            val timersList = customGridItems.filterIsInstance<Timer>()
                            TimerItem(
                                timer = item,
                                timers = timersList,
                                gridConfig = gridConfig,
                                cellWidth = cellWidth,
                                cellHeight = cellHeight,
                                onUpdateTimer = { updatedTimer -> onUpdateItem(updatedTimer) },
                                onDelete = { onDeleteItem(item) },
                                showEdges = gridConfig.showEdges,
                                isAnyTimerDragging = isAnyItemDragging,
                                onDraggingChange = { isAnyItemDragging = it },
                                gridContainerOffset = gridContainerOffset,
                                onTimerFinished = { finishedTimer ->
                                    if (!finishedTimer.isDismissed) {
                                        showShakeItOffDialogForTimer = finishedTimer
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // SHAKE IT OFF DIALOG FOR TIMERS
    showShakeItOffDialogForTimer?.let { timer ->
        ShakeItOffDialog(
            onShakeDismiss = {
                if (timer.useOnce) {
                    onDeleteItem(timer)
                } else {
                    onUpdateItem(
                        timer.copy(
                            remainingSeconds = timer.initialSeconds,
                            isRunning = false,
                            isFinished = false,
                            isDismissed = true
                        )
                    )
                }
                showShakeItOffDialogForTimer = null
            },
            onManualDismiss = {
                if (timer.useOnce) {
                    onDeleteItem(timer)
                } else {
                    onUpdateItem(
                        timer.copy(
                            remainingSeconds = timer.initialSeconds,
                            isRunning = false,
                            isFinished = false,
                            isDismissed = true
                        )
                    )
                }
                showShakeItOffDialogForTimer = null
            }
        )
    }
}
