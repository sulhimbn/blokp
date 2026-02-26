package com.example.iurankomplek.di

import android.content.Context
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.PemanfaatanRepositoryImpl
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.data.repository.UserRepositoryImpl
import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.data.repository.VendorRepositoryImpl
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.network.NetworkStatusListener
import com.example.iurankomplek.payment.PaymentGateway
import com.example.iurankomplek.payment.RealPaymentGateway
import com.example.iurankomplek.transaction.TransactionDao
import com.example.iurankomplek.transaction.TransactionDatabase
import com.example.iurankomplek.data.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return ApiConfig.getApiService()
    }

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService): UserRepository {
        return UserRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun providePemanfaatanRepository(apiService: ApiService): PemanfaatanRepository {
        return PemafaatanRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideVendorRepository(apiService: ApiService): VendorRepository {
        return VendorRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun providePaymentGateway(apiService: ApiService): PaymentGateway {
        return RealPaymentGateway(apiService)
    }

    @Provides
    @Singleton
    fun provideTransactionDatabase(@ApplicationContext context: Context): TransactionDatabase {
        return TransactionDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: TransactionDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        paymentGateway: PaymentGateway,
        transactionDao: TransactionDao
    ): com.example.iurankomplek.data.repository.TransactionRepository {
        return TransactionRepository(paymentGateway, transactionDao)
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
}
