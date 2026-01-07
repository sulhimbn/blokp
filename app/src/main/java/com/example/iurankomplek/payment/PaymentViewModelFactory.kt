package com.example.iurankomplek.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.iurankomplek.receipt.ReceiptGenerator
import com.example.iurankomplek.transaction.TransactionRepository

class PaymentViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val receiptGenerator: ReceiptGenerator
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(transactionRepository, receiptGenerator) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
