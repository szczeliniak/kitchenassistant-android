package pl.szczeliniak.kitchenassistant.android.utils

import timber.log.Timber
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ZonedDateTimeUtils {

    companion object {

        private const val ZONED_DATE_TIME_FORMAT = "yyyy-MM-DD'T'HH:mm:ssZ"
        private val ZONED_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(ZONED_DATE_TIME_FORMAT)

        fun parse(dateTimeAsString: String?): ZonedDateTime? {
            if (dateTimeAsString.isNullOrEmpty()) {
                return null
            }
            try {
                return ZonedDateTime.parse(dateTimeAsString, ZONED_DATE_TIME_FORMATTER)
            } catch (e: Exception) {
                Timber.i(e, "Cannot parse ZonedDateTime: $dateTimeAsString")
            }
            return null
        }

        fun stringify(dateTime: ZonedDateTime?): String? {
            if (dateTime == null) {
                return null
            }
            try {
                return ZONED_DATE_TIME_FORMATTER.format(dateTime)
            } catch (e: Exception) {
                Timber.i(e, "Cannot stringify ZonedDateTime: $dateTime")
            }
            return null
        }

    }

}


