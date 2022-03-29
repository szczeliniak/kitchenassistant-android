package pl.szczeliniak.kitchenassistant.android.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import pl.szczeliniak.kitchenassistant.android.exceptions.ReceiptsStorageNetworkException

class NetworkCheckInterceptor constructor(private val networkConnectionChecker: NetworkConnectionChecker) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!networkConnectionChecker.check()) {
            throw ReceiptsStorageNetworkException()
        }
        return chain.proceed(chain.request())
    }

}
