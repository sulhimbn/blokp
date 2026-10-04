package com.example.iurankomplek.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtils {
    
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return hasUsableTransport(networkCapabilities)
    }

    fun hasUsableTransport(capabilities: NetworkCapabilities?): Boolean {
        if (capabilities == null) return false
        return SUPPORTED_TRANSPORTS.any { capabilities.hasTransport(it) }
    }

    fun isSupportedTransport(transport: Int): Boolean = transport in SUPPORTED_TRANSPORTS

    val SUPPORTED_TRANSPORTS = listOf(
        NetworkCapabilities.TRANSPORT_WIFI,
        NetworkCapabilities.TRANSPORT_CELLULAR,
        NetworkCapabilities.TRANSPORT_ETHERNET,
        NetworkCapabilities.TRANSPORT_VPN
    )
}