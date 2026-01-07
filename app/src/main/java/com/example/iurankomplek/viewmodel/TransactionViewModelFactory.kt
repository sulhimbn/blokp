package com.example.iurankomplek.viewmodel

object TransactionViewModelFactory {
    private var instance: TransactionViewModelFactory? = null

    fun getInstance(transactionRepository: com.example.iurankomplek.transaction.TransactionRepository): TransactionViewModel.Factory {
        return TransactionViewModel.Factory(transactionRepository)
    }
}
