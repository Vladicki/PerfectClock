package com.griffith.perfectclock

import android.R.attr.minWidth
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.nio.file.Files.size
import java.util.UUID
import kotlin.concurrent.timer
import kotlin.math.roundToInt

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
    val context = LocalContext.current
    val timerStorage = remember { TimerStorage(context) }
    var showDialog by remember { mutableStateOf(false) }
    var isAnyTimerDragging by remember { mutableStateOf(false) }

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
        ) {
            val cellWidth = maxWidth / gridConfig.columns
            val cellHeight = maxHeight / gridConfig.rows

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
                        onDraggingChange = { isAnyTimerDragging = it }
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
    onDraggingChange: (Boolean) -> Unit
) {
    var currentRemainingSeconds by remember(timer.id) { mutableStateOf(timer.remainingSeconds) }
    var isRunning by remember(timer.id) { mutableStateOf(timer.isRunning) }
    var isFinished by remember(timer.id) { mutableStateOf(timer.isFinished) }
    var isDismissed by remember(timer.id) { mutableStateOf(timer.isDismissed) }

    var offsetX by remember { mutableStateOf(timer.x) }
    var offsetY by remember { mutableStateOf(timer.y) }
    var widthInCells by remember { mutableStateOf(timer.width) }
    var heightInCells by remember { mutableStateOf(timer.height) }

    var isResizing by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(timer) {
        offsetX = timer.x
        offsetY = timer.y
        widthInCells = timer.width
        heightInCells = timer.height
    }

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
            }
        }
    }

    val density = LocalDensity.current
    val cellWidthPx = with(density) { cellWidth.toPx() }
    val cellHeightPx = with(density) { cellHeight.toPx() }

    val cardModifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { startOffset ->
                    onDraggingChange(true)
                    isDragging = true
                    val resizeHandleSizePx = 24.dp.toPx()
                    val cardWidth = size.width
                    val cardHeight = size.height

                    isResizing = startOffset.x > cardWidth - resizeHandleSizePx &&
                            startOffset.y > cardHeight - resizeHandleSizePx
                },
                onDragEnd = {
                    onDraggingChange(false)
                    isDragging = false
                    val updatedTimer = timer.copy(
                        x = offsetX,
                        y = offsetY,
                        width = widthInCells,
                        height = heightInCells
                    )
                    onUpdateTimer(updatedTimer)
                }
            ) { change, dragAmount ->
                change.consumeAllChanges()

                if (isResizing) {
                    val newWidth = widthInCells + (dragAmount.x / cellWidthPx)
                    val newHeight = heightInCells + (dragAmount.y / cellHeightPx)

                    val clampedWidth = newWidth.roundToInt().coerceIn(1, gridConfig.columns - offsetX)
                    val clampedHeight = newHeight.roundToInt().coerceIn(1, gridConfig.rows - offsetY)

                    val collision = timers.any {
                        it.id != timer.id &&
                                offsetX < it.x + it.width && offsetX + clampedWidth > it.x &&
                                offsetY < it.y + it.height && offsetY + clampedHeight > it.y
                    }

                    if (!collision) {
                        widthInCells = clampedWidth
                        heightInCells = clampedHeight
                    }
                } else {
                    val newX = offsetX + (dragAmount.x / cellWidthPx)
                    val newY = offsetY + (dragAmount.y / cellHeightPx)

                    val clampedX = newX.roundToInt().coerceIn(0, gridConfig.columns - widthInCells)
                    val clampedY = newY.roundToInt().coerceIn(0, gridConfig.rows - heightInCells)

                    val collision = timers.any {
                        it.id != timer.id &&
                                clampedX < it.x + it.width && clampedX + widthInCells > it.x &&
                                clampedY < it.y + it.height && clampedY + heightInCells > it.y
                    }

                    if (!collision) {
                        offsetX = clampedX
                        offsetY = clampedY
                    }
                }
            }
        }
        .then(
            if (isDragging || showEdges) Modifier.border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                RoundedCornerShape(16.dp)
            ) else Modifier
        )
        .combinedClickable(
            onClick = {},
            // onLongClick = { onDelete() }
        )

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isFinished && !isDismissed -> Color(0xFFed6d8b)
                isRunning -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                    .background(Color.White.copy(alpha = 0.8f))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onDraggingChange(true) },
                            onDragEnd = {
                                onDraggingChange(false)
                                val updatedTimer = timer.copy(
                                    x = offsetX,
                                    y = offsetY,
                                    width = widthInCells,
                                    height = heightInCells
                                )
                                onUpdateTimer(updatedTimer)
                            }
                        ) { change, dragAmount ->
                            change.consumeAllChanges()
                            val newX = offsetX + (dragAmount.x / cellWidth.value)
                            val newY = offsetY + (dragAmount.y / cellHeight.value)
                            val clampedX = newX.roundToInt().coerceIn(0, gridConfig.columns - widthInCells)
                            val clampedY = newY.roundToInt().coerceIn(0, gridConfig.rows - heightInCells)
                            val collision = timers.any {
                                it.id != timer.id &&
                                        clampedX < it.x + it.width && clampedX + widthInCells > it.x &&
                                        clampedY < it.y + it.height && clampedY + heightInCells > it.y
                            }
                            if (!collision) {
                                offsetX = clampedX
                                offsetY = clampedY
                            }
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Add, // optional icon for visual handle
                    contentDescription = "Drag Timer",
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.Black
                )
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
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onDraggingChange(true) },
                            onDragEnd = {
                                onDraggingChange(false)
                                val updatedTimer = timer.copy(
                                    x = offsetX,
                                    y = offsetY,
                                    width = widthInCells,
                                    height = heightInCells
                                )
                                onUpdateTimer(updatedTimer)
                            }
                        ) { change, dragAmount ->
                            change.consumeAllChanges()
                            val newWidth = widthInCells + (dragAmount.x / cellWidth.value)
                            val newHeight = heightInCells + (dragAmount.y / cellHeight.value)
                            val clampedWidth = newWidth.roundToInt().coerceIn(1, gridConfig.columns - offsetX)
                            val clampedHeight = newHeight.roundToInt().coerceIn(1, gridConfig.rows - offsetY)

                            val collision = timers.any {
                                it.id != timer.id &&
                                        offsetX < it.x + it.width && offsetX + clampedWidth > it.x &&
                                        offsetY < it.y + it.height && offsetY + clampedHeight > it.y
                            }

                            if (!collision) {
                                widthInCells = clampedWidth
                                heightInCells = clampedHeight
                            }
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInFull,
                    contentDescription = "Resize Timer",
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.Black
                )
            }
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
        modifier = Modifier.width(380.dp).height(600.dp),
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
                                        "⌫" -> if (inputText.isNotEmpty()) inputText = inputText.dropLast(1)
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