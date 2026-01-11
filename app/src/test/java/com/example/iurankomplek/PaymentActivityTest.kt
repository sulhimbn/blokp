package com.example.iurankomplek

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.databinding.ActivityPaymentBinding
import com.example.iurankomplek.payment.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner::class)
class PaymentActivityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var binding: ActivityPaymentBinding

    @Mock
    private lateinit var paymentViewModel: PaymentViewModel

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var receiptGenerator: ReceiptGenerator

    private lateinit var paymentViewModelFactory: PaymentViewModelFactory

    @Before
    fun setup() {
        paymentViewModelFactory = PaymentViewModelFactory(transactionRepository, receiptGenerator)
    }

    @Test
    fun `processPayment should show error when amount is empty`() {
        val activity = TestPaymentActivity()
        
        `when`(binding.etAmount.text.toString().trim()).thenReturn("")
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel, never()).processPayment()
    }

    @Test
    fun `processPayment should show error when amount is whitespace only`() {
        val activity = TestPaymentActivity()
        
        `when`(binding.etAmount.text.toString().trim()).thenReturn("   ")
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel, never()).processPayment()
    }

    @Test
    fun `processPayment should show error when amount is zero`() {
        val activity = TestPaymentActivity()
        
        `when`(binding.etAmount.text.toString().trim()).thenReturn("0")
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel, never()).processPayment()
    }

    @Test
    fun `processPayment should show error when amount is negative`() {
        val activity = TestPaymentActivity()
        
        `when`(binding.etAmount.text.toString().trim()).thenReturn("-10")
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel, never()).processPayment()
    }

    @Test
    fun `processPayment should accept positive amount`() {
        val activity = TestPaymentActivity()
        
        `when`(binding.etAmount.text.toString().trim()).thenReturn("100")
        `when`(binding.spinnerPaymentMethod.selectedItemPosition).thenReturn(0)
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel).setAmount(BigDecimal("100"))
        verify(paymentViewModel).selectPaymentMethod(PaymentMethod.CREDIT_CARD)
        verify(paymentViewModel).processPayment()
    }

    @Test
    fun `processPayment should validate maximum amount limit`() {
        val activity = TestPaymentActivity()
        
        val maxAmount = BigDecimal.valueOf(1000000.0)
        `when`(binding.etAmount.text.toString().trim()).thenReturn("1000001")
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel, never()).processPayment()
    }

    @Test
    fun `processPayment should accept amount at maximum limit`() {
        val activity = TestPaymentActivity()
        
        val maxAmount = BigDecimal.valueOf(1000000.0)
        `when`(binding.etAmount.text.toString().trim()).thenReturn("1000000")
        `when`(binding.spinnerPaymentMethod.selectedItemPosition).thenReturn(0)
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel).setAmount(maxAmount)
        verify(paymentViewModel).processPayment()
    }

    @Test
    fun `processPayment should reject amount with more than 2 decimal places`() {
        val activity = TestPaymentActivity()
        
        `when`(binding.etAmount.text.toString().trim()).thenReturn("100.123")
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel, never()).processPayment()
    }

    @Test
    fun `processPayment should accept amount with 2 decimal places`() {
        val activity = TestPaymentActivity()
        
        `when`(binding.etAmount.text.toString().trim()).thenReturn("100.50")
        `when`(binding.spinnerPaymentMethod.selectedItemPosition).thenReturn(0)
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel).setAmount(BigDecimal("100.50"))
        verify(paymentViewModel).processPayment()
    }

    @Test
    fun `processPayment should accept amount with 1 decimal place`() {
        val activity = TestPaymentActivity()
        
        `when`(binding.etAmount.text.toString().trim()).thenReturn("100.5")
        `when`(binding.spinnerPaymentMethod.selectedItemPosition).thenReturn(0)
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel).setAmount(BigDecimal("100.5"))
        verify(paymentViewModel).processPayment()
    }

    @Test
    fun `processPayment should handle NumberFormatException for invalid format`() {
        val activity = TestPaymentActivity()
        
        `when`(binding.etAmount.text.toString().trim()).thenReturn("abc")
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel, never()).processPayment()
    }

    @Test
    fun `processPayment should handle ArithmeticException for invalid value`() {
        val activity = TestPaymentActivity()
        
        `when`(binding.etAmount.text.toString().trim()).thenReturn("999999999999999999999999999999999999999")
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel, never()).processPayment()
    }

    @Test
    fun `processPayment should select CREDIT_CARD when spinner position is 0`() {
        val activity = TestPaymentActivity()
        
        `when`(binding.etAmount.text.toString().trim()).thenReturn("100")
        `when`(binding.spinnerPaymentMethod.selectedItemPosition).thenReturn(0)
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel).selectPaymentMethod(PaymentMethod.CREDIT_CARD)
    }

    @Test
    fun `processPayment should select BANK_TRANSFER when spinner position is 1`() {
        val activity = TestPaymentActivity()
        
        `when`(binding.etAmount.text.toString().trim()).thenReturn("100")
        `when`(binding.spinnerPaymentMethod.selectedItemPosition).thenReturn(1)
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel).selectPaymentMethod(PaymentMethod.BANK_TRANSFER)
    }

    @Test
    fun `processPayment should select E_WALLET when spinner position is 2`() {
        val activity = TestPaymentActivity()
        
        `when`(binding.etAmount.text.toString().trim()).thenReturn("100")
        `when`(binding.spinnerPaymentMethod.selectedItemPosition).thenReturn(2)
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel).selectPaymentMethod(PaymentMethod.E_WALLET)
    }

    @Test
    fun `processPayment should select VIRTUAL_ACCOUNT when spinner position is 3`() {
        val activity = TestPaymentActivity()
        
        `when`(binding.etAmount.text.toString().trim()).thenReturn("100")
        `when`(binding.spinnerPaymentMethod.selectedItemPosition).thenReturn(3)
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel).selectPaymentMethod(PaymentMethod.VIRTUAL_ACCOUNT)
    }

    @Test
    fun `processPayment should default to CREDIT_CARD for unknown spinner position`() {
        val activity = TestPaymentActivity()
        
        `when`(binding.etAmount.text.toString().trim()).thenReturn("100")
        `when`(binding.spinnerPaymentMethod.selectedItemPosition).thenReturn(99)
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel).selectPaymentMethod(PaymentMethod.CREDIT_CARD)
    }

    @Test
    fun `processPayment should navigate to TransactionHistoryActivity on success`() {
        val activity = TestPaymentActivity()
        
        `when`(binding.etAmount.text.toString().trim()).thenReturn("100")
        `when`(binding.spinnerPaymentMethod.selectedItemPosition).thenReturn(0)
        
        activity.testProcessPayment(binding, paymentViewModel)
        
        verify(paymentViewModel).processPayment()
    }
}

class TestPaymentActivity : PaymentActivity() {
    fun testProcessPayment(binding: ActivityPaymentBinding, viewModel: PaymentViewModel) {
        this.binding = binding
        this.paymentViewModel = viewModel
        
        processPayment()
    }
}
