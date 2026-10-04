package com.example.iurankomplek

import android.app.Application
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.iurankomplek.databinding.ActivityMainBinding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
class MainActivityTest {

    private lateinit var binding: ActivityMainBinding

    @Before
    fun setUp() {
        val themed = ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_BlokP)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(themed))
    }

    @Test
    fun `the screen inflates with the resident list present`() {
        assertNotNull("activity_main.xml must expose the resident list", binding.rvUsers)
        assertTrue(
            "rvUsers must be a RecyclerView but was ${binding.rvUsers.javaClass.name}",
            binding.rvUsers is RecyclerView
        )
    }

    @Test
    fun `the loading indicator is part of the screen`() {
        assertNotNull(binding.progressBar)
        assertTrue(binding.progressBar is ProgressBar)
    }

    @Test
    fun `pull to refresh is wired for the resident list`() {
        assertNotNull(binding.swipeRefreshLayout)
        assertTrue(
            "swipeRefreshLayout must be a SwipeRefreshLayout but was ${binding.swipeRefreshLayout.javaClass.name}",
            binding.swipeRefreshLayout is SwipeRefreshLayout
        )
    }

    @Test
    fun `the search filter component is part of the screen`() {
        assertNotNull(binding.searchFilterView)
    }

    @Test
    fun `the inflated hierarchy is rooted in a single view`() {
        assertNotNull(binding.root)
        assertTrue(
            "the root must itself be a view but was ${binding.root.javaClass.name}",
            binding.root is View
        )
    }
}
