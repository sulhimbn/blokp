package com.example.iurankomplek.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.iurankomplek.event.AppEvent
import com.example.iurankomplek.event.EventBus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStatusListener @Inject constructor(
    @ApplicationContext private val context: Context,
    private val eventBus: EventBus
) : ConnectivityManager.NetworkCallback() {
    
    private val connectivityManager = 
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private var _isConnected = false
    val isConnected: Boolean
        get() = _isConnected
    
    fun startListening() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, this)
        
        _isConnected = checkInitialNetworkState()
    }
    
    fun stopListening() {
        connectivityManager.unregisterNetworkCallback(this)
    }
    
    private fun checkInitialNetworkState(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
    
    override fun onAvailable(network: Network) {
        if (!_isConnected) {
            _isConnected = true
            scope.launch {
                eventBus.publish(AppEvent.NetworkStatusChanged(isConnected = true))
            }
        }
    }
    
    override fun onLost(network: Network) {
        if (_isConnected) {
            _isConnected = false
            scope.launch {
                eventBus.publish(AppEvent.NetworkStatusChanged(isConnected = false))
            }
        }
    }
    
    override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
    ) {
        val hasInternet = networkCapabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        )
        if (hasInternet != _isConnected) {
            _isConnected = hasInternet
            scope.launch {
                eventBus.publish(AppEvent.NetworkStatusChanged(isConnected = hasInternet))
            }
        }
    }
}
