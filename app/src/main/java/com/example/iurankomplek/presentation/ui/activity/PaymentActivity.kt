package com.example.iurankomplek.presentation.ui.activity

import com.example.iurankomplek.core.base.BaseActivity
import com.example.iurankomplek.R
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.iurankomplek.databinding.ActivityPaymentBinding
import com.example.iurankomplek.payment.PaymentEvent
import com.example.iurankomplek.payment.PaymentViewModel
import com.example.iurankomplek.di.DependencyContainer

class PaymentActivity : BaseActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var paymentViewModel: PaymentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupViewModel() {
        paymentViewModel = DependencyContainer.providePaymentViewModel()
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            paymentViewModel.paymentEvent.collect { event ->
                when (event) {
                    is PaymentEvent.Processing -> {
                    }
                    is PaymentEvent.Success -> {
                        Toast.makeText(this@PaymentActivity, getString(R.string.payment_processed_successfully), Toast.LENGTH_LONG).show()
                    }
                    is PaymentEvent.Error -> {
                        Toast.makeText(this@PaymentActivity, getString(R.string.payment_failed_with_error, event.message), Toast.LENGTH_LONG).show()
                    }
                    is PaymentEvent.ValidationError -> {
                        Toast.makeText(this@PaymentActivity, event.message, Toast.LENGTH_SHORT).show()
                    }
                    null -> { }
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnPay.setOnClickListener {
            val amountText = binding.etAmount.text.toString().trim()
            val spinnerPosition = binding.spinnerPaymentMethod.selectedItemPosition
            paymentViewModel.validateAndProcessPayment(amountText, spinnerPosition)
        }
        
        binding.btnViewHistory.setOnClickListener {
            startActivity(android.content.Intent(this, TransactionHistoryActivity::class.java))
        }
    }
}
