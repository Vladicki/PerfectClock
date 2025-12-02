// DraggingSystem.kt
package com.griffith.perfectclock.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.input.pointer.consume
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.runtime.compositionLocalOf

/**
 * Holds shared state for the current drag session.
 */
class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero) // top-left of the item in window coords
    var dragOffset by mutableStateOf(Offset.Zero)   // how far pointer moved from start
    var pointerOffsetWithinItem by mutableStateOf(Offset.Zero) // pointer offset inside item at drag start
    var draggableComposable by mutableStateOf<(@Composable () -> Unit)?>(null)
    var dataToDrop by mutableStateOf<Any?>(null)

    // convenience reset
    fun reset() {
        isDragging = false
        dragPosition = Offset.Zero
        dragOffset = Offset.Zero
        pointerOffsetWithinItem = Offset.Zero
        draggableComposable = null
        dataToDrop = null
    }
}

internal val LocalDragTargetInfo = compositionLocalOf { DragTargetInfo() }

/**
 * Root container which provides DragTargetInfo and shows the floating dragged item.
 * Use this to wrap the whole screen where drag/drop should work.
 */
@Composable
fun DragableScreen(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = remember { DragTargetInfo() }

    CompositionLocalProvider(LocalDragTargetInfo provides state) {
        Box(modifier = modifier.fillMaxSize()) {
            content()

            // Floating ghost overlay when dragging
            if (state.isDragging) {
                // The ghost is positioned at the original composable's position plus the drag offset.
                // This makes the dragged item appear to move with the finger, maintaining the
                // initial pointer offset relative to the composable.
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val dragAmount = state.dragOffset
                            val startPosition = state.dragPosition
                            translationX = startPosition.x + dragAmount.x
                            translationY = startPosition.y + dragAmount.y

                            // slight scale + alpha so it's visually on top
                            scaleX = 1.05f
                            scaleY = 1.05f
                            alpha = 0.95f
                        }
                ) {
                    state.draggableComposable?.invoke()
                }
            }
        }
    }
}

/**
 * Makes content draggable. Provide the data that will be passed to DropItem.
 *
 * NOTE:
 * - This implementation uses immediate drag start. If you want long-press-to-start,
 *   we can add a small pointer helper that waits for long press before enabling drag.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun <T> DragTarget(
    dataToDrop: T,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dragState = LocalDragTargetInfo.current

    // track the top-left position (window coords) of this composable so we can calculate pointer offset
    var topLeftWindow by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                // boundsInWindow gives us top-left in window coordinates
                val bounds = coordinates.boundsInWindow()
                topLeftWindow = Offset(bounds.left, bounds.top)
            }
            .pointerInput(dataToDrop) {
                detectDragGestures(
                    // called when drag starts; offset is the position of the pointer relative to the pointerInput's coordinate space
                    onDragStart = { pointerPosition ->
                        // record absolute drag start position (top-left of the item)
                        dragState.isDragging = true
                        dragState.dragPosition = topLeftWindow
                        dragState.dragOffset = Offset.Zero
                        // pointer offset within item = pointer absolute pos - topLeftWindow
                        val pointerAbsolute = topLeftWindow + pointerPosition
                        dragState.pointerOffsetWithinItem = pointerAbsolute - topLeftWindow
                        // expose composable and data
                        dragState.draggableComposable = content
                        dragState.dataToDrop = dataToDrop
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // update offset so overlay moves smoothly
                        dragState.dragOffset = dragState.dragOffset + Offset(dragAmount.x, dragAmount.y)
                    },
                    onDragEnd = {
                        // On release, mark not dragging; DropItem will react when it sees isDragging = false
                        dragState.isDragging = false
                    },
                    onDragCancel = {
                        dragState.reset()
                    }
                )
            }
    ) {
        content()
    }
}

/**
 * Drop zone. Provide an onDrop that receives the data.
 *
 * This uses boundsInWindow to test whether the dragged pointer (dragPosition + dragOffset)
 * is inside the drop zone.
 */
@Composable
fun <T> DropItem(
    modifier: Modifier = Modifier,
    onDrop: (T) -> Unit,
    content: @Composable (isInBound: Boolean) -> Unit
) {
    val dragState = LocalDragTargetInfo.current

    var boundsInWindowRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    // Compute whether pointer currently within this drop area
    val isHovering by derivedStateOf {
        if (!dragState.isDragging) {
            false
        } else {
            // The pointer's position is the sum of the drag start position, the pointer's offset
            // within the drag target, and the total drag offset.
            val pointerGlobal = dragState.dragPosition + dragState.pointerOffsetWithinItem + dragState.dragOffset
            boundsInWindowRect?.contains(pointerGlobal) == true
        }
    }

    // When drag finishes, if the pointer is inside, call onDrop
    LaunchedEffect(dragState.isDragging, isHovering) {
        if (!dragState.isDragging && isHovering) {
            @Suppress("UNCHECKED_CAST")
            (dragState.dataToDrop as? T)?.let { data ->
                onDrop(data)
            }
            // clear data
            dragState.reset()
        }
    }

    Box(
        modifier = modifier.onGloballyPositioned { coords ->
            boundsInWindowRect = coords.boundsInWindow()
        }
    ) {
        content(isHovering)
    }
}

