package pl.szczeliniak.kitchenassistant.android.network.converters

import com.google.gson.*
import org.joda.time.DateTime
import pl.szczeliniak.kitchenassistant.android.utils.parseDate
import pl.szczeliniak.kitchenassistant.android.utils.stringifyDateTime
import java.lang.reflect.Type

class DateTimeConverter : JsonDeserializer<DateTime>, JsonSerializer<DateTime> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DateTime? {
        json?.asString?.let {
            return parseDate(it)
        }
        return null
    }

    override fun serialize(src: DateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {
        src?.let {
            return JsonPrimitive(stringifyDateTime(it))
        }
        return null
    }

}