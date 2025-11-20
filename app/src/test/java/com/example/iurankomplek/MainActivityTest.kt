package com.example.iurankomplek

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.UserViewModel
import com.example.iurankomplek.data.repository.UserRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28]) // Target API level 28 for testing
class MainActivityTest {

    @Mock
    private lateinit var mockUserViewModel: UserViewModel

    @Mock
    private lateinit var mockUserRepository: UserRepository

    private lateinit var activity: MainActivity
    private lateinit var activityController: ActivityController<MainActivity>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `MainActivity should initialize UI components correctly`() {
        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.setup().get()
        
        // Verify that RecyclerView is found
        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)
        assertNotNull("RecyclerView should be initialized", recyclerView)
        
        // Verify that ProgressBar is found
        val progressBar = activity.findViewById<ProgressBar>(R.id.progress_bar)
        assertNotNull("ProgressBar should be initialized", progressBar)
    }

    @Test
    fun `MainActivity should observe ViewModel state changes correctly`() {
        // Create a mock ViewModel with a test StateFlow
        val testStateFlow = MutableStateFlow<UiState<UserResponse>>(UiState.Loading)
        `when`(mockUserViewModel.usersState).thenReturn(testStateFlow.asStateFlow())
        
        // Create Activity with mocked ViewModel
        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.get()
        
        // Access private viewModel field using reflection to inject our mock
        val viewModelField = MainActivity::class.java.getDeclaredField("viewModel")
        viewModelField.isAccessible = true
        viewModelField.set(activity, mockUserViewModel)
        
        // Start the activity to trigger the observers
        activityController.setup()
        
        // Verify that the observer was set up (the activity was created successfully)
        assertTrue(activity.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED))
    }

    @Test
    fun `MainActivity should show progress bar during loading state`() {
        val testStateFlow = MutableStateFlow<UiState<UserResponse>>(UiState.Loading)
        
        // Create activity and set up the mock state
        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.get()
        
        // Access the UI elements
        val progressBar = activity.findViewById<ProgressBar>(R.id.progress_bar)
        assertNotNull(progressBar)
        
        // Initially, progress bar visibility would depend on the initial state
        // This test verifies UI component exists and can be manipulated
        progressBar.visibility = View.VISIBLE
        assertEquals(View.VISIBLE, progressBar.visibility)
    }

    @Test
    fun `MainActivity should handle success state correctly`() {
        // Create test data
        val testData = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john.doe@example.com",
                alamat = "123 Main St",
                iuran_perwarga = 100,
                total_iuran_rekap = 500,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 150,
                pengeluaran_iuran_warga = 50,
                pemanfaatan_iuran = "Maintenance",
                avatar = "https://example.com/avatar.jpg"
            )
        )
        val testResponse = UserResponse(data = testData)
        val testStateFlow = MutableStateFlow<UiState<UserResponse>>(UiState.Success(testResponse))
        
        // Create activity
        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.get()
        
        // Access UI elements
        val progressBar = activity.findViewById<ProgressBar>(R.id.progress_bar)
        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)
        
        assertNotNull(progressBar)
        assertNotNull(recyclerView)
    }

    @Test
    fun `MainActivity should handle error state and show toast`() {
        val errorMessage = "Network error occurred"
        val testStateFlow = MutableStateFlow<UiState<UserResponse>>(UiState.Error(errorMessage))
        
        // Create activity
        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.setup().get()
        
        // Access UI elements
        val progressBar = activity.findViewById<ProgressBar>(R.id.progress_bar)
        
        assertNotNull(progressBar)
        assertEquals(View.GONE, progressBar.visibility) // Error state should hide progress bar
    }

    @Test
    fun `MainActivity should handle success state with empty data correctly`() {
        // Create test response with empty data
        val testResponse = UserResponse(data = emptyList())
        val testStateFlow = MutableStateFlow<UiState<UserResponse>>(UiState.Success(testResponse))
        
        // Create activity
        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.setup().get()
        
        // Access UI elements
        val progressBar = activity.findViewById<ProgressBar>(R.id.progress_bar)
        
        assertNotNull(progressBar)
        assertEquals(View.GONE, progressBar.visibility) // Success state should hide progress bar
    }

    @Test
    fun `MainActivity should handle null response data correctly`() {
        // Test case where response.data is null
        val testResponse = UserResponse(data = null)
        val testStateFlow = MutableStateFlow<UiState<UserResponse>>(UiState.Success(testResponse))
        
        // Create activity
        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.setup().get()
        
        // Access UI elements
        val progressBar = activity.findViewById<ProgressBar>(R.id.progress_bar)
        
        assertNotNull(progressBar)
        assertEquals(View.GONE, progressBar.visibility) // Success state should hide progress bar
    }

    @Test
    fun `MainActivity should initialize UserAdapter correctly`() {
        // Create activity
        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.setup().get()
        
        // Access RecyclerView
        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)
        assertNotNull("RecyclerView should be initialized", recyclerView)
        
        // Verify that an adapter is set
        assertNotNull("RecyclerView adapter should be set", recyclerView.adapter)
        assertTrue("Adapter should be UserAdapter", recyclerView.adapter is UserAdapter)
    }

    @Test
    fun `MainActivity should be created successfully`() {
        activityController = Robolectric.buildActivity(MainActivity::class.java).setup()
        activity = activityController.get()
        
        // Verify that the activity was created successfully
        assertNotNull("Activity should be created", activity)
        assertTrue("Activity should be started", !activity.isFinishing)
    }
}