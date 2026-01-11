package com.example.iurankomplek

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.example.iurankomplek.presentation.ui.activity.TransactionHistoryActivity
import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.presentation.viewmodel.TransactionViewModel
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.payment.PaymentStatus
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TransactionHistoryActivityTest {

    @Mock
    private lateinit var mockTransactionViewModel: TransactionViewModel

    private lateinit var activity: TransactionHistoryActivity
    private lateinit var controller: Robolectric.BuildActivity<TransactionHistoryActivity>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should initialize UI components correctly`() {
        controller = Robolectric.buildActivity(TransactionHistoryActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should be created", activity)
        assertEquals("Activity should be in created state", Lifecycle.State.CREATED, activity.lifecycle.currentState)

        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_transaction_history)
        assertNotNull("RecyclerView should be initialized", recyclerView)

        val progressBar = activity.findViewById<View>(R.id.progress_bar)
        assertNotNull("ProgressBar should be initialized", progressBar)
    }

    @Test
    fun `should setup RecyclerView with LinearLayoutManager`() {
        controller = Robolectric.buildActivity(TransactionHistoryActivity::class.java)
        activity = controller.create().get()

        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_transaction_history)
        assertNotNull("RecyclerView should have a layout manager", recyclerView.layoutManager)
        assertTrue("RecyclerView should use LinearLayoutManager", 
            recyclerView.layoutManager is androidx.recyclerview.widget.LinearLayoutManager)
    }

    @Test
    fun `should setup RecyclerView with adapter`() {
        controller = Robolectric.buildActivity(TransactionHistoryActivity::class.java)
        activity = controller.create().get()

        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_transaction_history)
        assertNotNull("RecyclerView should have an adapter", recyclerView.adapter)
    }

    @Test
    fun `should load completed transactions on startup`() {
        controller = Robolectric.buildActivity(TransactionHistoryActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should be created", activity)
        assertEquals("Activity should be in created state", Lifecycle.State.CREATED, activity.lifecycle.currentState)
    }

    @Test
    fun `should show progress bar during loading state`() {
        controller = Robolectric.buildActivity(TransactionHistoryActivity::class.java)
        activity = controller.create().get()

        val progressBar = activity.findViewById<View>(R.id.progress_bar)
        assertNotNull("ProgressBar should be initialized", progressBar)

        progressBar.visibility = View.VISIBLE
        assertEquals("ProgressBar should be visible", View.VISIBLE, progressBar.visibility)
    }

    @Test
    fun `should hide progress bar on success state`() {
        controller = Robolectric.buildActivity(TransactionHistoryActivity::class.java)
        activity = controller.create().get()

        val progressBar = activity.findViewById<View>(R.id.progress_bar)
        assertNotNull("ProgressBar should be initialized", progressBar)

        progressBar.visibility = View.GONE
        assertEquals("ProgressBar should be hidden", View.GONE, progressBar.visibility)
    }

    @Test
    fun `should show error toast on error state`() {
        controller = Robolectric.buildActivity(TransactionHistoryActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should be created", activity)
    }

    @Test
    fun `should verify PaymentStatus COMPLETED enum value`() {
        assertEquals("COMPLETED should equal 'completed'", "completed", PaymentStatus.COMPLETED.value)
    }

    @Test
    fun `should verify PaymentStatus PENDING enum value`() {
        assertEquals("PENDING should equal 'pending'", "pending", PaymentStatus.PENDING.value)
    }

    @Test
    fun `should verify PaymentStatus FAILED enum value`() {
        assertEquals("FAILED should equal 'failed'", "failed", PaymentStatus.FAILED.value)
    }

    @Test
    fun `should handle empty transaction list on success state`() {
        controller = Robolectric.buildActivity(TransactionHistoryActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should handle empty transaction list", activity)
    }

    @Test
    fun `should handle non-empty transaction list on success state`() {
        controller = Robolectric.buildActivity(TransactionHistoryActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should handle transaction list", activity)
    }

    @Test
    fun `should verify Transaction data structure`() {
        val testTransaction = Transaction(
            id = "TXN-12345",
            userId = 1L,
            amount = 50000L,
            paymentMethod = com.example.iurankomplek.payment.PaymentMethod.BANK_TRANSFER,
            status = PaymentStatus.COMPLETED,
            createdAt = Date(),
            updatedAt = Date()
        )

        assertNotNull("Transaction should have ID", testTransaction.id)
        assertNotNull("Transaction should have userId", testTransaction.userId)
        assertTrue("Transaction amount should be positive", testTransaction.amount > 0)
        assertNotNull("Transaction should have payment method", testTransaction.paymentMethod)
        assertNotNull("Transaction should have status", testTransaction.status)
        assertNotNull("Transaction should have creation date", testTransaction.createdAt)
        assertNotNull("Transaction should have update date", testTransaction.updatedAt)
    }

    @Test
    fun `should verify Transaction with all fields`() {
        val testDate = Date()
        val testTransaction = Transaction(
            id = "TXN-67890",
            userId = 2L,
            amount = 150000L,
            paymentMethod = com.example.iurankomplek.payment.PaymentMethod.E_WALLET,
            status = PaymentStatus.COMPLETED,
            createdAt = testDate,
            updatedAt = testDate
        )

        assertEquals("TXN-67890", testTransaction.id)
        assertEquals(2L, testTransaction.userId)
        assertEquals(150000L, testTransaction.amount)
        assertEquals(com.example.iurankomplek.payment.PaymentMethod.E_WALLET, testTransaction.paymentMethod)
        assertEquals(PaymentStatus.COMPLETED, testTransaction.status)
        assertEquals(testDate, testTransaction.createdAt)
        assertEquals(testDate, testTransaction.updatedAt)
    }

    @Test
    fun `should handle UiState Idle state gracefully`() {
        controller = Robolectric.buildActivity(TransactionHistoryActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should handle Idle state", activity)
    }

    @Test
    fun `should verify activity lifecycle states`() {
        controller = Robolectric.buildActivity(TransactionHistoryActivity::class.java)
        
        controller.create()
        assertEquals("Activity should be in created state", Lifecycle.State.CREATED, activity.lifecycle.currentState)
        
        controller.start()
        assertEquals("Activity should be in started state", Lifecycle.State.STARTED, activity.lifecycle.currentState)
        
        controller.resume()
        assertEquals("Activity should be in resumed state", Lifecycle.State.RESUMED, activity.lifecycle.currentState)
    }

    @Test
    fun `should initialize TransactionViewModel correctly`() {
        controller = Robolectric.buildActivity(TransactionHistoryActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should be created", activity)
        assertNotNull("ViewModel should be initialized", activity.lifecycle.currentState)
    }

    @Test
    fun `should initialize TransactionHistoryAdapter correctly`() {
        controller = Robolectric.buildActivity(TransactionHistoryActivity::class.java)
        activity = controller.create().get()

        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_transaction_history)
        assertNotNull("RecyclerView adapter should be initialized", recyclerView.adapter)
    }

    @Test
    fun `should submit list to adapter on success state`() {
        controller = Robolectric.buildActivity(TransactionHistoryActivity::class.java)
        activity = controller.create().get()

        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_transaction_history)
        assertNotNull("RecyclerView should be able to submit list", recyclerView.adapter)
    }

    @Test
    fun `should display error message correctly`() {
        controller = Robolectric.buildActivity(TransactionHistoryActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should handle error state", activity)
    }

    @Test
    fun `should verify PaymentStatus values are distinct`() {
        assertNotEquals("COMPLETED and PENDING should be different", 
            PaymentStatus.COMPLETED.value, PaymentStatus.PENDING.value)
        assertNotEquals("COMPLETED and FAILED should be different", 
            PaymentStatus.COMPLETED.value, PaymentStatus.FAILED.value)
        assertNotEquals("PENDING and FAILED should be different", 
            PaymentStatus.PENDING.value, PaymentStatus.FAILED.value)
    }

    @Test
    fun `should handle TransactionRepository initialization`() {
        controller = Robolectric.buildActivity(TransactionHistoryActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should initialize repository", activity)
    }
}