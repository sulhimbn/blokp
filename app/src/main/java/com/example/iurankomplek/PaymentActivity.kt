package com.example.iurankomplek

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import kotlinx.coroutines.launch
import com.example.iurankomplek.databinding.ActivityPaymentBinding
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentViewModel
import com.example.iurankomplek.payment.PaymentViewModelFactory
import com.example.iurankomplek.receipt.ReceiptGenerator
import com.example.iurankomplek.transaction.TransactionRepositoryFactory
import com.example.iurankomplek.utils.Constants
import java.math.BigDecimal

class PaymentActivity : BaseActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var paymentViewModel: PaymentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupPaymentProcessing()
        setupClickListeners()
    }
    
    private fun setupPaymentProcessing() {
        val transactionRepository = TransactionRepositoryFactory.getInstance(this)
        val receiptGenerator = ReceiptGenerator()
        
        paymentViewModel = ViewModelProvider(
            this,
            PaymentViewModelFactory(transactionRepository, receiptGenerator)
        )[PaymentViewModel::class.java]
    }
    
    private fun setupClickListeners() {
        binding.btnPay.setOnClickListener {
            processPayment()
        }
        
        binding.btnViewHistory.setOnClickListener {
            startActivity(android.content.Intent(this, TransactionHistoryActivity::class.java))
        }
    }
    
    private fun processPayment() {
        val amountText = binding.etAmount.text.toString().trim()
        
         // SECURITY: Validate input before processing
         if (amountText.isEmpty()) {
             Toast.makeText(this, getString(R.string.payment_enter_amount), Toast.LENGTH_SHORT).show()
             return
         }
         
         try {
             val amount = BigDecimal(amountText)
             
             // SECURITY: Validate amount is positive and within reasonable bounds
             if (amount <= BigDecimal.ZERO) {
                 Toast.makeText(this, getString(R.string.payment_amount_greater_than_zero), Toast.LENGTH_SHORT).show()
                 return
             }
             
             // SECURITY: Add maximum amount limit to prevent abuse
             val maxPaymentAmount = BigDecimal.valueOf(Constants.Payment.MAX_PAYMENT_AMOUNT)
             if (amount > maxPaymentAmount) {
                 Toast.makeText(this, getString(R.string.payment_exceeds_max_limit), Toast.LENGTH_SHORT).show()
                 return
             }
             
             // SECURITY: Check for suspicious decimal places
             if (amount.scale() > 2) {
                 Toast.makeText(this, getString(R.string.payment_max_decimal_places), Toast.LENGTH_SHORT).show()
                 return
             }
            
            val selectedMethod = when (binding.spinnerPaymentMethod.selectedItemPosition) {
                0 -> PaymentMethod.CREDIT_CARD
                1 -> PaymentMethod.BANK_TRANSFER
                2 -> PaymentMethod.E_WALLET
                3 -> PaymentMethod.VIRTUAL_ACCOUNT
                else -> PaymentMethod.CREDIT_CARD
            }
            
            // Update the ViewModel with the amount and selected method
            paymentViewModel.setAmount(amount)
            paymentViewModel.selectPaymentMethod(selectedMethod)
            
             // Set up observer for UI state changes
             lifecycleScope.launch {
                 paymentViewModel.uiState.collect { uiState ->
                     if (!uiState.isProcessing && uiState.errorMessage != null && uiState.errorMessage.isNotEmpty()) {
                         Toast.makeText(this@PaymentActivity, getString(R.string.payment_failed_with_error, uiState.errorMessage), Toast.LENGTH_LONG).show()
                     } else if (!uiState.isProcessing && uiState.errorMessage == null && uiState.amount > BigDecimal.ZERO) {
                         Toast.makeText(this@PaymentActivity, getString(R.string.payment_processed_successfully), Toast.LENGTH_LONG).show()
                     }
                 }
             }
             
             // Process payment using ViewModel
              paymentViewModel.processPayment()

          } catch (e: NumberFormatException) {
               Toast.makeText(this, getString(R.string.payment_invalid_format), Toast.LENGTH_SHORT).show()
           } catch (e: ArithmeticException) {
               Toast.makeText(this, getString(R.string.payment_invalid_value), Toast.LENGTH_SHORT).show()
           }
       }
   }
}