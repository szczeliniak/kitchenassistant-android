package pl.szczeliniak.kitchenassistant.android.utils

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber

const val DATETIME_FORMAT = "dd-MM-yyyy HH:mm:ss"
const val DATE_FORMAT = "dd-MM-yyyy"

private val DATETIME_FORMATTER = DateTimeFormat.forPattern(DATETIME_FORMAT)
private val DATE_FORMATTER = DateTimeFormat.forPattern(DATE_FORMAT)

fun parseDate(dateTimeAsString: String?): DateTime? {
    if (dateTimeAsString.isNullOrEmpty()) {
        return null
    }
    try {
        return DATETIME_FORMATTER.parseDateTime(dateTimeAsString)
    } catch (e: Exception) {
        try {
            return DATE_FORMATTER.parseDateTime(dateTimeAsString)
        } catch (e: Exception) {
            Timber.i(e, "Cannot parse date time: $dateTimeAsString")
        }
    }
    return null
}

fun stringifyDateTime(dateTime: DateTime?): String? {
    if (dateTime == null) {
        return null
    }
    try {
        return DATETIME_FORMATTER.print(dateTime)
    } catch (e: Exception) {
        Timber.i(e, "Cannot stringify DateTime: $dateTime")
    }
    return null
}

fun stringifyDate(dateTime: DateTime?): String? {
    if (dateTime == null) {
        return null
    }
    try {
        return DATE_FORMATTER.print(dateTime)
    } catch (e: Exception) {
        Timber.i(e, "Cannot stringify DateTime: $dateTime")
    }
    return null
}
