package com.griffith.perfectclock

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

data class GridLayoutConfig(
    var columns: Int = 4,
    var rows: Int = 5,
    var showEdges: Boolean = false
)

class GridConfigStorage(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("grid_config_storage", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveGridConfig(config: GridLayoutConfig) {
        val json = gson.toJson(config)
        sharedPreferences.edit().putString("grid_config", json).apply()
    }

    fun loadGridConfig(): GridLayoutConfig {
        val json = sharedPreferences.getString("grid_config", null)
        return if (json != null) {
            gson.fromJson(json, GridLayoutConfig::class.java)
        } else {
            GridLayoutConfig() // Default configuration
        }
    }
}
