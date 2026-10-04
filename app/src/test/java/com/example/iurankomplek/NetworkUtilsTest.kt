package com.example.iurankomplek

import android.app.Application
import android.content.Context
import android.net.NetworkCapabilities
import com.example.iurankomplek.utils.NetworkUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
class NetworkUtilsTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun `the connectivity service is reachable from the app context`() {
        assertNotNull(context.getSystemService(Context.CONNECTIVITY_SERVICE))
    }

    @Test
    fun `wifi cellular ethernet and vpn are accepted`() {
        assertTrue(NetworkUtils.isSupportedTransport(NetworkCapabilities.TRANSPORT_WIFI))
        assertTrue(NetworkUtils.isSupportedTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        assertTrue(NetworkUtils.isSupportedTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        assertTrue(NetworkUtils.isSupportedTransport(NetworkCapabilities.TRANSPORT_VPN))
    }

    @Test
    fun `bluetooth and lowpan are rejected`() {
        assertFalse(NetworkUtils.isSupportedTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH))
        assertFalse(NetworkUtils.isSupportedTransport(NetworkCapabilities.TRANSPORT_LOWPAN))
    }

    @Test
    fun `an unknown transport code is rejected`() {
        assertFalse(NetworkUtils.isSupportedTransport(Int.MAX_VALUE))
        assertFalse(NetworkUtils.isSupportedTransport(Int.MIN_VALUE))
        assertFalse(NetworkUtils.isSupportedTransport(-1))
        assertFalse(NetworkUtils.isSupportedTransport(2))
        assertFalse(NetworkUtils.isSupportedTransport(5))
        assertFalse(NetworkUtils.isSupportedTransport(6))
    }

    @Test
    fun `transport code zero is cellular and is therefore accepted`() {
        assertEquals(0, NetworkCapabilities.TRANSPORT_CELLULAR)
        assertTrue(NetworkUtils.isSupportedTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    @Test
    fun `the accepted set is exactly the four supported transports`() {
        assertEquals(
            listOf(
                NetworkCapabilities.TRANSPORT_WIFI,
                NetworkCapabilities.TRANSPORT_CELLULAR,
                NetworkCapabilities.TRANSPORT_ETHERNET,
                NetworkCapabilities.TRANSPORT_VPN
            ),
            NetworkUtils.SUPPORTED_TRANSPORTS
        )
    }

    @Test
    fun `missing capabilities mean the network is not usable`() {
        assertFalse(NetworkUtils.hasUsableTransport(null))
    }

    @Test
    fun `isNetworkAvailable is callable and never throws`() {
        val result = runCatching { NetworkUtils.isNetworkAvailable(context) }

        assertNotNull("isNetworkAvailable must return rather than throw", result.getOrNull())
        assertTrue(
            "isNetworkAvailable must return a Boolean but returned ${result.getOrNull()}",
            result.getOrNull() is Boolean
        )
    }
}
