package com.griffith.perfectclock

import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class Alarm(
    val id: String,
    val hour: Int,
    val minute: Int,
    val useOnce: Boolean,
    val isEnabled: Boolean = true
) {
    fun getTimeString(): String {
        return LocalTime.of(hour, minute).format(DateTimeFormatter.ofPattern("hh:mm a"))
    }
}