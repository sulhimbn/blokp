package com.example.iurankomplek.transaction

import android.content.Context
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.payment.PaymentGateway
import com.example.iurankomplek.payment.RealPaymentGateway

object TransactionRepositoryFactory {
    private var instance: TransactionRepository? = null

    fun getInstance(context: Context): TransactionRepository {
        return instance ?: synchronized(this) {
            instance ?: createInstance(context).also { instance = it }
        }
    }

    private fun createInstance(context: Context): TransactionRepository {
        val apiService = ApiConfig.getApiService()
        val paymentGateway: PaymentGateway = RealPaymentGateway(apiService)
        val database = TransactionDatabase.getDatabase(context)
        val transactionDao = database.transactionDao()
        
        return TransactionRepositoryImpl(paymentGateway, transactionDao)
    }

    fun getMockInstance(context: Context): TransactionRepository {
        val paymentGateway = com.example.iurankomplek.payment.MockPaymentGateway()
        val database = TransactionDatabase.getDatabase(context)
        val transactionDao = database.transactionDao()
        
        return TransactionRepositoryImpl(paymentGateway, transactionDao)
    }

    fun createInstance(paymentGateway: PaymentGateway, context: Context): TransactionRepository {
        val database = TransactionDatabase.getDatabase(context)
        val transactionDao = database.transactionDao()
        
        return TransactionRepositoryImpl(paymentGateway, transactionDao)
    }
}
