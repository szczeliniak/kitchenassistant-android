package pl.szczeliniak.cookbook.android.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import pl.szczeliniak.cookbook.android.exceptions.CookBookNetworkException

class NetworkCheckInterceptor (private val networkConnectionChecker: NetworkConnectionChecker) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!networkConnectionChecker.check()) {
            throw CookBookNetworkException()
        }
        return chain.proceed(chain.request())
    }

}
