package com.example.iurankomplek

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.transaction.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.math.BigDecimal
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class TransactionHistoryAdapterTest {

    private lateinit var adapter: TransactionHistoryAdapter
    private lateinit var testScope: CoroutineScope

    @Before
    fun setup() {
        val testDispatcher = UnconfinedTestDispatcher()
        testScope = CoroutineScope(testDispatcher)
        adapter = TransactionHistoryAdapter(testScope)
    }

    @Test
    fun `TransactionHistoryAdapter should have correct initial item count`() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `TransactionHistoryAdapter should set items correctly`() {
        val transactions = createMockTransactions(count = 2)

        adapter.submitList(transactions)

        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `TransactionHistoryAdapter should create ViewHolder correctly`() {
        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)

        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        assertNotNull(viewHolder)
        assertNotNull(viewHolder.itemView)
    }

    @Test
    fun `TransactionHistoryAdapter should bind ViewHolder correctly with COMPLETED transaction`() {
        val transaction = createMockTransaction(
            status = PaymentStatus.COMPLETED,
            amount = BigDecimal("100000")
        )

        adapter.submitList(listOf(transaction))

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        assertEquals("Test transaction", viewHolder.tvDescription.text.toString())
        assertEquals("COMPLETED", viewHolder.tvStatus.text.toString())
        assertEquals("CREDIT_CARD", viewHolder.tvPaymentMethod.text.toString())
        assertEquals(View.VISIBLE, viewHolder.btnRefund.visibility)
    }

    @Test
    fun `TransactionHistoryAdapter should hide refund button for non-COMPLETED transactions`() {
        val transactions = listOf(
            createMockTransaction(
                id = "txn_1",
                status = PaymentStatus.PENDING,
                amount = BigDecimal("100000")
            ),
            createMockTransaction(
                id = "txn_2",
                status = PaymentStatus.FAILED,
                amount = BigDecimal("200000")
            ),
            createMockTransaction(
                id = "txn_3",
                status = PaymentStatus.REFUNDED,
                amount = BigDecimal("300000")
            )
        )

        adapter.submitList(transactions)

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)

        for (i in transactions.indices) {
            val viewHolder = adapter.onCreateViewHolder(parent, 0)
            adapter.onBindViewHolder(viewHolder, i)
            assertEquals(View.GONE, viewHolder.btnRefund.visibility)
        }
    }

    @Test
    fun `TransactionHistoryAdapter should format amount correctly`() {
        val transaction = createMockTransaction(
            amount = BigDecimal("1000000")
        )

        adapter.submitList(listOf(transaction))

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        val amountText = viewHolder.tvAmount.text.toString()
        assertTrue(amountText.contains("1.000.000"))
        assertTrue(amountText.contains("Rp"))
    }

    @Test
    fun `TransactionHistoryAdapter should display all payment methods correctly`() {
        val paymentMethods = listOf(
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.BANK_TRANSFER,
            PaymentMethod.E_WALLET,
            PaymentMethod.VIRTUAL_ACCOUNT
        )

        val transactions = paymentMethods.mapIndexed { index, method ->
            createMockTransaction(
                id = "txn_$index",
                paymentMethod = method
            )
        }

        adapter.submitList(transactions)

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)

        for (i in transactions.indices) {
            val viewHolder = adapter.onCreateViewHolder(parent, 0)
            adapter.onBindViewHolder(viewHolder, i)
            assertEquals(paymentMethods[i].name, viewHolder.tvPaymentMethod.text.toString())
        }
    }

    @Test
    fun `TransactionHistoryAdapter should display all payment statuses correctly`() {
        val statuses = listOf(
            PaymentStatus.PENDING,
            PaymentStatus.PROCESSING,
            PaymentStatus.COMPLETED,
            PaymentStatus.FAILED,
            PaymentStatus.REFUNDED,
            PaymentStatus.CANCELLED
        )

        val transactions = statuses.mapIndexed { index, status ->
            createMockTransaction(
                id = "txn_$index",
                status = status
            )
        }

        adapter.submitList(transactions)

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)

        for (i in transactions.indices) {
            val viewHolder = adapter.onCreateViewHolder(parent, 0)
            adapter.onBindViewHolder(viewHolder, i)
            assertEquals(statuses[i].name, viewHolder.tvStatus.text.toString())
        }
    }

    @Test
    fun `TransactionHistoryAdapter DiffCallback should identify same items by id`() {
        val oldTransactions = listOf(
            createMockTransaction(id = "txn_123", amount = BigDecimal("100000"))
        )
        val newTransactions = listOf(
            createMockTransaction(id = "txn_123", amount = BigDecimal("200000")) // Same id, different amount
        )

        adapter.submitList(oldTransactions)
        assertEquals(1, adapter.itemCount)

        adapter.submitList(newTransactions)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `TransactionHistoryAdapter should handle empty list`() {
        adapter.submitList(emptyList())

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `TransactionHistoryAdapter should handle single item`() {
        val transaction = createMockTransaction()

        adapter.submitList(listOf(transaction))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `TransactionHistoryAdapter should handle large number of items`() {
        val transactions = createMockTransactions(count = 100)

        adapter.submitList(transactions)

        assertEquals(100, adapter.itemCount)
    }

    @Test
    fun `TransactionHistoryAdapter should handle zero amount transactions`() {
        val transaction = createMockTransaction(
            amount = BigDecimal.ZERO
        )

        adapter.submitList(listOf(transaction))

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        val amountText = viewHolder.tvAmount.text.toString()
        assertTrue(amountText.contains("0"))
    }

    @Test
    fun `TransactionHistoryAdapter should handle very large amounts`() {
        val transaction = createMockTransaction(
            amount = BigDecimal("999999999.99")
        )

        adapter.submitList(listOf(transaction))

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        val amountText = viewHolder.tvAmount.text.toString()
        assertTrue(amountText.isNotEmpty())
    }

    @Test
    fun `TransactionHistoryAdapter should handle special characters in description`() {
        val transaction = createMockTransaction(
            description = "Iuran Warga (2024) - Pembayaran & Biaya"
        )

        adapter.submitList(listOf(transaction))

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        assertEquals("Iuran Warga (2024) - Pembayaran & Biaya", viewHolder.tvDescription.text.toString())
    }

    @Test
    fun `TransactionHistoryAdapter should display transaction date`() {
        val testDate = Date(1234567890000L)
        val transaction = createMockTransaction(
            createdAt = testDate
        )

        adapter.submitList(listOf(transaction))

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        assertNotNull(viewHolder.tvDate.text)
        assertTrue(viewHolder.tvDate.text.toString().isNotEmpty())
    }

    @Test
    fun `TransactionHistoryAdapter ViewHolder should have correct view references`() {
        val transaction = createMockTransaction()

        adapter.submitList(listOf(transaction))

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        assertNotNull(viewHolder.tvAmount)
        assertNotNull(viewHolder.tvDescription)
        assertNotNull(viewHolder.tvDate)
        assertNotNull(viewHolder.tvStatus)
        assertNotNull(viewHolder.tvPaymentMethod)
        assertNotNull(viewHolder.btnRefund)
    }

    @Test
    fun `TransactionHistoryAdapter should update items incrementally`() {
        val initialTransactions = createMockTransactions(count = 1)

        adapter.submitList(initialTransactions)
        assertEquals(1, adapter.itemCount)

        val updatedTransactions = createMockTransactions(count = 3)

        adapter.submitList(updatedTransactions)
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `TransactionHistoryAdapter should handle transactions with metadata`() {
        val transaction = createMockTransaction(
            metadata = mapOf("reference" to "REF123", "category" to "maintenance")
        )

        adapter.submitList(listOf(transaction))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `TransactionHistoryAdapter should handle different currency values`() {
        val currencies = listOf("IDR", "USD", "EUR", "SGD")

        val transactions = currencies.mapIndexed { index, currency ->
            createMockTransaction(
                id = "txn_$index",
                currency = currency
            )
        }

        adapter.submitList(transactions)

        assertEquals(currencies.size, adapter.itemCount)
    }

    @Test
    fun `TransactionHistoryAdapter should handle very long descriptions`() {
        val longDescription = "A".repeat(500)
        val transaction = createMockTransaction(
            description = longDescription
        )

        adapter.submitList(listOf(transaction))

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        assertEquals(longDescription, viewHolder.tvDescription.text.toString())
    }

    @Test
    fun `TransactionHistoryAdapter should show refund button for COMPLETED status`() {
        val transaction = createMockTransaction(
            status = PaymentStatus.COMPLETED
        )

        adapter.submitList(listOf(transaction))

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        assertEquals(View.VISIBLE, viewHolder.btnRefund.visibility)
    }

    private fun createMockTransactions(count: Int): List<Transaction> {
        return (1..count).map { index ->
            createMockTransaction(
                id = "txn_$index",
                amount = BigDecimal("100000").multiply(BigDecimal(index.toLong()))
            )
        }
    }

    private fun createMockTransaction(
        id: String = "txn_123",
        userId: String = "user123",
        amount: BigDecimal = BigDecimal("100000"),
        currency: String = "IDR",
        status: PaymentStatus = PaymentStatus.COMPLETED,
        paymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD,
        description: String = "Test transaction",
        createdAt: Date = Date(),
        updatedAt: Date = Date(),
        metadata: Map<String, String> = emptyMap()
    ): Transaction {
        return Transaction(
            id = id,
            userId = userId,
            amount = amount,
            currency = currency,
            status = status,
            paymentMethod = paymentMethod,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
            metadata = metadata
        )
    }
}