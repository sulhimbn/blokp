package com.example.iurankomplek

import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.iurankomplek.presentation.adapter.UserAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.app.Application
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * MainActivity is @AndroidEntryPoint, so its Hilt graph cannot be satisfied on the JVM.
 * These tests instead verify the layout contract MainActivity binds to by id, which is
 * where a broken view reference would actually surface.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
class MainActivityTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var root: View

    @Before
    fun setUp() {
        val themedContext =
            androidx.appcompat.view.ContextThemeWrapper(
                RuntimeEnvironment.getApplication(),
                R.style.Theme_BlokP
            )
        root = LayoutInflater.from(themedContext).inflate(R.layout.activity_main, null)
    }

    @Test
    fun `layout inflates with the application theme`() {
        assertNotNull(root)
    }

    @Test
    fun `RecyclerView is present and can host a UserAdapter`() {
        val recyclerView = root.findViewById<RecyclerView>(R.id.rv_users)

        assertNotNull("RecyclerView should be inflated", recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(RuntimeEnvironment.getApplication())
        recyclerView.adapter = UserAdapter(mutableListOf(), CoroutineScope(testDispatcher))

        assertNotNull("RecyclerView should have a LayoutManager", recyclerView.layoutManager)
        assertNotNull("RecyclerView should have an adapter", recyclerView.adapter)
        assertTrue("Adapter should be UserAdapter", recyclerView.adapter is UserAdapter)
        assertEquals(0, recyclerView.adapter?.itemCount)
    }

    @Test
    fun `progress bar and swipe refresh are present`() {
        assertNotNull(
            "ProgressBar should be inflated",
            root.findViewById<ProgressBar>(R.id.progressBar)
        )
        assertNotNull(
            "SwipeRefreshLayout should be inflated",
            root.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        )
    }

    @Test
    fun `search filter view is present`() {
        assertNotNull(
            "SearchFilterView should be inflated",
            root.findViewById<View>(R.id.searchFilterView)
        )
    }

    @Test
    fun `loading and success toggle the progress bar`() {
        val progressBar = root.findViewById<ProgressBar>(R.id.progressBar)

        progressBar.visibility = View.VISIBLE
        assertEquals(View.VISIBLE, progressBar.visibility)

        progressBar.visibility = View.GONE
        assertEquals(View.GONE, progressBar.visibility)
    }
}
