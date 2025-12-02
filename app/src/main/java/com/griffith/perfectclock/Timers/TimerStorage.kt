package com.griffith.perfectclock

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.griffith.perfectclock.Timer

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
