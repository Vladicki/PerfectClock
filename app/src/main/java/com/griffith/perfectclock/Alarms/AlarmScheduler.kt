package com.griffith.perfectclock

interface AlarmScheduler {
    fun schedule(item: Alarm)
    fun cancel(item: Alarm)
}
