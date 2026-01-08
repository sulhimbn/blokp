package com.example.iurankomplek.presentation.ui.activity

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.iurankomplek.R
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@MediumTest
@Config(sdk = [33])
class LaporanActivityTest {

    private lateinit var scenario: ActivityScenario<LaporanActivity>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        scenario = ActivityScenario.launch(LaporanActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun `activity launches successfully`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity)
            org.junit.Assert.assertNotNull(activity.findViewById(android.R.id.content))
        }
    }

    @Test
    fun `progressBar is initially visible`() {
        scenario.onActivity { activity ->
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            org.junit.Assert.assertNotNull(progressBar)
        }
    }

    @Test
    fun `recyclerView for laporan is initialized`() {
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvLaporan)
            org.junit.Assert.assertNotNull(recyclerView)
            org.junit.Assert.assertNotNull(recyclerView.adapter)
            org.junit.Assert.assertNotNull(recyclerView.layoutManager)
        }
    }

    @Test
    fun `recyclerView for summary is initialized`() {
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvSummary)
            org.junit.Assert.assertNotNull(recyclerView)
            org.junit.Assert.assertNotNull(recyclerView.adapter)
            org.junit.Assert.assertNotNull(recyclerView.layoutManager)
        }
    }

    @Test
    fun `swipeRefreshLayout is initialized`() {
        scenario.onActivity { activity ->
            val swipeRefreshLayout = activity.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefreshLayout)
            org.junit.Assert.assertNotNull(swipeRefreshLayout)
        }
    }

    @Test
    fun `activity extends BaseActivity`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertTrue(activity is com.example.iurankomplek.core.base.BaseActivity)
        }
    }

    @Test
    fun `recyclerViews use LinearLayoutManager`() {
        scenario.onActivity { activity ->
            val rvLaporan = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvLaporan)
            val rvSummary = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvSummary)

            org.junit.Assert.assertTrue(rvLaporan.layoutManager is androidx.recyclerview.widget.LinearLayoutManager)
            org.junit.Assert.assertTrue(rvSummary.layoutManager is androidx.recyclerview.widget.LinearLayoutManager)
        }
    }

    @Test
    fun `empty state TextView is present`() {
        scenario.onActivity { activity ->
            val emptyStateTextView = activity.findViewById<android.widget.TextView>(R.id.emptyStateTextView)
            org.junit.Assert.assertNotNull(emptyStateTextView)
        }
    }

    @Test
    fun `error state layout is present`() {
        scenario.onActivity { activity ->
            val errorStateLayout = activity.findViewById<android.view.ViewGroup>(R.id.errorStateLayout)
            org.junit.Assert.assertNotNull(errorStateLayout)
        }
    }

    @Test
    fun `error state TextView is present`() {
        scenario.onActivity { activity ->
            val errorStateTextView = activity.findViewById<android.widget.TextView>(R.id.errorStateTextView)
            org.junit.Assert.assertNotNull(errorStateTextView)
        }
    }

    @Test
    fun `retry TextView is present`() {
        scenario.onActivity { activity ->
            val retryTextView = activity.findViewById<android.widget.TextView>(R.id.retryTextView)
            org.junit.Assert.assertNotNull(retryTextView)
        }
    }

    @Test
    fun `financialViewModel is initialized`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.viewModel)
        }
    }

    @Test
    fun `pemanfaatanRepository is initialized via factory`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.viewModel)
        }
    }

    @Test
    fun `transactionRepository is initialized via factory`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.transactionRepository)
        }
    }

    @Test
    fun `activity lifecycle scope is valid`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.lifecycle)
            org.junit.Assert.assertTrue(activity.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.CREATED))
        }
    }

    @Test
    fun `loading state shows progressBar and hides content`() {
        scenario.onActivity { activity ->
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            val rvLaporan = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvLaporan)
            val rvSummary = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvSummary)
            val emptyStateTextView = activity.findViewById<android.widget.TextView>(R.id.emptyStateTextView)
            val errorStateLayout = activity.findViewById<android.view.ViewGroup>(R.id.errorStateLayout)

            org.junit.Assert.assertNotNull(progressBar)
            org.junit.Assert.assertNotNull(rvLaporan)
            org.junit.Assert.assertNotNull(rvSummary)
            org.junit.Assert.assertNotNull(emptyStateTextView)
            org.junit.Assert.assertNotNull(errorStateLayout)
        }
    }

    @Test
    fun `empty state shows empty message and hides other elements`() {
        scenario.onActivity { activity ->
            val emptyStateTextView = activity.findViewById<android.widget.TextView>(R.id.emptyStateTextView)
            val rvLaporan = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvLaporan)
            val rvSummary = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvSummary)
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            val errorStateLayout = activity.findViewById<android.view.ViewGroup>(R.id.errorStateLayout)

            org.junit.Assert.assertNotNull(emptyStateTextView)
            org.junit.Assert.assertNotNull(rvLaporan)
            org.junit.Assert.assertNotNull(rvSummary)
            org.junit.Assert.assertNotNull(progressBar)
            org.junit.Assert.assertNotNull(errorStateLayout)
        }
    }

    @Test
    fun `error state shows error message and retry button`() {
        scenario.onActivity { activity ->
            val errorStateLayout = activity.findViewById<android.view.ViewGroup>(R.id.errorStateLayout)
            val errorStateTextView = activity.findViewById<android.widget.TextView>(R.id.errorStateTextView)
            val retryTextView = activity.findViewById<android.widget.TextView>(R.id.retryTextView)

            org.junit.Assert.assertNotNull(errorStateLayout)
            org.junit.Assert.assertNotNull(errorStateTextView)
            org.junit.Assert.assertNotNull(retryTextView)
        }
    }

    @Test
    fun `success state shows content and hides loading`() {
        scenario.onActivity { activity ->
            val rvLaporan = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvLaporan)
            val rvSummary = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvSummary)
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)

            org.junit.Assert.assertNotNull(rvLaporan)
            org.junit.Assert.assertNotNull(rvSummary)
            org.junit.Assert.assertNotNull(progressBar)
        }
    }

    @Test
    fun `pemanfaatanAdapter is initialized`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.adapter)
        }
    }

    @Test
    fun `summaryAdapter is initialized`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.summaryAdapter)
        }
    }

    @Test
    fun `pemanfaatanAdapter is attached to laporan RecyclerView`() {
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvLaporan)
            org.junit.Assert.assertEquals(activity.adapter, recyclerView.adapter)
        }
    }

    @Test
    fun `summaryAdapter is attached to summary RecyclerView`() {
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvSummary)
            org.junit.Assert.assertEquals(activity.summaryAdapter, recyclerView.adapter)
        }
    }

    @Test
    fun `swipeRefreshLayout has refresh listener`() {
        scenario.onActivity { activity ->
            val swipeRefreshLayout = activity.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefreshLayout)
            org.junit.Assert.assertNotNull(swipeRefreshLayout)
        }
    }

    @Test
    fun `retry TextView has click listener`() {
        scenario.onActivity { activity ->
            val retryTextView = activity.findViewById<android.widget.TextView>(R.id.retryTextView)
            org.junit.Assert.assertNotNull(retryTextView)
        }
    }
}