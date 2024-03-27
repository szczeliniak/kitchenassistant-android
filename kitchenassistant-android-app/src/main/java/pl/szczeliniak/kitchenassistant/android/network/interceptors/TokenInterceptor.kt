package pl.szczeliniak.kitchenassistant.android.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService

class TokenInterceptor(
    private var localStorageService: LocalStorageService,
) : Interceptor {

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        const val NO_AUTHENTICATION_HEADER = "No-Authentication"
        private const val TOKEN_TYPE_HEADER = "Token-Type"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.header(NO_AUTHENTICATION_HEADER) == null || request.header(NO_AUTHENTICATION_HEADER) == "false") {
            val type =
                if (request.header(TOKEN_TYPE_HEADER) != null && request.header(TOKEN_TYPE_HEADER) == "REFRESH") LocalStorageService.TokenType.REFRESH else LocalStorageService.TokenType.ACCESS
            localStorageService.getToken(type)
                .let { request = request.newBuilder().addHeader(AUTHORIZATION_HEADER, "$it").build() }
        }
        return chain.proceed(request)
    }

}
