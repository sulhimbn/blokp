package com.example.iurankomplek

import androidx.test.core.app.ApplicationProvider
import com.example.iurankomplek.utils.NetworkUtils
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class NetworkUtilsTest {

    @Test
    fun `isNetworkAvailable should not throw when called`() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()

        try {
            NetworkUtils.isNetworkAvailable(context)
        } catch (e: Exception) {
            fail("NetworkUtils.isNetworkAvailable should not throw: ${e.message}")
        }
    }

    @Test
    fun `isNetworkAvailable should return a boolean under Robolectric`() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()

        val result = NetworkUtils.isNetworkAvailable(context)

        assertTrue("isNetworkAvailable must return a Boolean", result is Boolean)
    }
}
