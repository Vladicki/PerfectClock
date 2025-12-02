
package com.griffith.perfectclock

interface GridItem {
    val id: String
    val x: Int
    val y: Int
    val width: Int
    val height: Int

    fun copyWithNewGridValues(x: Int, y: Int, width: Int, height: Int): GridItem
}
