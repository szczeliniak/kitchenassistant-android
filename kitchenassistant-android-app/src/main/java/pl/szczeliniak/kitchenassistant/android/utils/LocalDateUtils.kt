package pl.szczeliniak.kitchenassistant.android.utils

import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    }

}


