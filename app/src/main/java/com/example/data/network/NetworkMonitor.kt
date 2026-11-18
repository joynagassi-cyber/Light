package com.example.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Observes device network connectivity via Android ConnectivityManager without requiring extra permissions
 * beyond ACCESS_NETWORK_STATE.
 */
class NetworkMonitor(context: Context) {
    private val appContext = context.applicationContext
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isOnline = MutableStateFlow(computeCurrentOnline())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOnline.value = true
        }

        override fun onLost(network: Network) {
            _isOnline.value = computeCurrentOnline()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val validated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            _isOnline.value = hasInternet && validated
        }
    }

    @Volatile
    private var registered = false

    fun start() {
        val cm = connectivityManager ?: return
        if (registered) return
        runCatching {
            cm.registerDefaultNetworkCallback(callback)
            registered = true
            _isOnline.value = computeCurrentOnline()
        }
    }

    fun stop() {
        val cm = connectivityManager ?: return
        if (!registered) return
        runCatching {
            cm.unregisterNetworkCallback(callback)
        }
        registered = false
    }

    private fun computeCurrentOnline(): Boolean {
        val cm = connectivityManager ?: return true
        return runCatching {
            val active = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(active) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }.getOrDefault(true)
    }
}
