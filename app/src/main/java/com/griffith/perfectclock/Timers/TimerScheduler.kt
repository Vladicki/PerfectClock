package com.griffith.perfectclock.Timers

import com.griffith.perfectclock.Timer

interface TimerScheduler {
    fun schedule(timer: Timer, triggerAtMillis: Long)
    fun cancel(timer: Timer)
}
