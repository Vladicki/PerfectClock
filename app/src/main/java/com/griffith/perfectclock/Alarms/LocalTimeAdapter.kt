package com.griffith.perfectclock.Alarms

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalTime

class LocalTimeAdapter : TypeAdapter<LocalTime>() {
    override fun write(out: JsonWriter, value: LocalTime?) {
        out.value(value?.toString())
    }

    override fun read(input: JsonReader): LocalTime? {
        return if (input.peek() == null) {
            null
        } else {
            LocalTime.parse(input.nextString())
        }
    }
}
