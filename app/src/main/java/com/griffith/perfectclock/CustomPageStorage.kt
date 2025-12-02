package com.griffith.perfectclock

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.griffith.perfectclock.Alarm

class CustomPageStorage(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("custom_page_prefs", Context.MODE_PRIVATE)
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(GridItem::class.java, CustomGridItemAdapter())
        .create()

    fun saveCustomGridItems(items: List<GridItem>) {
        val json = gson.toJson(items)
        sharedPreferences.edit().putString("custom_grid_items", json).apply()
    }

    fun loadCustomGridItems(): MutableList<GridItem> {
        val json = sharedPreferences.getString("custom_grid_items", null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<MutableList<GridItem>>() {}.type)
        } else {
            mutableListOf()
        }
    }
}
