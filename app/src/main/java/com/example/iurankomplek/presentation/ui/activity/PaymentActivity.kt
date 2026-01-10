package com.example.iurankomplek.presentation.ui.activity

import com.example.iurankomplek.core.base.BaseActivity
import com.example.iurankomplek.R
import android.os.Bundle
import android.text.TextWatcher
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
        setupInputListeners()
    }

    private fun setupViewModel() {
        paymentViewModel = DependencyContainer.providePaymentViewModel()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            paymentViewModel.paymentEvent.collect { event ->
                when (event) {
                    is PaymentEvent.Processing -> {
                        clearInlineErrors()
                        binding.progressBar.visibility = android.view.View.VISIBLE
                        binding.btnPay.isEnabled = false
                    }
                    is PaymentEvent.Success -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        binding.btnPay.isEnabled = true
                        Toast.makeText(this@PaymentActivity, getString(R.string.payment_processed_successfully), Toast.LENGTH_LONG).show()
                    }
                    is PaymentEvent.Error -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        binding.btnPay.isEnabled = true
                        binding.tilAmount.error = getString(R.string.payment_failed_with_error, event.message)
                    }
                    is PaymentEvent.ValidationError -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        binding.btnPay.isEnabled = true
                        binding.tilAmount.error = event.message
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

    private fun setupInputListeners() {
        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (binding.tilAmount.error != null && s?.isNotBlank() == true) {
                    binding.tilAmount.error = null
                }
            }
        })
    }

    private fun clearInlineErrors() {
        binding.tilAmount.error = null
    }
}
