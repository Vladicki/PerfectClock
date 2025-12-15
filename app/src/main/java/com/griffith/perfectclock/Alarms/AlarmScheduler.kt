package com.griffith.perfectclock.Alarms

interface AlarmScheduler {
    fun schedule(item: Alarm)
    fun cancel(item: Alarm)
    fun snooze(item: Alarm)
}
