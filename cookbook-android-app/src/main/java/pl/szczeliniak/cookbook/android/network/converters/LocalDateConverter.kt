package pl.szczeliniak.cookbook.android.network.converters

import com.google.gson.*
import pl.szczeliniak.cookbook.android.utils.LocalDateUtils
import java.lang.reflect.Type
import java.time.LocalDate

class LocalDateConverter : JsonDeserializer<LocalDate>, JsonSerializer<LocalDate> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDate? {
        return json?.asString?.let {
            return LocalDateUtils.parse(it)
        }
    }

    override fun serialize(src: LocalDate?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {
        return src?.let {
            return JsonPrimitive(LocalDateUtils.stringify(it))
        }
    }

}