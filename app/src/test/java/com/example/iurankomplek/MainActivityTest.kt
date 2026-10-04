package com.example.iurankomplek

import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.presentation.adapter.UserAdapter
import com.example.iurankomplek.testing.FakeUserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [33])
class MainActivityTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var fakeUserRepository: FakeUserRepository

    private lateinit var activityController: ActivityController<MainActivity>
    private lateinit var activity: MainActivity

    @Before
    fun setup() {
        hiltRule.inject()
        activityController = Robolectric.buildActivity(MainActivity::class.java).setup()
        activity = activityController.get()
    }

    // UserAdapter diffs on Dispatchers.Default and applies the result on Main, so the
    // RecyclerView count settles a little after the state emission.
    private fun awaitItemCount(recyclerView: RecyclerView, expected: Int) {
        val deadline = System.currentTimeMillis() + 10_000
        while (recyclerView.adapter?.itemCount != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(25)
            shadowOf(android.os.Looper.getMainLooper()).idle()
        }
    }

    private fun sampleUsers() = listOf(
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

    @Test
    fun `activity is created with its views and adapter wired`() {
        assertNotNull("Activity should be created", activity)
        assertTrue("Activity should be started", !activity.isFinishing)

        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)
        assertNotNull("RecyclerView should be inflated", recyclerView)
        assertNotNull("ProgressBar should be inflated", activity.findViewById<ProgressBar>(R.id.progressBar))
        assertTrue("Adapter should be a UserAdapter", recyclerView.adapter is UserAdapter)
    }

    @Test
    fun `success state hides the progress bar and shows the users`() {
        fakeUserRepository.response.value = Result.success(UserResponse(data = sampleUsers()))

        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        shadowOf(activity.mainLooper).idle()

        val progressBar = activity.findViewById<ProgressBar>(R.id.progressBar)
        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)

        assertEquals("ProgressBar should be hidden once data arrived", View.GONE, progressBar.visibility)
        awaitItemCount(recyclerView, 1)
        assertEquals("RecyclerView should show one user", 1, recyclerView.adapter?.itemCount)
    }

    @Test
    fun `empty response still hides the progress bar`() {
        fakeUserRepository.response.value = Result.success(UserResponse(data = emptyList()))

        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        shadowOf(activity.mainLooper).idle()

        assertEquals(View.GONE, activity.findViewById<ProgressBar>(R.id.progressBar).visibility)
        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)
        awaitItemCount(recyclerView, 0)
        assertEquals(0, recyclerView.adapter?.itemCount)
    }

    @Test
    fun `error state hides the progress bar`() {
        fakeUserRepository.response.value = Result.failure(java.io.IOException("boom"))

        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        shadowOf(activity.mainLooper).idle()

        assertEquals(View.GONE, activity.findViewById<ProgressBar>(R.id.progressBar).visibility)
    }
}
