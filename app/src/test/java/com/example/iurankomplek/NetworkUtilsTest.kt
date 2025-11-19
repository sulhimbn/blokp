package com.example.iurankomplek

import android.content.Context
import android.net.ConnectivityManager
import com.example.iurankomplek.utils.NetworkUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import androidx.test.core.app.ApplicationProvider
import android.app.Application
import org.junit.Assert.assertThrows

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class NetworkUtilsTest {

    @Test
    fun `isNetworkAvailable should not throw exception when called`() {
        // In unit tests, we're mainly checking that the function doesn't crash
        // Since we can't easily mock the ConnectivityManager with NetworkCapabilities,
        // we'll ensure the method can be called without throwing an exception
        val context = RuntimeEnvironment.getApplication().applicationContext
        
        // This may return false (no network) but shouldn't crash
        try {
            val result = NetworkUtils.isNetworkAvailable(context)
            // If we reach this point, no exception was thrown
        } catch (e: Exception) {
            // Fail the test if an exception was thrown
            org.junit.Assert.fail("NetworkUtils.isNetworkAvailable should not throw an exception: ${e.message}")
        }
    }
}