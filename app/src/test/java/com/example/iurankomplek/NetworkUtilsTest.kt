package com.example.iurankomplek

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import com.example.iurankomplek.utils.NetworkUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowConnectivityManager
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.shadows.ShadowNetworkCapabilities
import org.robolectric.shadows.ShadowNetworkInfo

@RunWith(RobolectricTestRunner::class)
class NetworkUtilsTest {

    private lateinit var context: Context
    private lateinit var shadowConnectivityManager: ShadowConnectivityManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        shadowConnectivityManager = Shadows.shadowOf(
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        )
        shadowConnectivityManager.clearAllNetworks()
    }

    private fun activateNetwork(capabilities: NetworkCapabilities) {
        val network = ShadowNetwork.newInstance(1)
        val networkInfo = ShadowNetworkInfo.newInstance(
            NetworkInfo.DetailedState.CONNECTED,
            ConnectivityManager.TYPE_WIFI,
            0,
            true,
            NetworkInfo.State.CONNECTED
        )
        shadowConnectivityManager.addNetwork(network, networkInfo)
        shadowConnectivityManager.setActiveNetworkInfo(networkInfo)
        shadowConnectivityManager.setDefaultNetworkActive(true)
        shadowConnectivityManager.setNetworkCapabilities(network, capabilities)
    }

    private fun withTransport(transport: Int): NetworkCapabilities =
        Shadows.shadowOf(ShadowNetworkCapabilities.newInstance()).addTransportType(transport)

    @Test
    fun `no active network reports unavailable`() {
        assertFalse(NetworkUtils.isNetworkAvailable(context))
    }

    @Test
    fun `an active network without a usable transport reports unavailable`() {
        activateNetwork(ShadowNetworkCapabilities.newInstance())

        assertFalse(NetworkUtils.isNetworkAvailable(context))
    }

    @Test
    fun `wifi transport reports available`() {
        activateNetwork(withTransport(NetworkCapabilities.TRANSPORT_WIFI))

        assertTrue(NetworkUtils.isNetworkAvailable(context))
    }

    @Test
    fun `cellular transport reports available`() {
        activateNetwork(withTransport(NetworkCapabilities.TRANSPORT_CELLULAR))

        assertTrue(NetworkUtils.isNetworkAvailable(context))
    }

    @Test
    fun `ethernet transport reports available`() {
        activateNetwork(withTransport(NetworkCapabilities.TRANSPORT_ETHERNET))

        assertTrue(NetworkUtils.isNetworkAvailable(context))
    }

    @Test
    fun `vpn transport reports available`() {
        activateNetwork(withTransport(NetworkCapabilities.TRANSPORT_VPN))

        assertTrue(NetworkUtils.isNetworkAvailable(context))
    }
}