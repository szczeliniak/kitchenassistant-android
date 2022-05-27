package pl.szczeliniak.kitchenassistant.android.network.converters

import com.google.gson.*
import pl.szczeliniak.kitchenassistant.android.utils.ZonedDateTimeUtils
import java.lang.reflect.Type
import java.time.ZonedDateTime

class ZonedDateTimeConverter : JsonDeserializer<ZonedDateTime>, JsonSerializer<ZonedDateTime> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ZonedDateTime? {
        return json?.asString?.let {
            return ZonedDateTimeUtils.parse(it)
        }
    }

    override fun serialize(src: ZonedDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {
        return src?.let {
            return JsonPrimitive(ZonedDateTimeUtils.stringify(it))
        }
    }

}