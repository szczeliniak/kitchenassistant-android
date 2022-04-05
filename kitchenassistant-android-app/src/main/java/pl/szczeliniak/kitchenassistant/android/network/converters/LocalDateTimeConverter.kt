package pl.szczeliniak.kitchenassistant.android.network.converters

import com.google.gson.*
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateTimeUtils
import java.lang.reflect.Type
import java.time.LocalDateTime

class LocalDateTimeConverter : JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDateTime? {
        json?.asString?.let {
            return LocalDateTimeUtils.parse(it)
        }
        return null
    }

    override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {
        src?.let {
            return JsonPrimitive(LocalDateTimeUtils.stringify(it))
        }
        return null
    }

}