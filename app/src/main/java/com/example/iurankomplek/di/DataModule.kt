package com.example.iurankomplek.di

import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.PemanfaatanRepositoryImpl
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.data.repository.UserRepositoryImpl
import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.data.repository.VendorRepositoryImpl
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.payment.PaymentGateway
import com.example.iurankomplek.payment.RealPaymentGateway
import com.example.iurankomplek.session.UserSessionManager
import com.example.iurankomplek.transaction.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService = ApiConfig.getApiService()

    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        sessionManager: UserSessionManager
    ): UserRepository = UserRepositoryImpl(apiService, sessionManager)

    @Provides
    @Singleton
    fun providePemanfaatanRepository(apiService: ApiService): PemanfaatanRepository =
        PemanfaatanRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun provideVendorRepository(apiService: ApiService): VendorRepository =
        VendorRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun providePaymentGateway(apiService: ApiService): PaymentGateway = RealPaymentGateway(apiService)

    @Provides
    @Singleton
    fun provideTransactionRepository(
        paymentGateway: PaymentGateway,
        transactionDao: TransactionDao
    ): com.example.iurankomplek.data.repository.TransactionRepository =
        com.example.iurankomplek.transaction.TransactionRepository(paymentGateway, transactionDao)
}