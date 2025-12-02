package com.griffith.perfectclock

import java.time.LocalTime
import java.time.format.DateTimeFormatter // Added import for DateTimeFormatter
import java.util.UUID

data class Alarm(
    override val id: String = UUID.randomUUID().toString(),
    val time: LocalTime, 
    val useOnce: Boolean,
    val isEnabled: Boolean = true,
    val message: String = "",
    override val x: Int = 0,
    override val y: Int = 0,
    override val width: Int = 1,
    override val height: Int = 1
) : GridItem {

    fun getTimeString(): String {
        return time.format(DateTimeFormatter.ofPattern("HH:mm")) // Updated to use LocalTime
    }

    override fun copyWithNewGridValues(x: Int, y: Int, width: Int, height: Int): GridItem {
        return this.copy(x = x, y = y, width = width, height = height)
    }
}
