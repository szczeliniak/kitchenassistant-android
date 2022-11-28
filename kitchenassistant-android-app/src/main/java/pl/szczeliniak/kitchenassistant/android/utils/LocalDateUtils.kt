package pl.szczeliniak.kitchenassistant.android.utils

import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class LocalDateUtils {

    companion object {

        private const val LOCAL_DATE_FORMAT = "yyyy-MM-dd"

        private val LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern(LOCAL_DATE_FORMAT)

        fun parse(dateTimeAsString: String?): LocalDate? {
            if (dateTimeAsString.isNullOrEmpty()) {
                return null
            }
            try {
                return LocalDate.parse(dateTimeAsString, LOCAL_DATE_FORMATTER)
            } catch (e: Exception) {
                Timber.i(e, "Cannot parse local date: $dateTimeAsString")
            }
            return null
        }

        fun parsable(dateTimeAsString: String?): Boolean {
            if (dateTimeAsString.isNullOrEmpty()) {
                return false
            }
            try {
                LocalDate.parse(dateTimeAsString, LOCAL_DATE_FORMATTER)
                return true
            } catch (_: DateTimeParseException) {
            }
            return false
        }

        fun stringify(dateTime: LocalDate?): String? {
            if (dateTime == null) {
                return null
            }
            try {
                return LOCAL_DATE_FORMATTER.format(dateTime)
            } catch (e: Exception) {
                Timber.i(e, "Cannot stringify LocalDate: $dateTime")
            }
            return null
        }

        fun toMillis(year: Int, month: Int, day: Int): Long {
            return toMillis(LocalDate.of(year, month + 1, day))
        }

        fun toMillis(date: LocalDate): Long {
            return date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        }

        fun toLocalDate(millis: Long): LocalDate {
            return Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate()
        }
    }

}


