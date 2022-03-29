package pl.szczeliniak.kitchenassistant.android.utils

import java.util.regex.Pattern

class ValidationUtils {

    companion object {
        private val EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)

        fun isEmail(email: String): Boolean {
            return EMAIL_PATTERN.matcher(email).matches()
        }
    }

}

