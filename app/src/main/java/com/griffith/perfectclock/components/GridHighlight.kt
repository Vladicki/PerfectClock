package com.griffith.perfectclock.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.griffith.perfectclock.DragController
import com.griffith.perfectclock.GridLayoutConfig

@Composable
fun GridHighlight(
    gridConfig: GridLayoutConfig,
    cellWidth: Dp,
    cellHeight: Dp,
    gridContainerOffset: Offset
) {
    val dragInfo by DragController.dragInfo.collectAsState()

    dragInfo?.let { d ->
        val density = LocalDensity.current
        val cellWidthPx = with(density) { cellWidth.toPx() }
        val cellHeightPx = with(density) { cellHeight.toPx() }

        val relativeX = d.dragX - gridContainerOffset.x
        val relativeY = d.dragY - gridContainerOffset.y

        val col = (relativeX / cellWidthPx).toInt().coerceIn(0, gridConfig.columns - 1)
        val row = (relativeY / cellHeightPx).toInt().coerceIn(0, gridConfig.rows - 1)

        Box(
            modifier = Modifier
                .offset(
                    x = (col * cellWidth.value).dp,
                    y = (row * cellHeight.value).dp
                )
                .size(cellWidth, cellHeight)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        )
    }
}
