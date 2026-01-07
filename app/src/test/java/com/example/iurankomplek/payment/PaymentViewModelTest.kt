package com.example.iurankomplek.payment

import com.example.iurankomplek.receipt.ReceiptGenerator
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.transaction.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.Date

@ExperimentalCoroutinesApi
class PaymentViewModelTest {

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var receiptGenerator: ReceiptGenerator

    private lateinit var paymentViewModel: PaymentViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        paymentViewModel = PaymentViewModel(transactionRepository, receiptGenerator)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial ui state has default values`() {
        // Assert
        assertEquals(BigDecimal.ZERO, paymentViewModel.uiState.value.amount)
        assertEquals(PaymentMethod.CREDIT_CARD, paymentViewModel.uiState.value.selectedMethod)
        assertFalse(paymentViewModel.uiState.value.isProcessing)
        assertFalse(paymentViewModel.uiState.value.isPaymentEnabled)
        assertNull(paymentViewModel.uiState.value.errorMessage)
    }

    @Test
    fun `setAmount updates amount and enables payment when amount is positive`() {
        // Arrange
        val amount = BigDecimal("100.00")

        // Act
        paymentViewModel.setAmount(amount)

        // Assert
        assertEquals(amount, paymentViewModel.uiState.value.amount)
        assertTrue(paymentViewModel.uiState.value.isPaymentEnabled)
    }

    @Test
    fun `setAmount disables payment when amount is zero`() {
        // Arrange
        val amount = BigDecimal.ZERO

        // Act
        paymentViewModel.setAmount(amount)

        // Assert
        assertEquals(amount, paymentViewModel.uiState.value.amount)
        assertFalse(paymentViewModel.uiState.value.isPaymentEnabled)
    }

    @Test
    fun `setAmount disables payment when amount is negative`() {
        // Arrange
        val amount = BigDecimal("-100.00")

        // Act
        paymentViewModel.setAmount(amount)

        // Assert
        assertEquals(amount, paymentViewModel.uiState.value.amount)
        assertFalse(paymentViewModel.uiState.value.isPaymentEnabled)
    }

    @Test
    fun `setAmount can handle very large amounts`() {
        // Arrange
        val amount = BigDecimal("999999999999.99")

        // Act
        paymentViewModel.setAmount(amount)

        // Assert
        assertEquals(amount, paymentViewModel.uiState.value.amount)
        assertTrue(paymentViewModel.uiState.value.isPaymentEnabled)
    }

    @Test
    fun `setAmount can handle decimal amounts`() {
        // Arrange
        val amount = BigDecimal("123.45")

        // Act
        paymentViewModel.setAmount(amount)

        // Assert
        assertEquals(amount, paymentViewModel.uiState.value.amount)
        assertTrue(paymentViewModel.uiState.value.isPaymentEnabled)
    }

    @Test
    fun `selectPaymentMethod updates selected method`() {
        // Arrange
        val method = PaymentMethod.BANK_TRANSFER

        // Act
        paymentViewModel.selectPaymentMethod(method)

        // Assert
        assertEquals(method, paymentViewModel.uiState.value.selectedMethod)
    }

    @Test
    fun `selectPaymentMethod preserves other state values`() {
        // Arrange
        val amount = BigDecimal("50.00")
        paymentViewModel.setAmount(amount)

        // Act
        paymentViewModel.selectPaymentMethod(PaymentMethod.E_WALLET)

        // Assert
        assertEquals(amount, paymentViewModel.uiState.value.amount)
        assertEquals(PaymentMethod.E_WALLET, paymentViewModel.uiState.value.selectedMethod)
        assertTrue(paymentViewModel.uiState.value.isPaymentEnabled)
    }

    @Test
    fun `selectPaymentMethod can cycle through all payment methods`() {
        val methods = listOf(
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.BANK_TRANSFER,
            PaymentMethod.E_WALLET,
            PaymentMethod.VIRTUAL_ACCOUNT
        )

        methods.forEach { method ->
            paymentViewModel.selectPaymentMethod(method)
            assertEquals(method, paymentViewModel.uiState.value.selectedMethod)
        }
    }

    @Test
    fun `processPayment sets processing state before calling repository`() = runTest {
        // Arrange
        val amount = BigDecimal("100.00")
        paymentViewModel.setAmount(amount)

        val mockTransaction = Transaction(
            id = "test_transaction_id",
            userId = "current_user_id",
            amount = amount,
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "HOA Payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        whenever(transactionRepository.processPayment(any())).thenReturn(Result.success(mockTransaction))

        // Act
        paymentViewModel.processPayment()

        // Assert - Check processing state immediately
        assertTrue(paymentViewModel.uiState.value.isProcessing)
    }

    @Test
    fun `processPayment clears processing state on success`() = runTest {
        // Arrange
        val amount = BigDecimal("100.00")
        paymentViewModel.setAmount(amount)

        val mockTransaction = Transaction(
            id = "test_transaction_id",
            userId = "current_user_id",
            amount = amount,
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "HOA Payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        whenever(transactionRepository.processPayment(any())).thenReturn(Result.success(mockTransaction))

        // Act
        paymentViewModel.processPayment()
        advanceUntilIdle()

        // Assert
        assertFalse(paymentViewModel.uiState.value.isProcessing)
        assertNull(paymentViewModel.uiState.value.errorMessage)
    }

    @Test
    fun `processPayment generates receipt on success`() = runTest {
        // Arrange
        val amount = BigDecimal("100.00")
        paymentViewModel.setAmount(amount)

        val mockTransaction = Transaction(
            id = "test_transaction_id",
            userId = "current_user_id",
            amount = amount,
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "HOA Payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        whenever(transactionRepository.processPayment(any())).thenReturn(Result.success(mockTransaction))

        // Act
        paymentViewModel.processPayment()
        advanceUntilIdle()

        // Assert
        verify(receiptGenerator).generateReceipt(mockTransaction)
    }

    @Test
    fun `processPayment sets error message on failure`() = runTest {
        // Arrange
        val amount = BigDecimal("100.00")
        paymentViewModel.setAmount(amount)

        val error = Exception("Payment gateway error")
        whenever(transactionRepository.processPayment(any())).thenReturn(Result.failure(error))

        // Act
        paymentViewModel.processPayment()
        advanceUntilIdle()

        // Assert
        assertFalse(paymentViewModel.uiState.value.isProcessing)
        assertEquals("Payment gateway error", paymentViewModel.uiState.value.errorMessage)
    }

    @Test
    fun `processPayment does not generate receipt on failure`() = runTest {
        // Arrange
        val amount = BigDecimal("100.00")
        paymentViewModel.setAmount(amount)

        val error = Exception("Payment gateway error")
        whenever(transactionRepository.processPayment(any())).thenReturn(Result.failure(error))

        // Act
        paymentViewModel.processPayment()
        advanceUntilIdle()

        // Assert
        verify(receiptGenerator, never()).generateReceipt(any())
    }

    @Test
    fun `processPayment cannot be called when amount is zero`() = runTest {
        // Arrange
        paymentViewModel.setAmount(BigDecimal.ZERO)

        // Act
        paymentViewModel.processPayment()
        advanceUntilIdle()

        // Assert - Should not call repository
        verify(transactionRepository, never()).processPayment(any())
        assertFalse(paymentViewModel.uiState.value.isProcessing)
    }

    @Test
    fun `processPayment creates correct payment request`() = runTest {
        // Arrange
        val amount = BigDecimal("150.50")
        paymentViewModel.setAmount(amount)
        paymentViewModel.selectPaymentMethod(PaymentMethod.BANK_TRANSFER)

        val mockTransaction = Transaction(
            id = "test_transaction_id",
            userId = "current_user_id",
            amount = amount,
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            description = "HOA Payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        whenever(transactionRepository.processPayment(any())).thenReturn(Result.success(mockTransaction))

        // Act
        paymentViewModel.processPayment()
        advanceUntilIdle()

        // Assert
        verify(transactionRepository).processPayment(
            argThat { request ->
                request.amount == amount &&
                request.description == "HOA Payment" &&
                request.customerId == "current_user_id" &&
                request.paymentMethod == PaymentMethod.BANK_TRANSFER
            }
        )
    }

    @Test
    fun `processPayment handles timeout error`() = runTest {
        // Arrange
        val amount = BigDecimal("100.00")
        paymentViewModel.setAmount(amount)

        val error = Exception("Request timeout")
        whenever(transactionRepository.processPayment(any())).thenReturn(Result.failure(error))

        // Act
        paymentViewModel.processPayment()
        advanceUntilIdle()

        // Assert
        assertFalse(paymentViewModel.uiState.value.isProcessing)
        assertEquals("Request timeout", paymentViewModel.uiState.value.errorMessage)
    }

    @Test
    fun `processPayment clears previous error message on new attempt`() = runTest {
        // Arrange
        val amount = BigDecimal("100.00")
        paymentViewModel.setAmount(amount)

        val error = Exception("First error")
        whenever(transactionRepository.processPayment(any())).thenReturn(Result.failure(error))

        paymentViewModel.processPayment()
        advanceUntilIdle()

        assertEquals("First error", paymentViewModel.uiState.value.errorMessage)

        val mockTransaction = Transaction(
            id = "test_transaction_id",
            userId = "current_user_id",
            amount = amount,
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "HOA Payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        whenever(transactionRepository.processPayment(any())).thenReturn(Result.success(mockTransaction))

        // Act
        paymentViewModel.processPayment()
        advanceUntilIdle()

        // Assert
        assertNull(paymentViewModel.uiState.value.errorMessage)
    }

    @Test
    fun `uiState is immutable and does not affect previous state updates`() = runTest {
        // Arrange
        val amount1 = BigDecimal("100.00")
        paymentViewModel.setAmount(amount1)

        val state1 = paymentViewModel.uiState.value

        // Act
        paymentViewModel.setAmount(BigDecimal("200.00"))

        // Assert
        assertEquals(amount1, state1.amount)
        assertEquals(BigDecimal("200.00"), paymentViewModel.uiState.value.amount)
    }

    @Test
    fun `processPayment preserves amount after payment`() = runTest {
        // Arrange
        val amount = BigDecimal("100.00")
        paymentViewModel.setAmount(amount)

        val mockTransaction = Transaction(
            id = "test_transaction_id",
            userId = "current_user_id",
            amount = amount,
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "HOA Payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        whenever(transactionRepository.processPayment(any())).thenReturn(Result.success(mockTransaction))

        // Act
        paymentViewModel.processPayment()
        advanceUntilIdle()

        // Assert
        assertEquals(amount, paymentViewModel.uiState.value.amount)
    }

    @Test
    fun `processPayment preserves selected payment method after payment`() = runTest {
        // Arrange
        val amount = BigDecimal("100.00")
        paymentViewModel.setAmount(amount)
        paymentViewModel.selectPaymentMethod(PaymentMethod.E_WALLET)

        val mockTransaction = Transaction(
            id = "test_transaction_id",
            userId = "current_user_id",
            amount = amount,
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.E_WALLET,
            description = "HOA Payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        whenever(transactionRepository.processPayment(any())).thenReturn(Result.success(mockTransaction))

        // Act
        paymentViewModel.processPayment()
        advanceUntilIdle()

        // Assert
        assertEquals(PaymentMethod.E_WALLET, paymentViewModel.uiState.value.selectedMethod)
    }
}
