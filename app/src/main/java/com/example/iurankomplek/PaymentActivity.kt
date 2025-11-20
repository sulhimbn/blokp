package com.example.iurankomplek

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentRequest
import com.example.iurankomplek.payment.PaymentViewModel
import com.example.iurankomplek.receipt.ReceiptGenerator
import com.example.iurankomplek.payment.MockPaymentGateway
import com.example.iurankomplek.transaction.TransactionDao
import com.example.iurankomplek.transaction.TransactionRepository
import java.math.BigDecimal

class PaymentActivity : AppCompatActivity() {
    private lateinit var amountEditText: EditText
    private lateinit var paymentMethodSpinner: Spinner
    private lateinit var payButton: Button
    
    // In a real implementation, these would be injected via Hilt or similar
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var receiptGenerator: ReceiptGenerator
    private lateinit var paymentViewModel: PaymentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        
        initViews()
        setupPaymentProcessing()
        setupClickListeners()
    }
    
    private fun initViews() {
        amountEditText = findViewById(R.id.et_amount)
        paymentMethodSpinner = findViewById(R.id.spinner_payment_method)
        payButton = findViewById(R.id.btn_pay)
    }
    
    private fun setupPaymentProcessing() {
        // Note: In a real app, these would be properly injected
        // For this implementation, we're creating simplified versions
        val mockPaymentGateway = MockPaymentGateway()
        val transactionDatabase = com.example.iurankomplek.transaction.TransactionDatabase.getDatabase(this)
        val transactionDao = transactionDatabase.transactionDao()
        transactionRepository = TransactionRepository(mockPaymentGateway, transactionDao)
        receiptGenerator = ReceiptGenerator()
        paymentViewModel = PaymentViewModel(transactionRepository, receiptGenerator)
    }
    
    private fun setupClickListeners() {
        payButton.setOnClickListener {
            processPayment()
        }
    }
    
    private fun processPayment() {
        val amountText = amountEditText.text.toString()
        if (amountText.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val amount = BigDecimal(amountText)
            val selectedMethod = when (paymentMethodSpinner.selectedItemPosition) {
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
            paymentViewModel.uiState.observe(this) { uiState ->
                if (!uiState.isProcessing && uiState.errorMessage != null && uiState.errorMessage.isNotEmpty()) {
                    Toast.makeText(this, "Payment failed: ${uiState.errorMessage}", Toast.LENGTH_LONG).show()
                } else if (!uiState.isProcessing && uiState.errorMessage == null && uiState.amount > BigDecimal.ZERO) {
                    Toast.makeText(this, "Payment processed successfully!", Toast.LENGTH_LONG).show()
                }
            }
            
            // Process the payment using the ViewModel
            paymentViewModel.processPayment()
            
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
        }
    }
}