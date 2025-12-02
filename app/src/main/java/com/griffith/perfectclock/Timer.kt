package com.griffith.perfectclock

import java.util.UUID


data class Timer(
    override val id: String = UUID.randomUUID().toString(),
    var initialSeconds: Int,
    var remainingSeconds: Int,
    var isRunning: Boolean = false,
    var isFinished: Boolean = false,
    var useOnce: Boolean = true,
    var isDismissed: Boolean = false,
    override var x: Int = 0,
    override var y: Int = 0,
    override var width: Int = 1,
    override var height: Int = 1
) : GridItem {
    override fun copyWithNewGridValues(x: Int, y: Int, width: Int, height: Int): GridItem {
        return this.copy(x = x, y = y, width = width, height = height)
    }
    fun getTimeString(): String {
        val hours = remainingSeconds / 3600
        val minutes = (remainingSeconds % 3600) / 60
        val seconds = remainingSeconds % 60
    return when {
        hours > 0 ->
            String.format("%d:%02d:%02d", hours, minutes, seconds)

        minutes > 0 ->
            String.format("%d:%02d", minutes, seconds)

        else ->
            String.format("%d", seconds)
    }
    }
}
