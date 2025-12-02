package com.griffith.perfectclock

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(private val context: Context, private val onShake: () -> Unit) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var acceleration = 0.0f
    private var currentAcceleration = SensorManager.GRAVITY_EARTH
    private var lastAcceleration = SensorManager.GRAVITY_EARTH

    private val SHAKE_THRESHOLD_GRAVITY = 2.7f // Adjust this value to change sensitivity
    private val SHAKE_SLOP_TIME_MS = 500 // Time before shake count resets
    private val SHAKE_COUNT_RESET_TIME_MS = 3000 // Total time for a sequence of shakes
    private val SHAKE_MIN_MOVEMENTS = 3 // Minimum number of movements to count as a shake

    private var shakeTimestamp: Long = 0
    private var shakeCount: Int = 0

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]

            lastAcceleration = currentAcceleration
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta // Low-pass filter

            if (acceleration > SHAKE_THRESHOLD_GRAVITY) {
                val now = System.currentTimeMillis()
                if (shakeTimestamp == 0L || (now - shakeTimestamp > SHAKE_SLOP_TIME_MS && now - shakeTimestamp < SHAKE_COUNT_RESET_TIME_MS)) {
                    shakeTimestamp = now
                    shakeCount++
                    if (shakeCount >= SHAKE_MIN_MOVEMENTS) {
                        onShake()
                        shakeCount = 0 // Reset after shake detected
                        shakeTimestamp = 0L // Reset timestamp
                    }
                } else if (now - shakeTimestamp > SHAKE_COUNT_RESET_TIME_MS) {
                    // Reset if too much time passes between shakes
                    shakeCount = 0
                    shakeTimestamp = 0L
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}
