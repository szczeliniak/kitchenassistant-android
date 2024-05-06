package pl.szczeliniak.cookbook.android.network.interceptors

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NetworkConnectionChecker @Inject constructor(@ApplicationContext private val context: Context) {

    fun check(): Boolean {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?)?.run {
            getNetworkCapabilities(activeNetwork)?.run {
                return hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            }
        }
        return false
    }

}