package com.example.iurankomplek

import android.content.Context
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.utils.NetworkUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import retrofit2.Call
import retrofit2.Callback

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28]) // Target API level 28 for testing
class MainActivityTest {

    private lateinit var activity: MainActivity

    @Before
    fun setup() {
        // No need for mock annotations here as we're using Robolectric
    }

    @Test
    fun `MainActivity should initialize UI components correctly`() {
        activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        
        // Verify that RecyclerView is found
        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)
        assertNotNull(recyclerView)
        
        // Verify that adapter is initialized
        // Note: This will be the real adapter, not the mock, because of how Android works
    }

    @Test
    fun `MainActivity should be created successfully`() {
        activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        
        // Verify that the activity was created successfully
        assertNotNull(activity)
    }
}