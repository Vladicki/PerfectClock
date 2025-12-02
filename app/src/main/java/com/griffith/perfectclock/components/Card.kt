package com.griffith.perfectclock.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.griffith.perfectclock.DragController
import com.griffith.perfectclock.GridItem
import com.griffith.perfectclock.GridLayoutConfig
import kotlin.math.roundToInt
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.input.pointer.positionChange

/**
 * A generic draggable and resizable card for grid layouts.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : GridItem> ItemCard(
    item: T,
    items: List<T>,
    gridConfig: GridLayoutConfig,
    cellWidth: Dp,
    cellHeight: Dp,
    onUpdateItem: (T) -> Unit,
    onDraggingChange: (Boolean) -> Unit,
    isDragging: Boolean,
    showEdges: Boolean,
    containerColor: Color,
    modifier: Modifier = Modifier,
    gridContainerOffset: Offset,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val cellWidthPx = with(density) { cellWidth.toPx() }
    val cellHeightPx = with(density) { cellHeight.toPx() }

    var offsetX by remember { mutableStateOf(item.x) }
    var offsetY by remember { mutableStateOf(item.y) }
    var widthInCells by remember { mutableStateOf(item.width) }
    var heightInCells by remember { mutableStateOf(item.height) }
    var isResizing by remember { mutableStateOf(false) }
    var windowOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(item) {
        offsetX = item.x
        offsetY = item.y
        widthInCells = item.width
        heightInCells = item.height
    }

    val cardModifier = modifier
        .onGloballyPositioned {
            windowOffset = it.localToWindow(Offset.Zero)
        }
        .pointerInput(item.id) {
            detectDragGestures(
                onDragStart = { startOffset ->
                    onDraggingChange(true)
                    val resizeHandleSizePx = 32.dp.toPx()
                    val cardWidth = size.width
                    val cardHeight = size.height

                    isResizing = startOffset.x > cardWidth - resizeHandleSizePx &&
                            startOffset.y > cardHeight - resizeHandleSizePx

                    if (!isResizing) {
                        DragController.startDrag(
                            item = item,
                            startX = windowOffset.x + startOffset.x,
                            startY = windowOffset.y + startOffset.y,
                            widthPx = size.width,
                            heightPx = size.height,
                            composable = {
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = containerColor),
                                    elevation = CardDefaults.cardElevation(6.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        content()
                                    }
                                }
                            }
                        )
                    }
                },
                onDragEnd = {
                    onDraggingChange(false)
                    if (!isResizing) {
                        // Get current drag position from DragController before ending drag
                        val currentDragInfo = DragController.dragInfo.value
                        DragController.endDrag()

                        currentDragInfo?.let { d ->
                            val cellWidthPx = with(density) { cellWidth.toPx() }
                            val cellHeightPx = with(density) { cellHeight.toPx() }

                            // Calculate card's top-left screen position
                            val cardScreenX = d.dragX - d.widthPx / 2
                            val cardScreenY = d.dragY - d.heightPx / 2

                            // Calculate card's top-left position relative to grid container
                            val relativeX = cardScreenX - gridContainerOffset.x
                            val relativeY = cardScreenY - gridContainerOffset.y

                            // Calculate target column and row, clamping within grid bounds
                            var targetCol = (relativeX / cellWidthPx).roundToInt()
                                .coerceIn(0, gridConfig.columns - widthInCells)
                            var targetRow = (relativeY / cellHeightPx).roundToInt()
                                .coerceIn(0, gridConfig.rows - heightInCells)

                            // Check for collision at target position
                            val collision = items.any {
                                it.id != item.id &&
                                        targetCol < it.x + it.width && targetCol + widthInCells > it.x &&
                                        targetRow < it.y + it.height && targetRow + heightInCells > it.y
                            }

                            if (collision) {
                                // Revert to original position if collision occurs
                                targetCol = item.x
                                targetRow = item.y
                            }

                            // Update offsetX and offsetY with the final snapped/reverted position
                            offsetX = targetCol
                            offsetY = targetRow
                        }
                    }
                    @Suppress("UNCHECKED_CAST")
                    val updatedItem = item.copyWithNewGridValues(
                        x = offsetX,
                        y = offsetY,
                        width = widthInCells,
                        height = heightInCells
                    ) as T
                    onUpdateItem(updatedItem)
                }
            ) { change, dragAmount ->
                change.consumeAllChanges()

                if (isResizing) {
                    val newWidth = widthInCells + (dragAmount.x / cellWidthPx)
                    val newHeight = heightInCells + (dragAmount.y / cellHeightPx)

                    val clampedWidth = newWidth
                        .roundToInt()
                        .coerceIn(1, gridConfig.columns - offsetX)
                    val clampedHeight = newHeight
                        .roundToInt()
                        .coerceIn(1, gridConfig.rows - offsetY)

                    val collision = items.any {
                        it.id != item.id &&
                                offsetX < it.x + it.width && offsetX + clampedWidth > it.x &&
                                offsetY < it.y + it.height && offsetY + clampedHeight > it.y
                    }

                    if (!collision) {
                        widthInCells = clampedWidth
                        heightInCells = clampedHeight
                    }
                } else {
                    DragController.updateDrag(
                        x = windowOffset.x + change.position.x,
                        y = windowOffset.y + change.position.y
                    )
                    val newX = offsetX + (dragAmount.x / cellWidthPx)
                    val newY = offsetY + (dragAmount.y / cellHeightPx)

                    val clampedX = newX
                        .roundToInt()
                        .coerceIn(0, gridConfig.columns - widthInCells)
                    val clampedY = newY
                        .roundToInt()
                        .coerceIn(0, gridConfig.rows - heightInCells)

                    val collision = items.any {
                        it.id != item.id &&
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
        .border(
            width = if (showEdges || isDragging) 1.dp else 0.dp,
            color = if (showEdges || isDragging)
                MaterialTheme.colorScheme.primary
            else Color.Transparent,
            shape = RoundedCornerShape(16.dp)
        )

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}


