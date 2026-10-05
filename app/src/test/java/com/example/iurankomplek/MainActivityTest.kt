package com.example.iurankomplek

import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.presentation.adapter.UserAdapter
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

/**
 * Verifies MainActivity inflates and wires up its views. State-transition
 * behaviour (loading / success / error rendering) needs a real ViewModel and is
 * covered by MainActivityEspressoTest on device, where the ViewModel can be fed
 * a live MockWebServer.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MainActivityTest {

    private lateinit var activityController: ActivityController<MainActivity>
    private lateinit var activity: MainActivity

    @Before
    fun setup() {
        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.setup().get()
    }

    @Test
    fun `MainActivity should initialize UI components correctly`() {
        assertNotNull("RecyclerView should be initialized", activity.findViewById<RecyclerView>(R.id.rv_users))
        assertNotNull("ProgressBar should be initialized", activity.findViewById<ProgressBar>(R.id.progressBar))
    }

    @Test
    fun `MainActivity should initialize UserAdapter correctly`() {
        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)

        assertNotNull("RecyclerView should be initialized", recyclerView)
        assertNotNull("RecyclerView adapter should be set", recyclerView.adapter)
        assertTrue("Adapter should be UserAdapter", recyclerView.adapter is UserAdapter)
    }

    @Test
    fun `MainActivity should inflate a progress bar for the loading state`() {
        val progressBar = activity.findViewById<ProgressBar>(R.id.progressBar)

        assertNotNull("Progress bar should be part of the inflated layout", progressBar)
        // Visibility is state-driven and settles as soon as the repository call
        // resolves, so it is asserted on device instead; see
        // MainActivityEspressoTest.
    }
}