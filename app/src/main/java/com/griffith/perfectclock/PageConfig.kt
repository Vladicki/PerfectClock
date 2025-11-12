package com.griffith.perfectclock

import kotlinx.serialization.Serializable

@Serializable
data class PageConfig(
    val id: String,
    val title: String,
    var isEnabled: Boolean = true,
    val isCustom: Boolean = false
)
