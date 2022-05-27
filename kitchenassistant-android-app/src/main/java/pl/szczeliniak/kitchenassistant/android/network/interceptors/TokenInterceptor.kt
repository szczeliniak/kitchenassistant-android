package pl.szczeliniak.kitchenassistant.android.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService

class TokenInterceptor constructor(
    private var localStorageService: LocalStorageService,
) : Interceptor {

    companion object {
        private var AUTHORIZATION_HEADER = "X-Token"
        var NO_AUTHENTICATION_HEADER = "No-Authentication"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.header(NO_AUTHENTICATION_HEADER) == null || request.header(NO_AUTHENTICATION_HEADER) == "false") {
            localStorageService.getToken().let {
                request = request.newBuilder()
                    .addHeader(AUTHORIZATION_HEADER, "$it")
                    .build()
            }
        }
        return chain.proceed(request)
    }

}
