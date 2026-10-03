package com.example.iurankomplek

import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.di.UserRepositoryModule
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.presentation.adapter.UserAdapter
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

/**
 * MainActivity is @AndroidEntryPoint and calls viewModel.loadUsers() from onCreate,
 * so these tests swap the user repository and let the activity drive itself.
 */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@UninstallModules(UserRepositoryModule::class)
@Config(sdk = [28], application = HiltTestApplication::class)
class MainActivityTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val userRepository: UserRepository = mock()

    private lateinit var activity: MainActivity
    private lateinit var activityController: ActivityController<MainActivity>

    @Before
    fun setup() {
        hiltRule.inject()
    }

    private fun launch(): MainActivity {
        activityController = Robolectric.buildActivity(MainActivity::class.java).setup()
        activity = activityController.get()
        shadowOfMainLooper()
        return activity
    }

    private fun shadowOfMainLooper() {
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
    }

    // getUsers() is a suspend function, so stubbing it has to run in a coroutine.
    private fun stubUsers(result: Result<UserResponse>) = runBlocking {
        whenever(userRepository.getUsers()).thenReturn(result)
    }

    private fun user(name: String) = DataItem(
        first_name = name,
        last_name = "Doe",
        email = "${name.lowercase()}@example.com",
        alamat = "123 Main St",
        iuran_perwarga = 100,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = 50,
        pemanfaatan_iuran = "Maintenance",
        avatar = "https://example.com/avatar.jpg"
    )

    @Test
    fun `MainActivity should initialize UI components correctly`() {
        stubUsers(Result.success(UserResponse(data = emptyList())))
        val host = launch()

        val recyclerView = host.findViewById<RecyclerView>(R.id.rv_users)
        assertNotNull("RecyclerView should be initialized", recyclerView)

        val progressBar = host.findViewById<ProgressBar>(R.id.progressBar)
        assertNotNull("ProgressBar should be initialized", progressBar)

        assertTrue("Adapter should be UserAdapter", recyclerView.adapter is UserAdapter)
    }

    @Test
    fun `MainActivity should show users when the repository succeeds`() {
        stubUsers(Result.success(UserResponse(data = listOf(user("John"), user("Jane")))))
        val host = launch()

        val progressBar = host.findViewById<ProgressBar>(R.id.progressBar)
        assertEquals("Progress bar should hide on success", View.GONE, progressBar.visibility)

        val recyclerView = host.findViewById<RecyclerView>(R.id.rv_users)
        assertEquals("Both users should be bound", 2, recyclerView.adapter?.itemCount)
    }

    @Test
    fun `MainActivity should handle success state with empty data correctly`() {
        stubUsers(Result.success(UserResponse(data = emptyList())))
        val host = launch()

        val progressBar = host.findViewById<ProgressBar>(R.id.progressBar)
        assertEquals(View.GONE, progressBar.visibility)

        val recyclerView = host.findViewById<RecyclerView>(R.id.rv_users)
        assertEquals(0, recyclerView.adapter?.itemCount)
    }

    @Test
    fun `MainActivity should handle error state without showing a progress bar`() {
        stubUsers(Result.failure(java.io.IOException("Network error occurred")))
        val host = launch()

        val progressBar = host.findViewById<ProgressBar>(R.id.progressBar)
        assertNotNull(progressBar)
        assertEquals("Progress bar should hide on error", View.GONE, progressBar.visibility)

        val recyclerView = host.findViewById<RecyclerView>(R.id.rv_users)
        assertEquals("No users should be bound on error", 0, recyclerView.adapter?.itemCount)
    }

    @Test
    fun `MainActivity should request users exactly once on create`() {
        stubUsers(Result.success(UserResponse(data = emptyList())))
        launch()

        runBlocking {
            org.mockito.Mockito.verify(userRepository, org.mockito.Mockito.times(1)).getUsers()
        }
    }
}
