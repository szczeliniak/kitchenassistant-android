package pl.szczeliniak.kitchenassistant.android.network.converters

import com.google.gson.*
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateUtils
import java.lang.reflect.Type
import java.time.LocalDate

class LocalDateConverter : JsonDeserializer<LocalDate>, JsonSerializer<LocalDate> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDate? {
        json?.asString?.let {
            return LocalDateUtils.parse(it)
        }
        return null
    }

    override fun serialize(src: LocalDate?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {
        src?.let {
            return JsonPrimitive(LocalDateUtils.stringify(it))
        }
        return null
    }

}