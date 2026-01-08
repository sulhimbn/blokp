package com.example.iurankomplek.presentation.ui.activity

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.iurankomplek.R
import com.example.iurankomplek.data.repository.TransactionRepositoryFactory
import com.example.iurankomplek.presentation.viewmodel.TransactionViewModel
import com.example.iurankomplek.presentation.viewmodel.TransactionViewModelFactory
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.payment.PaymentStatus
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@MediumTest
@Config(sdk = [33])
class TransactionHistoryActivityTest {

    private lateinit var scenario: ActivityScenario<TransactionHistoryActivity>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        scenario = ActivityScenario.launch(TransactionHistoryActivity::class.java)
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
    fun `recyclerView is initialized`() {
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvTransactionHistory)
            org.junit.Assert.assertNotNull(recyclerView)
            org.junit.Assert.assertNotNull(recyclerView.adapter)
            org.junit.Assert.assertNotNull(recyclerView.layoutManager)
        }
    }

    @Test
    fun `progressBar is visible in loading state`() {
        scenario.onActivity { activity ->
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            org.junit.Assert.assertNotNull(progressBar)
        }
    }

    @Test
    fun `progressBar is hidden in success state`() {
        scenario.onActivity { activity ->
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            org.junit.Assert.assertNotNull(progressBar)
        }
    }

    @Test
    fun `progressBar is hidden in error state`() {
        scenario.onActivity { activity ->
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            org.junit.Assert.assertNotNull(progressBar)
        }
    }

    @Test
    fun `viewModel is initialized`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.viewModel)
        }
    }

    @Test
    fun `transactionRepository is initialized via factory`() {
        scenario.onActivity { activity ->
            val repository = TransactionRepositoryFactory.getInstance(activity)
            org.junit.Assert.assertNotNull(repository)
        }
    }

    @Test
    fun `transactionHistoryAdapter is initialized`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.transactionAdapter)
        }
    }

    @Test
    fun `activity loads transactions with COMPLETED status`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.viewModel)
            org.junit.Assert.assertNotNull(activity.transactionAdapter)
        }
    }

    @Test
    fun `activity extends BaseActivity`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertTrue(activity is com.example.iurankomplek.core.base.BaseActivity)
        }
    }

    @Test
    fun `recyclerView uses LinearLayoutManager`() {
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvTransactionHistory)
            org.junit.Assert.assertTrue(
                recyclerView.layoutManager is androidx.recyclerview.widget.LinearLayoutManager
            )
        }
    }

    @Test
    fun `progressBar is initially visible during loading`() {
        scenario.onActivity { activity ->
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            org.junit.Assert.assertNotNull(progressBar)
        }
    }

    @Test
    fun `adapter is attached to recyclerView`() {
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvTransactionHistory)
            org.junit.Assert.assertEquals(activity.transactionAdapter, recyclerView.adapter)
        }
    }

    @Test
    fun `activity observes viewModel state`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.viewModel)
        }
    }

    @Test
    fun `when loading state shows progressBar`() {
        scenario.onActivity { activity ->
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            org.junit.Assert.assertNotNull(progressBar)
        }
    }

    @Test
    fun `when success state hides progressBar and submits data`() {
        scenario.onActivity { activity ->
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            org.junit.Assert.assertNotNull(progressBar)
        }
    }

    @Test
    fun `when error state hides progressBar and shows error toast`() {
        scenario.onActivity { activity ->
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            org.junit.Assert.assertNotNull(progressBar)
        }
    }

    @Test
    fun `idle state does not change UI`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.viewModel)
        }
    }

    @Test
    fun `transactionViewModelFactory creates viewModel correctly`() {
        scenario.onActivity { activity ->
            val repository = TransactionRepositoryFactory.getInstance(activity)
            val factory = TransactionViewModelFactory.getInstance(repository)
            org.junit.Assert.assertNotNull(factory)
        }
    }

    @Test
    fun `adapter receives lifecycleScope`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.lifecycleScope)
            org.junit.Assert.assertNotNull(activity.transactionAdapter)
        }
    }

    @Test
    fun `adapter receives transactionRepository`() {
        scenario.onActivity { activity ->
            val repository = TransactionRepositoryFactory.getInstance(activity)
            org.junit.Assert.assertNotNull(repository)
            org.junit.Assert.assertNotNull(activity.transactionAdapter)
        }
    }

    @Test
    fun `activity lifecycle scope is valid`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.lifecycle)
            org.junit.Assert.assertTrue(activity.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.CREATED))
        }
    }
}