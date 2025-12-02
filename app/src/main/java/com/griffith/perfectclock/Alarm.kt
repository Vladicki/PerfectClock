package com.griffith.perfectclock

import java.time.LocalTime


import java.util.UUID

data class Alarm(
    override val id: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val useOnce: Boolean,
    val isEnabled: Boolean = true,
    override val x: Int = 0,
    override val y: Int = 0,
    override val width: Int = 1,
    override val height: Int = 1
) : GridItem {

    fun getTimeString(): String {
        return String.format("%02d:%02d", hour, minute)
    }

    override fun copyWithNewGridValues(x: Int, y: Int, width: Int, height: Int): GridItem {
        return this.copy(x = x, y = y, width = width, height = height)
    }
}
