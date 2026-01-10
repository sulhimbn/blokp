package com.example.iurankomplek.payment

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.payment.PaymentEvent.Error
import com.example.iurankomplek.payment.PaymentEvent.Processing
import com.example.iurankomplek.payment.PaymentEvent.Success
import com.example.iurankomplek.payment.PaymentEvent.ValidationError
import com.example.iurankomplek.payment.PaymentViewModel
import com.example.iurankomplek.receipt.ReceiptGenerator
import com.example.iurankomplek.transaction.TransactionRepository
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal
import org.junit.runner.RunWith

@RunWith(MockitoJUnitRunner::class)
class PaymentFlowIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var receiptGenerator: ReceiptGenerator

    private lateinit var viewModel: PaymentViewModel
    private lateinit var viewModelFactory: PaymentViewModelFactory

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModelFactory = PaymentViewModelFactory(transactionRepository, receiptGenerator)
        viewModel = viewModelFactory.create(PaymentViewModel::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `payment flow with valid amount and payment method succeeds`() {
        viewModel.setAmount(BigDecimal("100"))
        viewModel.selectPaymentMethod(PaymentMethod.CREDIT_CARD)

        `when`(transactionRepository.saveTransaction(any())).thenReturn(true)
        `when`(receiptGenerator.generateReceipt(any())).thenReturn("REC123")

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.paymentState.value
        assertTrue(state is UiState.Success)
        val successData = (state as UiState.Success).data
        assertEquals("REC123", successData)
    }

    @Test
    fun `payment flow with network error shows error state`() {
        viewModel.setAmount(BigDecimal("100"))
        viewModel.selectPaymentMethod(PaymentMethod.BANK_TRANSFER)

        `when`(transactionRepository.saveTransaction(any())).thenThrow(
            RuntimeException("Network connection failed")
        )

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.paymentState.value
        assertTrue(state is UiState.Error)
        val errorMessage = (state as UiState.Error).message
        assertTrue(errorMessage.contains("Network connection failed"))
    }

    @Test
    fun `payment flow with validation error shows validation error event`() {
        viewModel.setAmount(BigDecimal("-100"))

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        val event = viewModel.paymentEvent.value
        assertTrue(event is ValidationError)
        val validationError = event as ValidationError
        assertNotNull(validationError.message)
        assertTrue(validationError.message!!.contains("must be positive"))
    }

    @Test
    fun `payment flow with zero amount shows validation error`() {
        viewModel.setAmount(BigDecimal("0"))

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        val event = viewModel.paymentEvent.value
        assertTrue(event is ValidationError)
    }

    @Test
    fun `payment flow with amount exceeding maximum shows validation error`() {
        viewModel.setAmount(BigDecimal("1000001"))

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        val event = viewModel.paymentEvent.value
        assertTrue(event is ValidationError)
        val validationError = event as ValidationError
        assertNotNull(validationError.message)
        assertTrue(validationError.message!!.contains("maximum"))
    }

    @Test
    fun `payment flow with invalid payment method defaults to credit card`() {
        viewModel.setAmount(BigDecimal("100"))

        viewModel.selectPaymentMethod(PaymentMethod.UNKNOWN)

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        verify(transactionRepository).saveTransaction(argThat { transaction ->
            transaction.paymentMethod == PaymentMethod.CREDIT_CARD
        })
    }

    @Test
    fun `payment flow with processing state prevents multiple submissions`() {
        viewModel.setAmount(BigDecimal("100"))
        viewModel.selectPaymentMethod(PaymentMethod.CREDIT_CARD)

        `when`(transactionRepository.saveTransaction(any())).thenReturn(true)

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        val firstEvent = viewModel.paymentEvent.value
        assertTrue(firstEvent is Processing)

        viewModel.processPayment()

        verify(transactionRepository, atMostOnce()).saveTransaction(any())
    }

    @Test
    fun `payment flow recovers from error to success on retry`() {
        viewModel.setAmount(BigDecimal("100"))
        viewModel.selectPaymentMethod(PaymentMethod.CREDIT_CARD)

        var attemptCount = 0
        `when`(transactionRepository.saveTransaction(any())).thenAnswer {
            attemptCount++
            if (attemptCount == 1) {
                throw RuntimeException("Temporary error")
            } else {
                true
            }
        }
        `when`(receiptGenerator.generateReceipt(any())).thenReturn("REC456")

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        val firstState = viewModel.paymentState.value
        assertTrue(firstState is UiState.Error)

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        val retryState = viewModel.paymentState.value
        assertTrue(retryState is UiState.Success)
    }

    @Test
    fun `payment flow with receipt generation error shows error state`() {
        viewModel.setAmount(BigDecimal("100"))
        viewModel.selectPaymentMethod(PaymentMethod.E_WALLET)

        `when`(transactionRepository.saveTransaction(any())).thenReturn(true)
        `when`(receiptGenerator.generateReceipt(any())).thenThrow(
            RuntimeException("Failed to generate receipt")
        )

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.paymentState.value
        assertTrue(state is UiState.Error)
    }

    @Test
    fun `payment flow validates amount before processing`() {
        viewModel.setAmount(BigDecimal("100"))
        viewModel.selectPaymentMethod(PaymentMethod.VIRTUAL_ACCOUNT)

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        verify(transactionRepository).saveTransaction(argThat { transaction ->
            transaction.amount.compareTo(BigDecimal("100")) == 0
        })
    }

    @Test
    fun `payment flow with decimal amount handles correctly`() {
        viewModel.setAmount(BigDecimal("100.50"))
        viewModel.selectPaymentMethod(PaymentMethod.CREDIT_CARD)

        `when`(transactionRepository.saveTransaction(any())).thenReturn(true)
        `when`(receiptGenerator.generateReceipt(any())).thenReturn("REC789")

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.paymentState.value
        assertTrue(state is UiState.Success)

        verify(transactionRepository).saveTransaction(argThat { transaction ->
            transaction.amount.compareTo(BigDecimal("100.50")) == 0
        })
    }

    @Test
    fun `payment flow with amount exceeding two decimal places shows validation error`() {
        viewModel.setAmount(BigDecimal("100.123"))

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        val event = viewModel.paymentEvent.value
        assertTrue(event is ValidationError)
        val validationError = event as ValidationError
        assertNotNull(validationError.message)
        assertTrue(validationError.message!!.contains("decimal"))
    }

    @Test
    fun `payment flow resets state after error for new attempt`() {
        viewModel.setAmount(BigDecimal("100"))
        viewModel.selectPaymentMethod(PaymentMethod.CREDIT_CARD)

        `when`(transactionRepository.saveTransaction(any())).thenThrow(
            RuntimeException("First attempt failed")
        )

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        val firstState = viewModel.paymentState.value
        assertTrue(firstState is UiState.Error)

        viewModel.setAmount(BigDecimal("200"))
        `when`(transactionRepository.saveTransaction(any())).thenReturn(true)
        `when`(receiptGenerator.generateReceipt(any())).thenReturn("REC999")

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        val retryState = viewModel.paymentState.value
        assertTrue(retryState is UiState.Success)
    }

    @Test
    fun `payment flow handles concurrent payment attempts correctly`() {
        viewModel.setAmount(BigDecimal("100"))
        viewModel.selectPaymentMethod(PaymentMethod.CREDIT_CARD)

        `when`(transactionRepository.saveTransaction(any())).thenReturn(true)
        `when`(receiptGenerator.generateReceipt(any())).thenReturn("REC123")

        viewModel.processPayment()
        viewModel.processPayment()
        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        verify(transactionRepository, atMostOnce()).saveTransaction(any())
    }

    @Test
    fun `payment flow with empty payment method defaults to credit card`() {
        viewModel.setAmount(BigDecimal("100"))

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        verify(transactionRepository).saveTransaction(argThat { transaction ->
            transaction.paymentMethod == PaymentMethod.CREDIT_CARD
        })
    }

    @Test
    fun `payment flow preserves error message for display`() {
        viewModel.setAmount(BigDecimal("100"))
        viewModel.selectPaymentMethod(PaymentMethod.BANK_TRANSFER)

        val errorMessage = "Insufficient funds"
        `when`(transactionRepository.saveTransaction(any())).thenThrow(
            RuntimeException(errorMessage)
        )

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.paymentState.value
        assertTrue(state is UiState.Error)
        assertTrue((state as UiState.Error).message.contains(errorMessage))
    }

    @Test
    fun `payment flow transitions from processing to success correctly`() {
        viewModel.setAmount(BigDecimal("100"))
        viewModel.selectPaymentMethod(PaymentMethod.CREDIT_CARD)

        `when`(transactionRepository.saveTransaction(any())).thenReturn(true)
        `when`(receiptGenerator.generateReceipt(any())).thenReturn("REC123")

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        val events = mutableListOf<PaymentEvent>()
        viewModel.paymentEvent.value?.let { events.add(it) }

        assertTrue(events.any { it is Processing })
        assertTrue(events.any { it is Success })
    }

    @Test
    fun `payment flow transitions from processing to error correctly`() {
        viewModel.setAmount(BigDecimal("100"))
        viewModel.selectPaymentMethod(PaymentMethod.E_WALLET)

        `when`(transactionRepository.saveTransaction(any())).thenThrow(
            RuntimeException("Payment gateway error")
        )

        viewModel.processPayment()

        testDispatcher.scheduler.advanceUntilIdle()

        val event = viewModel.paymentEvent.value
        assertTrue(event is Processing || event is Error)
    }
}
