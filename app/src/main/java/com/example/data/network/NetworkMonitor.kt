package com.example.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Observes Android's default Internet path independently of application backend errors. */
class NetworkMonitor(context: Context) {
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isOnline = MutableStateFlow(computeCurrentOnline())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            // The default-network callback may report a loss during a handoff.
            _isOnline.value = computeCurrentOnline(excluding = network)
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            _isOnline.value = networkCapabilities.hasValidatedInternet()
        }
    }

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
        if (!registered) return
        runCatching { connectivityManager?.unregisterNetworkCallback(callback) }
        registered = false
    }

    private fun computeCurrentOnline(excluding: Network? = null): Boolean {
        val cm = connectivityManager ?: return true // Unknown is not proof of being offline.
        return runCatching {
            val active = cm.activeNetwork ?: return false
            if (active == excluding) return false
            cm.getNetworkCapabilities(active)?.hasValidatedInternet() ?: true
        }.getOrDefault(true)
    }
}

private fun NetworkCapabilities.hasValidatedInternet(): Boolean =
    hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
