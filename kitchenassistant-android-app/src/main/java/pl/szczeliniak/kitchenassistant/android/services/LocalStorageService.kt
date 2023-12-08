package pl.szczeliniak.kitchenassistant.android.services

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import pl.szczeliniak.kitchenassistant.android.utils.ZonedDateTimeUtils
import java.time.ZonedDateTime

class LocalStorageService(private val context: Context) {

    companion object {
        private const val SHARED_PREFS_NAME = "kitchenassistant-shared-prefs"
        private const val TOKEN = "TOKEN"
        private const val EMAIL = "EMAIL"
        private const val ID = "ID"
        private const val VALID_TO = "VALID_TO"
    }

    fun isLoggedIn(): Boolean {
        return openSharedPrefs().contains(TOKEN)
    }

    fun login(token: String, email: String?, id: Int, validTo: ZonedDateTime) {
        val editor = openSharedPrefs().edit()
        editor.clear()
        editor.putString(TOKEN, token)
        editor.putInt(ID, id)
        email?.let { editor.putString(EMAIL, it) }
        editor.putString(VALID_TO, ZonedDateTimeUtils.stringify(validTo))
        editor.commit()
    }

    fun logout(): Boolean {
        val editor = openSharedPrefs().edit()
        editor.clear()
        editor.apply()
        return true
    }

    fun getToken(): String? {
        return openSharedPrefs().getString(TOKEN, null)
    }

    fun getId(): Int {
        return openSharedPrefs().getInt(ID, -1)
    }

    fun getEmail(): String? {
        return openSharedPrefs().getString(EMAIL, null)
    }

    private fun openSharedPrefs(): SharedPreferences {
        return context.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
    }

}