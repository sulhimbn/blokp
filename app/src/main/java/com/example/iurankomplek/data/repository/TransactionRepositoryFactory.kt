package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.dao.TransactionDao
import com.example.iurankomplek.data.database.TransactionDatabase
import com.example.iurankomplek.payment.MockPaymentGateway
import com.example.iurankomplek.payment.RealPaymentGateway
import com.example.iurankomplek.network.ApiConfig
import android.content.Context

object TransactionRepositoryFactory {
    @Volatile
    private var instance: TransactionRepository? = null

    fun getInstance(context: Context): TransactionRepository {
        return instance ?: synchronized(this) {
            val database = TransactionDatabase.getDatabase(context)
            val dao = database.transactionDao()
            val paymentGateway = RealPaymentGateway(ApiConfig.getApiService())

            val newInstance = TransactionRepositoryImpl(paymentGateway, dao)
            instance = newInstance
            newInstance
        }
    }

    fun getMockInstance(context: Context): TransactionRepository {
        val database = TransactionDatabase.getDatabase(context)
        val dao = database.transactionDao()
        val paymentGateway = MockPaymentGateway()
        return TransactionRepositoryImpl(paymentGateway, dao)
    }
}
