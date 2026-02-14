package com.example.iurankomplek

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.iurankomplek.databinding.ActivityPaymentBinding
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal

@AndroidEntryPoint
class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private val paymentViewModel: PaymentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeUiState()
    }

    private fun setupClickListeners() {
        binding.btnPay.setOnClickListener {
            processPayment()
        }

        binding.btnViewHistory.setOnClickListener {
            startActivity(android.content.Intent(this, TransactionHistoryActivity::class.java))
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                paymentViewModel.uiState.collectLatest { uiState ->
                    uiState?.let { safeState ->
                        if (!safeState.isProcessing && safeState.errorMessage != null) {
                            Toast.makeText(
                                this@PaymentActivity,
                                "Payment failed: ${safeState.errorMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        } else if (!safeState.isProcessing && safeState.errorMessage == null && safeState.amount > BigDecimal.ZERO) {
                            Toast.makeText(
                                this@PaymentActivity,
                                "Payment processed successfully!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } ?: run {
                        android.util.Log.w("PaymentActivity", "Received null UI state")
                    }
                }
            }
        }
    }

    private fun processPayment() {
        val amountText = binding.etAmount.text.toString()
        
        when (val validationResult = validatePaymentAmount(amountText)) {
            is ValidationResult.Failure -> {
                Toast.makeText(this, validationResult.message, Toast.LENGTH_SHORT).show()
                return
            }
            is ValidationResult.Success -> {
                val amount = validationResult.amount
                val selectedMethod = when (binding.spinnerPaymentMethod.selectedItemPosition) {
                    0 -> PaymentMethod.CREDIT_CARD
                    1 -> PaymentMethod.BANK_TRANSFER
                    2 -> PaymentMethod.E_WALLET
                    3 -> PaymentMethod.VIRTUAL_ACCOUNT
                    else -> PaymentMethod.CREDIT_CARD
                }

                paymentViewModel.setAmount(amount)
                paymentViewModel.selectPaymentMethod(selectedMethod)
                paymentViewModel.processPayment()
            }
        }
    }

    private fun validatePaymentAmount(input: String): ValidationResult {
        if (input.isBlank()) {
            return ValidationResult.Failure("Amount cannot be empty")
        }

        return try {
            val amount = BigDecimal(input)
            when {
                amount <= BigDecimal.ZERO -> 
                    ValidationResult.Failure("Amount must be positive")
                amount > MAX_PAYMENT_AMOUNT -> 
                    ValidationResult.Failure("Amount exceeds maximum limit of Rp $MAX_PAYMENT_AMOUNT")
                amount.scale() > 2 -> 
                    ValidationResult.Failure("Amount cannot have more than 2 decimal places")
                else -> ValidationResult.Success(amount)
            }
        } catch (e: NumberFormatException) {
            ValidationResult.Failure("Invalid amount format")
        }
    }

    private sealed class ValidationResult {
        data class Success(val amount: BigDecimal) : ValidationResult()
        data class Failure(val message: String) : ValidationResult()
    }

    companion object {
        private val MAX_PAYMENT_AMOUNT = BigDecimal("999999.99")
    }
}
