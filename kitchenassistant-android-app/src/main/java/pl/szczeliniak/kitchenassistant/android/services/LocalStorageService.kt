package pl.szczeliniak.kitchenassistant.android.services

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class LocalStorageService constructor(private val context: Context) {

    companion object {
        private const val SHARED_PREFS_NAME = "kitchenassistant-shared-prefs"
        private const val TOKEN = "TOKEN"
    }

    fun isLoggedIn(): Boolean {
        return openSharedPrefs().contains(TOKEN)
    }

    fun login(token: String) {
        val editor = openSharedPrefs().edit()
        editor.clear()
        editor.putString(TOKEN, token)
        editor.apply()
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

    private fun openSharedPrefs(): SharedPreferences {
        return context.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
    }

}