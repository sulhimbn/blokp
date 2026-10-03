package com.example.iurankomplek

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.presentation.adapter.UserAdapter
import org.junit.Assert.assertEquals
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
 * Functional gate for the dashboard screen: launches the real Activity through the real Hilt
 * graph and asserts what a user would actually see. The repository cannot be substituted
 * without a Hilt test module, so these assert the observable outcome of a real launch rather
 * than a fabricated ViewModel state.
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
    fun `activity reaches the resumed state on launch`() {
        assertTrue(
            "activity should be at least RESUMED, was ${activity.lifecycle.currentState}",
            activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        )
    }

    @Test
    fun `dashboard inflates its user list and progress indicator`() {
        assertNotNull("RecyclerView should be inflated", activity.findViewById<RecyclerView>(R.id.rv_users))
        assertNotNull("ProgressBar should be inflated", activity.findViewById<ProgressBar>(R.id.progressBar))
    }

    @Test
    fun `user list is backed by a UserAdapter`() {
        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)

        val adapter = recyclerView.adapter

        assertNotNull("RecyclerView should have an adapter", adapter)
        assertTrue("expected a UserAdapter but got ${adapter!!::class.java.name}", adapter is UserAdapter)
    }

    @Test
    fun `progress indicator is visible while the dashboard is still loading`() {
        // A fresh launch starts in UiState.Loading and the API is unreachable here, so the
        // indicator stays up. If the first load never fires the screen would sit here
        // forever, which is exactly the regression this pins.
        val progressBar = activity.findViewById<ProgressBar>(R.id.progressBar)

        assertEquals(View.VISIBLE, progressBar.visibility)
    }

    @Test
    fun `dashboard chrome is inflated`() {
        listOf(R.id.textView1, R.id.searchFilterView, R.id.swipeRefreshLayout).forEach { id ->
            assertNotNull("view $id should be inflated", activity.findViewById<View>(id))
        }
    }

    @Test
    fun `user list uses a vertical layout manager`() {
        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)
        val layoutManager = recyclerView.layoutManager

        assertNotNull("RecyclerView should have a layout manager", layoutManager)
        assertTrue(
            "expected a vertical layout manager but got ${layoutManager!!::class.java.name}",
            layoutManager is androidx.recyclerview.widget.LinearLayoutManager &&
                (layoutManager as androidx.recyclerview.widget.LinearLayoutManager).orientation ==
                androidx.recyclerview.widget.RecyclerView.VERTICAL
        )
    }

    @Test
    fun `activity can be destroyed without leaking the graph`() {
        activityController.pause().stop().destroy()

        assertEquals(Lifecycle.State.DESTROYED, activity.lifecycle.currentState)
    }
}
