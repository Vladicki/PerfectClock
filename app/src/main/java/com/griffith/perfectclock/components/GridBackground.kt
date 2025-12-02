package com.griffith.perfectclock.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.griffith.perfectclock.GridLayoutConfig

@Composable
fun GridBackground(gridConfig: GridLayoutConfig, isDragging: Boolean) {
    val lineColor = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val strokeWidth = 1f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cellWidth = size.width / gridConfig.columns
        val cellHeight = size.height / gridConfig.rows

        // Draw vertical lines
        for (i in 0..gridConfig.columns) {
            val startX = i * cellWidth
            drawLine(
                color = lineColor,
                start = Offset(startX, 0f),
                end = Offset(startX, size.height),
                strokeWidth = strokeWidth
            )
        }

        // Draw horizontal lines
        for (i in 0..gridConfig.rows) {
            val startY = i * cellHeight
            drawLine(
                color = lineColor,
                start = Offset(0f, startY),
                end = Offset(size.width, startY),
                strokeWidth = strokeWidth
            )
        }
    }
}