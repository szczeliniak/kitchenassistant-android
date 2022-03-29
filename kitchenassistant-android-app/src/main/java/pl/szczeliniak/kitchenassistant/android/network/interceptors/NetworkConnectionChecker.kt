package pl.szczeliniak.kitchenassistant.android.network.interceptors

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NetworkConnectionChecker @Inject constructor(@ApplicationContext private val ctx: Context) {

    fun check(): Boolean {
        val connectionManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        for (network in connectionManager.allNetworks) {
            val capabilities = connectionManager.getNetworkCapabilities(network)
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        }
        return false
    }

}