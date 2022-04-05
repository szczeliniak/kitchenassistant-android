package pl.szczeliniak.kitchenassistant.android.utils

import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeUtils {

    companion object {

        private const val LOCAL_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
        private val LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_FORMAT)

        fun parse(dateTimeAsString: String?): LocalDateTime? {
            if (dateTimeAsString.isNullOrEmpty()) {
                return null
            }
            try {
                return LocalDateTime.parse(dateTimeAsString, LOCAL_DATE_TIME_FORMATTER)
            } catch (e: Exception) {
                Timber.i(e, "Cannot parse local date: $dateTimeAsString")
            }
            return null
        }

        fun stringify(dateTime: LocalDateTime?): String? {
            if (dateTime == null) {
                return null
            }
            try {
                return LOCAL_DATE_TIME_FORMATTER.format(dateTime)
            } catch (e: Exception) {
                Timber.i(e, "Cannot stringify LocalDateTime: $dateTime")
            }
            return null
        }

    }

}


