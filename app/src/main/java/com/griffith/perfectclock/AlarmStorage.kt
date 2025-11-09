package com.griffith.perfectclock

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AlarmStorage(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveAlarms(alarms: List<Alarm>) {
        val json = gson.toJson(alarms)
        sharedPreferences.edit().putString("alarms", json).apply()
    }

    fun loadAlarms(): List<Alarm> {
        val json = sharedPreferences.getString("alarms", null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<List<Alarm>>() {}.type)
        } else {
            emptyList()
        }
    }
}