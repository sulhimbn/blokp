package com.example.iurankomplek

import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.iurankomplek.presentation.adapter.UserAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MainActivityTest {

    @Before
    fun resetSharedCaches() {
        com.example.iurankomplek.utils.CacheManager.getInstance().clearSync()
    }

    private fun launch() = Robolectric.buildActivity(MainActivity::class.java).setup().get()

    @Test
    fun `activity starts and is not finishing`() {
        val activity = launch()

        assertNotNull(activity)
        assertTrue(!activity.isFinishing)
    }

    @Test
    fun `resident list is a recycler view with a linear layout manager and a user adapter`() {
        val activity = launch()

        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)

        assertNotNull(recyclerView)
        assertTrue(recyclerView.layoutManager is LinearLayoutManager)
        assertTrue(recyclerView.adapter is UserAdapter)
    }

    @Test
    fun `progress indicator and pull to refresh container are inflated`() {
        val activity = launch()

        assertNotNull(activity.findViewById<ProgressBar>(R.id.progressBar))
        assertNotNull(activity.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout))
    }

    @Test
    fun `search filter component is inflated`() {
        val activity = launch()

        assertNotNull(activity.findViewById<View>(R.id.searchFilterView))
    }

    @Test
    fun `list starts empty because the resident payload has not loaded yet`() {
        val activity = launch()

        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)

        assertEquals(0, (recyclerView.adapter as UserAdapter).itemCount)
    }
}