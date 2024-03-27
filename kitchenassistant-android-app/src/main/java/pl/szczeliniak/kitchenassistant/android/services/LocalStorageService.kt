package pl.szczeliniak.kitchenassistant.android.services

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import io.jsonwebtoken.*
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.exceptions.KitchenAssistantException

class LocalStorageService(private val context: Context) {

    companion object {
        private const val SHARED_PREFS_NAME = "kitchenassistant-shared-prefs"
        private const val ACCESS_TOKEN = "ACCESS_TOKEN"
        private const val REFRESH_TOKEN = "REFRESH_TOKEN"
        private const val TOKEN_TYPE = "TOKEN_TYPE"
        private const val EMAIL = "EMAIL"
        private const val ID = "ID"
        private const val ACCESS_VALID_TO = "ACCESS_VALID_TO"
        private const val REFRESH_VALID_TO = "REFRESH_VALID_TO"

        const val CLAIM_KEY_ID = "id"
        const val CLAIM_KEY_EMAIL = "email"
        const val CLAIM_KEY_TOKEN_TYPE = "type"
    }

    private val jwtParser: JwtParser = Jwts.parser().setSigningKey(context.getString(R.string.jwt_secret))

    fun isLoggedIn(): Boolean {
        return openSharedPrefs().contains(ACCESS_TOKEN)
    }

    fun login(accessToken: String, refreshToken: String) {
        val accessTokenBody = parseToken(accessToken)
        val refreshTokenBody = parseToken(refreshToken)
        val editor = openSharedPrefs().edit()
        editor.clear()
        editor.putString(ACCESS_TOKEN, accessToken)
        editor.putString(REFRESH_TOKEN, refreshToken)
        editor.putInt(ID, accessTokenBody[CLAIM_KEY_ID] as Int)
        editor.putString(EMAIL, accessTokenBody[CLAIM_KEY_EMAIL] as String)
        editor.putString(TOKEN_TYPE, TokenType.valueOf(accessTokenBody[CLAIM_KEY_TOKEN_TYPE] as String).name)
        editor.putLong(ACCESS_VALID_TO, accessTokenBody.expiration.time)
        editor.putLong(REFRESH_VALID_TO, refreshTokenBody.expiration.time)
        editor.commit()
    }

    private fun parseToken(token: String): Claims {
        try {
            return jwtParser.parseClaimsJws(token).body
        } catch (e: ExpiredJwtException) {
            throw KitchenAssistantException("JWT_EXPIRED_TOKEN")
        } catch (e: MalformedJwtException) {
            throw KitchenAssistantException("JWT_MALFORMED_TOKEN")
        } catch (e: KitchenAssistantException) {
            throw KitchenAssistantException("JWT_GENERIC_ERROR")
        }
    }

    fun logout(): Boolean {
        val editor = openSharedPrefs().edit()
        editor.clear()
        editor.apply()
        return true
    }

    fun getToken(tokenType: TokenType): String? {
        return openSharedPrefs().getString(if (tokenType == TokenType.ACCESS) ACCESS_TOKEN else REFRESH_TOKEN, null)
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

    enum class TokenType {
        ACCESS,
        REFRESH
    }

}