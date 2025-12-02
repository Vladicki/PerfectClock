package com.griffith.perfectclock

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Holds info about the currently dragged item.
 */
data class DragInfo(
    val item: GridItem,
    val dragX: Float,
    val dragY: Float,
    val widthPx: Int,
    val heightPx: Int,
    val draggableContent: @Composable () -> Unit
)

/**
 * Global drag controller.
 */
object DragController {
    private val _dragInfo = MutableStateFlow<DragInfo?>(null)
    val dragInfo: StateFlow<DragInfo?> = _dragInfo

    fun startDrag(
        item: GridItem,
        startX: Float,
        startY: Float,
        widthPx: Int,
        heightPx: Int,
        composable: @Composable () -> Unit
    ) {
        _dragInfo.value = DragInfo(
            item = item,
            dragX = startX,
            dragY = startY,
            widthPx = widthPx,
            heightPx = heightPx,
            draggableContent = composable
        )
    }

    fun updateDrag(x: Float, y: Float) {
        _dragInfo.value = _dragInfo.value?.copy(dragX = x, dragY = y)
    }

    fun endDrag() {
        _dragInfo.value = null
    }
}


/**
 * Drag overlay showing a ghost of the dragged composable.
 */
@Composable
fun DragOverlay(
    content: @Composable () -> Unit
) {
    val dragInfo by DragController.dragInfo.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Main app UI under
        content()

        // Ghost drag overlay
        dragInfo?.let { d ->
            val density = LocalDensity.current

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (d.dragX - (d.widthPx / 2)).toInt(),
                            (d.dragY - (d.heightPx / 2)).toInt()
                        )
                    }
                    .zIndex(999f)
                    .graphicsLayer {
                        this.alpha = 0.75f
                    }
            ) {
                Box(
                    modifier = Modifier.size(
                        width = with(density) { d.widthPx.toDp() },
                        height = with(density) { d.heightPx.toDp() }
                    )
                    .pointerInput(Unit) {} // BLOCK ALL INTERACTIONS
                ) {
                    d.draggableContent()
                }
            }
        }
    }
}