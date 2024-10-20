package com.makinul.background.remover.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NetworkUtil @Inject constructor(
    @ApplicationContext val appContext: Context
) {
    private fun isConnectedToInternet(): Boolean {
        val connectivityManager =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?.let { networkCapabilities ->
                return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            }
        return false
    }

    fun isInternetAvailable(): Boolean {
        if (isConnectedToInternet()) {
            val command = "ping -c 1 google.com"
            try {
                return Runtime.getRuntime().exec(command).waitFor() == 0
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }
}