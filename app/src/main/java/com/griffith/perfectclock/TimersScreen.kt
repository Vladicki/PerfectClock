package com.griffith.perfectclock

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.onGloballyPositioned
import com.griffith.perfectclock.components.GridHighlight
import com.griffith.perfectclock.components.ItemCard
import kotlinx.coroutines.delay
import java.util.UUID
import com.griffith.perfectclock.Timer

// ---------------------------
// TIMER SCREEN MAIN COMPOSABLE
// ---------------------------
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TimersScreen(
    gridConfig: GridLayoutConfig,
    timers: List<Timer>,
    onUpdateTimer: (Timer) -> Unit,
    onAddTimer: (Timer) -> Unit,
    onDeleteTimer: (Timer) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var isAnyTimerDragging by remember { mutableStateOf(false) }
    var gridContainerOffset by remember { mutableStateOf(Offset.Zero) }
    var showShakeItOffDialogForTimer by remember { mutableStateOf<Timer?>(null) } // State for shake it off dialog

    // -------------------------
    // ROOT LAYERED CONTAINER
    // -------------------------
    Box(modifier = Modifier.fillMaxSize()) {

        GridBackground(gridConfig = gridConfig, isDragging = isAnyTimerDragging)

        // -------------------------
        // GRID OF TIMERS
        // -------------------------
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

            timers.forEach { timer ->
                val timerModifier = Modifier
                    .offset(
                        x = (timer.x * cellWidth.value).dp,
                        y = (timer.y * cellHeight.value).dp
                    )
                    .size(
                        width = (timer.width * cellWidth.value).dp,
                        height = (timer.height * cellHeight.value).dp
                    )

                Box(modifier = timerModifier.padding(4.dp)) {
                    TimerItem(
                        timer = timer,
                        timers = timers,
                        gridConfig = gridConfig,
                        cellWidth = cellWidth,
                        cellHeight = cellHeight,
                        onUpdateTimer = onUpdateTimer,
                        onDelete = { onDeleteTimer(timer) },
                        showEdges = gridConfig.showEdges,
                        isAnyTimerDragging = isAnyTimerDragging,
                        onDraggingChange = { isAnyTimerDragging = it },
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

        // -------------------------
        // FLOATING "ADD TIMER" BUTTON
        // -------------------------
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, "Add new timer.")
        }

        // -------------------------
        // GLOBAL DISMISS BUTTON
        // -------------------------
        val showGlobalDismissButton = timers.any { it.isFinished && !it.isDismissed }
        if (showGlobalDismissButton) {
            Button(
                onClick = {
                    val timersToProcess = timers.filter { it.isFinished && !it.isDismissed }
                    timersToProcess.forEach { timerToDismiss ->
                        if (timerToDismiss.useOnce) {
                            onDeleteTimer(timerToDismiss)
                        } else {
                            onUpdateTimer(
                                timerToDismiss.copy(
                                    remainingSeconds = timerToDismiss.initialSeconds,
                                    isRunning = false,
                                    isFinished = false,
                                    isDismissed = true
                                )
                            )
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
    } // END OF ROOT BOX

    // -------------------------
    // TIMER SETUP DIALOG
    // -------------------------
    if (showDialog) {
        TimerSetupDialog(
            onStart = { hours, minutes, seconds ->
                val totalSeconds = hours * 3600 + minutes * 60 + seconds

                if (totalSeconds > 0) {
                    var newX = 0
                    var newY = 0
                    var found = false

                    for (y in 0 until gridConfig.rows) {
                        for (x in 0 until gridConfig.columns) {
                            if (!timers.any { it.x == x && it.y == y }) {
                                newX = x
                                newY = y
                                found = true
                                break
                            }
                        }
                        if (found) break
                    }

                    onAddTimer(
                        Timer(
                            id = UUID.randomUUID().toString(),
                            initialSeconds = totalSeconds,
                            remainingSeconds = totalSeconds,
                            isRunning = true,
                            x = newX,
                            y = newY
                        )
                    )
                }
                showDialog = false
            },
            onClose = { showDialog = false }
        )
    }

    // -------------------------
    // SHAKE IT OFF DIALOG FOR TIMERS
    // -------------------------
    showShakeItOffDialogForTimer?.let { timer ->
        ShakeItOffDialog(
            onShakeDismiss = {
                if (timer.useOnce) {
                    onDeleteTimer(timer)
                } else {
                    onUpdateTimer(
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
                    onDeleteTimer(timer)
                } else {
                    onUpdateTimer(
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimerItem(
    timer: Timer,
    timers: List<Timer>,
    gridConfig: GridLayoutConfig,
    cellWidth: Dp,
    cellHeight: Dp,
    onUpdateTimer: (Timer) -> Unit,
    onDelete: () -> Unit,
    showEdges: Boolean,
    isAnyTimerDragging: Boolean,
    onDraggingChange: (Boolean) -> Unit,
    gridContainerOffset: Offset,
    onTimerFinished: (Timer) -> Unit
) {
    var currentRemainingSeconds by remember(timer.id) { mutableStateOf(timer.remainingSeconds) }
    var isRunning by remember(timer.id) { mutableStateOf(timer.isRunning) }
    var isFinished by remember(timer.id) { mutableStateOf(timer.isFinished) }
    val isDismissed by remember(timer.id) { mutableStateOf(timer.isDismissed) }

    LaunchedEffect(isRunning) {
        if (isRunning && currentRemainingSeconds > 0) {
            while (currentRemainingSeconds > 0 && isRunning) {
                delay(1000L)
                currentRemainingSeconds--
                onUpdateTimer(timer.copy(remainingSeconds = currentRemainingSeconds, isRunning = true))
            }
            if (currentRemainingSeconds == 0) {
                isFinished = true
                isRunning = false
                onUpdateTimer(
                    timer.copy(
                        remainingSeconds = 0,
                        isRunning = false,
                        isFinished = true,
                        isDismissed = false
                    )
                )
                onTimerFinished(timer) // Call the callback when timer finishes
            }
        }
    }

    ItemCard(
        item = timer,
        items = timers,
        gridConfig = gridConfig,
        cellWidth = cellWidth,
        cellHeight = cellHeight,
        onUpdateItem = { onUpdateTimer(it as Timer) },
        onDraggingChange = onDraggingChange,
        isDragging = isAnyTimerDragging,
        showEdges = showEdges,
        containerColor = when {
            isFinished && !isDismissed -> Color(0xFFed6d8b)
            isRunning -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        modifier = Modifier.fillMaxSize(),
        gridContainerOffset = gridContainerOffset
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Timer Text in Center
            Text(
                text = timer.getTimeString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )

            // TOP-LEFT: Drag Handle
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
            ) {
                // Icon(
                //     imageVector = Icons.Default.Add, // optional icon for visual handle
                //     contentDescription = "Drag Timer",
                //     modifier = Modifier.fillMaxSize(),
                //     tint = Color.Black.copy(alpha = 0.5f)
                // )
            }

            // + TOP-RIGHT: Close Button
            // Top-left +1:00 button
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    if (isRunning) {
                        IconButton(
                            onClick = { onDelete() },
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }

                        Button(
                            onClick = {
                                currentRemainingSeconds += 60
                                onUpdateTimer(timer.copy(remainingSeconds = currentRemainingSeconds))
                            },
                            modifier = Modifier
                                .height(48.dp)
                                .defaultMinSize(minWidth = 64.dp), // enough for text
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp) // shrink internal padding
                        ) {
                            Text("+1:00", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Reset Button
                IconButton(
                    onClick = {
                        currentRemainingSeconds = timer.initialSeconds
                        isRunning = false
                        isFinished = false
                        onUpdateTimer(
                            timer.copy(
                                remainingSeconds = currentRemainingSeconds,
                                isRunning = false,
                                isFinished = false
                            )
                        )
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset"
                    )
                }


// Play / Stop Button using IconButton
                IconButton(
                    onClick = {
                        isRunning = !isRunning
                        onUpdateTimer(
                            timer.copy(
                                isRunning = isRunning,
                                remainingSeconds = currentRemainingSeconds
                            )
                        )
                    },
                    // The modifier is simplified for IconButton
                    modifier = Modifier.size(48.dp) // Set a size for the IconButton
                ) {
                    val icon = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow
                    val contentDesc = if (isRunning) "Pause Timer" else "Start Timer"

                    Icon(
                        imageVector = icon,
                        contentDescription = contentDesc,
                        // The default tint will match the Material Theme on surface/onBackground
                        // If you want a specific color, set it here:
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // BOTTOM-RIGHT: Resize Handle
            // Box(
            //     modifier = Modifier
            //         .align(Alignment.BottomEnd)
            //         .size(28.dp)
            //         .clip(CircleShape)
            //         .background(Color.White.copy(alpha = 0.8f))
            // ) {
            //     Icon(
            //         imageVector = Icons.Default.OpenInFull,
            //         contentDescription = "Resize Timer",
            //         modifier = Modifier.fillMaxSize(),
            //         tint = Color.Black
            //     )
            // }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerSetupDialog(
    onStart: (hours: Int, minutes: Int, seconds: Int) -> Unit,
    onClose: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    fun parseTime(): Triple<Int, Int, Int> {
        val padded = inputText.padStart(6, '0').takeLast(6)
        val hours = padded.substring(0, 2).toIntOrNull() ?: 0
        val minutes = padded.substring(2, 4).toIntOrNull() ?: 0
        val seconds = padded.substring(4, 6).toIntOrNull() ?: 0
        return Triple(hours, minutes, seconds)
    }

    val (hours, minutes, seconds) = parseTime()

    AlertDialog(
        onDismissRequest = { onClose() },
        confirmButton = {},
        title = { Text("Set Timer", style = MaterialTheme.typography.titleLarge) },
        modifier = Modifier
            .width(380.dp)
            .height(600.dp),
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // TIME DISPLAY WITH RIGHT-HIGHLIGHTED INPUT
                val paddedInput = inputText.padStart(6, '0').takeLast(6)
                val totalLength = paddedInput.length

                val annotatedTime = buildAnnotatedString {
                    paddedInput.forEachIndexed { i, char ->
                        val isHighlighted = i >= 6 - totalLength
                        withStyle(
                            style = SpanStyle(
                                color = if (isHighlighted) Color.White else Color.Gray,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(char)
                        }
                        // Append h/m/s after every 2 digits
                        if (i == 1) withStyle(SpanStyle(color = Color.Gray, fontSize = 20.sp)) { append("h ") }
                        if (i == 3) withStyle(SpanStyle(color = Color.Gray, fontSize = 20.sp)) { append("m ") }
                        if (i == 5) withStyle(SpanStyle(color = Color.Gray, fontSize = 20.sp)) { append("s") }
                    }
                }

                Text(
                    text = annotatedTime,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // NUMPAD
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("00", "0", "⌫")
                )

                for (row in keys) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        for (key in row) {
                            Button(
                                onClick = {
                                    when (key) {
                                        "⌫" -> if (inputText.isNotEmpty()) inputText =
                                            inputText.dropLast(1)
                                        "00" -> if (inputText.isNotEmpty() && inputText.length < 6) inputText += "00"
                                        else -> if (inputText.length < 6 && !(inputText.isEmpty() && key == "0")) inputText += key
                                    }
                                },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(60.dp)
                            ) {
                                Text(key, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ACTION BUTTONS
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onClose() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .height(70.dp)
                    ) {
                        // Text("X", color = Color.White, fontSize = 18.sp)
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "",
                            tint = Color.White,
                        )

                    }

                    val startEnabled = inputText.isNotEmpty()
                    Button(
                        onClick = {
                            val (h, m, s) = parseTime()
                            if (h + m + s > 0) onStart(h, m, s)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (startEnabled) Color(0xFF81C784) else Color.Gray
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .height(70.dp),
                        enabled = startEnabled
                    ) {
                        Text("▶", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    )
}

@Composable
fun GridBackground(
    gridConfig: GridLayoutConfig,
    isDragging: Boolean
) {
    if (isDragging) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / gridConfig.columns
            val cellHeight = size.height / gridConfig.rows
            val color = Color.DarkGray // Always dark gray when dragging

            for (i in 1 until gridConfig.columns) {
                drawLine(
                    color = color,
                    start = Offset(x = i * cellWidth, y = 0f),
                    end = Offset(x = i * cellWidth, y = size.height),
                    strokeWidth = 3.dp.toPx() // Thicker lines
                )
            }

            for (i in 1 until gridConfig.rows) {
                drawLine(
                    color = color,
                    start = Offset(x = 0f, y = i * cellHeight),
                    end = Offset(x = size.width, y = i * cellHeight),
                    strokeWidth = 3.dp.toPx() // Thicker lines
                )
            }
        }
    }
}
