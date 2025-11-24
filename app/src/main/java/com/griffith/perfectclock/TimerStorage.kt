package com.griffith.perfectclock

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Timer(
    val id: String,
    var initialSeconds: Int,
    var remainingSeconds: Int,
    var isRunning: Boolean = false,
    var isFinished: Boolean = false,
    var useOnce: Boolean = true,
    var isDismissed: Boolean = false
) {
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

class TimerStorage(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveTimers(timers: List<Timer>) {
        val json = gson.toJson(timers)
        sharedPreferences.edit().putString("timers", json).apply()
    }

    fun loadTimers(): MutableList<Timer> {
        val json = sharedPreferences.getString("timers", null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<MutableList<Timer>>() {}.type)
        } else {
            mutableListOf()
        }
    }
}
