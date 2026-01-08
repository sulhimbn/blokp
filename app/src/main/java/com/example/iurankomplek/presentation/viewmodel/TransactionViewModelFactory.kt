package com.example.iurankomplek.presentation.viewmodel

object TransactionViewModelFactory {
    private var instance: TransactionViewModelFactory? = null

    fun getInstance(transactionRepository: com.example.iurankomplek.data.repository.TransactionRepository): TransactionViewModel.Factory {
        return TransactionViewModel.Factory(transactionRepository)
    }
}