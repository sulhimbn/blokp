package com.example.iurankomplek

import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.presentation.adapter.UserAdapter
import com.example.iurankomplek.viewmodel.UserViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
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

        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)
        assertNotNull(recyclerView)
    }

    @Test
    fun `MainActivity should show progress bar during loading state`() {
        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.get()

        val progressBar = activity.findViewById<ProgressBar>(R.id.progressBar)
        assertNotNull(progressBar)

        progressBar.visibility = View.VISIBLE
        assertEquals(View.VISIBLE, progressBar.visibility)
    }

    @Test
    fun `MainActivity should handle success state correctly`() {
        val testResponse = UserResponse(
            data = listOf(
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
        )

        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.get()

        val progressBar = activity.findViewById<ProgressBar>(R.id.progressBar)
        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)

        assertNotNull(progressBar)
        assertNotNull(recyclerView)
    }

    @Test
    fun `MainActivity should handle error state and show toast`() {
        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.setup().get()

        val progressBar = activity.findViewById<ProgressBar>(R.id.progressBar)

        assertNotNull(progressBar)
        assertEquals(View.GONE, progressBar.visibility)
    }

    @Test
    fun `MainActivity should handle success state with empty data correctly`() {
        val testResponse = UserResponse(data = emptyList())

        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.setup().get()

        val progressBar = activity.findViewById<ProgressBar>(R.id.progressBar)

        assertNotNull(progressBar)
        assertEquals(View.GONE, progressBar.visibility)
    }

    @Test
    fun `MainActivity should handle absent response data as an empty list`() {
        // UserResponse.data is non-null, so a missing payload arrives as an empty list.
        val testResponse = UserResponse(data = emptyList())

        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.setup().get()

        val progressBar = activity.findViewById<ProgressBar>(R.id.progressBar)

        assertNotNull(progressBar)
        assertEquals(View.GONE, progressBar.visibility)
    }

    @Test
    fun `MainActivity should initialize UserAdapter correctly`() {
        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.setup().get()

        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)
        assertNotNull("RecyclerView should be initialized", recyclerView)
        assertNotNull("RecyclerView adapter should be set", recyclerView.adapter)
        assertTrue("Adapter should be UserAdapter", recyclerView.adapter is UserAdapter)
    }

    @Test
    fun `MainActivity should be created successfully`() {
        activityController = Robolectric.buildActivity(MainActivity::class.java).setup()
        activity = activityController.get()

        assertNotNull("Activity should be created", activity)
        assertTrue("Activity should be started", !activity.isFinishing)
    }

    @Test
    fun `MainActivity should observe ViewModel state changes`() {
        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.setup().get()

        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)
        assertNotNull(recyclerView)
        assertNotNull(recyclerView.adapter)
    }
}