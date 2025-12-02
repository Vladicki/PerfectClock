package com.griffith.perfectclock

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID


import kotlinx.serialization.Serializable

@Serializable
data class PageConfig(
    val id: String,
    val title: String,
    var isEnabled: Boolean = true,
    val isCustom: Boolean = false
)

class PageConfigStorage(private val context: Context) {
    private val prefs = context.getSharedPreferences("page_config", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadPageConfig(): MutableList<PageConfig> {
        val json = prefs.getString("pages", null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<PageConfig>>() {}.type
            gson.fromJson(json, type)
        } else {
            getDefaultPages()
        }
    }

    fun savePageConfig(pages: List<PageConfig>) {
        val json = gson.toJson(pages)
        prefs.edit().putString("pages", json).apply()
    }

    private fun getDefaultPages(): MutableList<PageConfig> {
        return mutableListOf(
            PageConfig(id = "timers", title = "Timers", isEnabled = true),
            PageConfig(id = "alarms", title = "Alarms", isEnabled = true),
            PageConfig(id = "stopwatch", title = "Stopwatch", isEnabled = true)
        )
    }
}
