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
        if (amountText.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val amount = BigDecimal(amountText)
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
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
        }
    }
}
