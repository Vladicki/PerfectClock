package com.griffith.perfectclock

import com.google.gson.*
import java.lang.reflect.Type

// Wrapper class to hold a GridItem and its type for JSON serialization
data class CustomGridItemWrapper(
    val type: String,
    val item: GridItem
)

// Custom TypeAdapter for GridItem polymorphism
class CustomGridItemAdapter : JsonSerializer<GridItem>, JsonDeserializer<GridItem> {
    override fun serialize(
        src: GridItem?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        if (src == null || context == null) return JsonNull.INSTANCE

        val type = when (src) {
            is Alarm -> "Alarm"
            is Timer -> "Timer"
            else -> throw IllegalArgumentException("Unknown GridItem type: ${src.javaClass.name}")
        }

        val jsonObject = JsonObject()
        jsonObject.addProperty("type", type)
        jsonObject.add("item", context.serialize(src))
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): GridItem {
        if (json == null || context == null || !json.isJsonObject) {
            throw JsonParseException("Cannot deserialize GridItem from null or non-object JSON")
        }

        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type")?.asString
            ?: throw JsonParseException("Missing 'type' field in GridItem JSON")
        val itemElement = jsonObject.get("item")
            ?: throw JsonParseException("Missing 'item' field in GridItem JSON")

        return when (type) {
            "Alarm" -> context.deserialize(itemElement, Alarm::class.java)
            "Timer" -> context.deserialize(itemElement, Timer::class.java)
            else -> throw JsonParseException("Unknown GridItem type: $type")
        }
    }
}
