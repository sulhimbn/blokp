package com.example.iurankomplek
import android.app.Application

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.example.iurankomplek.utils.NetworkUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class NetworkUtilsTest {

    private fun contextWith(connectivityManager: ConnectivityManager?): Context {
        val context = mock(Context::class.java)
        `when`(context.getSystemService(Context.CONNECTIVITY_SERVICE))
            .thenReturn(connectivityManager)
        return context
    }

    private fun connectivityWith(vararg transports: Int): ConnectivityManager {
        val manager = mock(ConnectivityManager::class.java)
        val network = mock(Network::class.java)
        val capabilities = mock(NetworkCapabilities::class.java)
        `when`(manager.activeNetwork).thenReturn(network)
        `when`(manager.getNetworkCapabilities(network)).thenReturn(capabilities)
        transports.forEach { transport ->
            `when`(capabilities.hasTransport(transport)).thenReturn(true)
        }
        return manager
    }

    @Test
    fun `reports available when wifi is the active transport`() {
        val context = contextWith(connectivityWith(NetworkCapabilities.TRANSPORT_WIFI))

        assertTrue(NetworkUtils.isNetworkAvailable(context))
    }

    @Test
    fun `reports available when cellular is the active transport`() {
        val context = contextWith(connectivityWith(NetworkCapabilities.TRANSPORT_CELLULAR))

        assertTrue(NetworkUtils.isNetworkAvailable(context))
    }

    @Test
    fun `reports available over ethernet`() {
        val context = contextWith(connectivityWith(NetworkCapabilities.TRANSPORT_ETHERNET))

        assertTrue(NetworkUtils.isNetworkAvailable(context))
    }

    @Test
    fun `reports available over vpn`() {
        val context = contextWith(connectivityWith(NetworkCapabilities.TRANSPORT_VPN))

        assertTrue(NetworkUtils.isNetworkAvailable(context))
    }

    @Test
    fun `reports unavailable when the network exposes an unrelated transport`() {
        val context = contextWith(connectivityWith(NetworkCapabilities.TRANSPORT_LOWPAN))

        assertFalse(NetworkUtils.isNetworkAvailable(context))
    }

    @Test
    fun `reports unavailable when there is no active network`() {
        val manager = mock(ConnectivityManager::class.java)
        `when`(manager.activeNetwork).thenReturn(null)

        assertFalse(NetworkUtils.isNetworkAvailable(contextWith(manager)))
    }

    @Test
    fun `reports unavailable when capabilities cannot be resolved`() {
        val manager = mock(ConnectivityManager::class.java)
        val network = mock(Network::class.java)
        `when`(manager.activeNetwork).thenReturn(network)
        `when`(manager.getNetworkCapabilities(any())).thenReturn(null)

        assertFalse(NetworkUtils.isNetworkAvailable(contextWith(manager)))
    }

    @Test
    fun `works against the real application context without throwing`() {
        val context = RuntimeEnvironment.getApplication().applicationContext

        NetworkUtils.isNetworkAvailable(context)
    }
}
