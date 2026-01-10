package com.example.iurankomplek.di

import android.content.Context
import com.example.iurankomplek.data.repository.AnnouncementRepository
import com.example.iurankomplek.data.repository.AnnouncementRepositoryImpl
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.PemanfaatanRepositoryImpl
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.data.repository.TransactionRepositoryImpl
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.data.repository.UserRepositoryImpl
import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.data.repository.VendorRepositoryImpl
import com.example.iurankomplek.data.repository.MessageRepository
import com.example.iurankomplek.data.repository.MessageRepositoryImpl
import com.example.iurankomplek.data.repository.CommunityPostRepository
import com.example.iurankomplek.data.repository.CommunityPostRepositoryImpl
import com.example.iurankomplek.data.dao.TransactionDao
import com.example.iurankomplek.data.database.AppDatabase
import com.example.iurankomplek.payment.PaymentGateway
import com.example.iurankomplek.payment.RealPaymentGateway
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiServiceV1
import com.example.iurankomplek.network.ApiService
import kotlinx.coroutines.CoroutineScope
import com.example.iurankomplek.presentation.viewmodel.UserViewModel
import com.example.iurankomplek.presentation.viewmodel.FinancialViewModel
import com.example.iurankomplek.payment.PaymentViewModel
import com.example.iurankomplek.presentation.viewmodel.VendorViewModel
import com.example.iurankomplek.presentation.viewmodel.TransactionViewModel
import com.example.iurankomplek.presentation.viewmodel.AnnouncementViewModel
import com.example.iurankomplek.presentation.viewmodel.MessageViewModel
import com.example.iurankomplek.presentation.viewmodel.CommunityPostViewModel
import com.example.iurankomplek.domain.usecase.CalculateFinancialSummaryUseCase
import com.example.iurankomplek.domain.usecase.CalculateFinancialTotalsUseCase
import com.example.iurankomplek.domain.usecase.LoadFinancialDataUseCase
import com.example.iurankomplek.domain.usecase.LoadUsersUseCase
import com.example.iurankomplek.domain.usecase.PaymentSummaryIntegrationUseCase
import com.example.iurankomplek.domain.usecase.ValidateFinancialDataUseCase
import com.example.iurankomplek.domain.usecase.ValidatePaymentUseCase

/**
 * Simple Dependency Injection container for managing application dependencies
 * Provides centralized dependency creation and eliminates tight coupling
 * 
 * This is a pragmatic DI solution that:
 * - Provides single source of truth for dependencies
 * - Eliminates direct Factory and UseCase instantiation in Activities
 * - Is testable and maintainable
 * - Simple to implement without external DI frameworks
 */
object DependencyContainer {
    
    private var context: Context? = null
    
    @Volatile
    private var receiptGenerator: com.example.iurankomplek.utils.ReceiptGenerator? = null
    
    @Volatile
    private var userRepository: UserRepository? = null
    
    @Volatile
    private var pemanfaatanRepository: PemanfaatanRepository? = null
    
    @Volatile
    private var vendorRepository: VendorRepository? = null
    
    @Volatile
    private var announcementRepository: AnnouncementRepository? = null
    
    @Volatile
    private var messageRepository: MessageRepository? = null
    
    @Volatile
    private var communityPostRepository: CommunityPostRepository? = null
    
    @Volatile
    private var transactionRepository: TransactionRepository? = null
    
    @Volatile
    private var paymentGateway: PaymentGateway? = null
    
    @Volatile
    private var transactionDao: TransactionDao? = null
    
    /**
     * Initialize DI container with application context
     * Call once from Application.onCreate()
     */
    fun initialize(context: Context) {
        this.context = context.applicationContext
        this.receiptGenerator = com.example.iurankomplek.utils.ReceiptGenerator()
    }
    
    private fun getApiServiceV1(): ApiServiceV1 {
        return ApiConfig.getApiServiceV1()
    }
    
    private fun getApiService(): ApiService {
        return ApiConfig.getApiService()
    }
    
    private fun getTransactionDao(): TransactionDao {
        return transactionDao ?: synchronized(this) {
            transactionDao ?: AppDatabase.getDatabase(
                context ?: throw IllegalStateException("DI container not initialized. Call DependencyContainer.initialize() first."),
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
            ).transactionDao().also { transactionDao = it }
        }
    }
    
    private fun getPaymentGateway(): PaymentGateway {
        return paymentGateway ?: synchronized(this) {
            paymentGateway ?: RealPaymentGateway(getApiService()).also { paymentGateway = it }
        }
    }
    
    private fun getCalculateFinancialTotalsUseCase(): CalculateFinancialTotalsUseCase {
        return CalculateFinancialTotalsUseCase()
    }
    
    private fun getValidateFinancialDataUseCase(): ValidateFinancialDataUseCase {
        return ValidateFinancialDataUseCase(getCalculateFinancialTotalsUseCase())
    }
    
    /**
     * Provide UserRepository instance
     */
    fun provideUserRepository(): UserRepository {
        return userRepository ?: synchronized(this) {
            userRepository ?: UserRepositoryImpl(getApiServiceV1()).also { userRepository = it }
        }
    }
    
    /**
     * Provide PemanfaatanRepository instance
     */
    fun providePemanfaatanRepository(): PemanfaatanRepository {
        return pemanfaatanRepository ?: synchronized(this) {
            pemanfaatanRepository ?: PemanfaatanRepositoryImpl(getApiServiceV1()).also { pemanfaatanRepository = it }
        }
    }
    
    /**
     * Provide TransactionRepository instance
     */
    fun provideTransactionRepository(): TransactionRepository {
        return transactionRepository ?: synchronized(this) {
            transactionRepository ?: TransactionRepositoryImpl(getPaymentGateway(), getTransactionDao()).also { transactionRepository = it }
        }
    }
    
    fun provideAnnouncementRepository(): AnnouncementRepository {
        return announcementRepository ?: synchronized(this) {
            announcementRepository ?: AnnouncementRepositoryImpl(getApiServiceV1()).also { announcementRepository = it }
        }
    }
    
    fun provideMessageRepository(): MessageRepository {
        return messageRepository ?: synchronized(this) {
            messageRepository ?: MessageRepositoryImpl(getApiServiceV1()).also { messageRepository = it }
        }
    }
    
    fun provideCommunityPostRepository(): CommunityPostRepository {
        return communityPostRepository ?: synchronized(this) {
            communityPostRepository ?: CommunityPostRepositoryImpl(getApiServiceV1()).also { communityPostRepository = it }
        }
    }
    
    fun provideVendorRepository(): VendorRepository {
        return vendorRepository ?: synchronized(this) {
            vendorRepository ?: VendorRepositoryImpl(getApiServiceV1()).also { vendorRepository = it }
        }
    }
    
    /**
     * Provide LoadUsersUseCase instance with all dependencies
     */
    fun provideLoadUsersUseCase(): LoadUsersUseCase {
        return LoadUsersUseCase(provideUserRepository())
    }
    
    /**
     * Provide LoadFinancialDataUseCase instance with all dependencies
     */
    fun provideLoadFinancialDataUseCase(): LoadFinancialDataUseCase {
        return LoadFinancialDataUseCase(providePemanfaatanRepository(), getValidateFinancialDataUseCase())
    }
    
    /**
     * Provide CalculateFinancialSummaryUseCase instance with all dependencies
     */
    fun provideCalculateFinancialSummaryUseCase(): CalculateFinancialSummaryUseCase {
        val validateFinancialDataUseCase = getValidateFinancialDataUseCase()
        val calculateFinancialTotalsUseCase = getCalculateFinancialTotalsUseCase()
        return CalculateFinancialSummaryUseCase(validateFinancialDataUseCase, calculateFinancialTotalsUseCase)
    }
    
    /**
     * Provide PaymentSummaryIntegrationUseCase instance with all dependencies
     */
    fun providePaymentSummaryIntegrationUseCase(): PaymentSummaryIntegrationUseCase {
        return PaymentSummaryIntegrationUseCase(provideTransactionRepository())
    }
    
    /**
     * Provide ValidatePaymentUseCase instance
     */
    fun provideValidatePaymentUseCase(): ValidatePaymentUseCase {
        return ValidatePaymentUseCase()
    }
    
    private fun getReceiptGenerator(): com.example.iurankomplek.utils.ReceiptGenerator {
        return receiptGenerator ?: throw IllegalStateException("ReceiptGenerator not initialized. Call DependencyContainer.initialize() first.")
    }
    
    fun provideUserViewModel(): UserViewModel {
        return UserViewModel.Factory(provideLoadUsersUseCase()).create(UserViewModel::class.java)
    }
    
    fun provideFinancialViewModel(): FinancialViewModel {
        val calculateFinancialSummaryUseCase = provideCalculateFinancialSummaryUseCase()
        val paymentSummaryIntegrationUseCase = providePaymentSummaryIntegrationUseCase()
        return FinancialViewModel.Factory(
            provideLoadFinancialDataUseCase(),
            calculateFinancialSummaryUseCase,
            paymentSummaryIntegrationUseCase
        ).create(FinancialViewModel::class.java)
    }
    
    fun providePaymentViewModel(): PaymentViewModel {
        return PaymentViewModel.Factory(
            provideTransactionRepository(),
            getReceiptGenerator(),
            provideValidatePaymentUseCase()
        ).create(PaymentViewModel::class.java)
    }
    
    fun provideVendorViewModel(): VendorViewModel {
        return VendorViewModel.Factory(provideVendorRepository()).create(VendorViewModel::class.java)
    }
    
    fun provideTransactionViewModel(): TransactionViewModel {
        return TransactionViewModel.Factory(provideTransactionRepository()).create(TransactionViewModel::class.java)
    }
    
    fun provideAnnouncementViewModel(): AnnouncementViewModel {
        return AnnouncementViewModel.Factory(provideAnnouncementRepository()).create(AnnouncementViewModel::class.java)
    }
    
    fun provideMessageViewModel(): MessageViewModel {
        return MessageViewModel.Factory(provideMessageRepository()).create(MessageViewModel::class.java)
    }
    
    fun provideCommunityPostViewModel(): CommunityPostViewModel {
        return CommunityPostViewModel.Factory(provideCommunityPostRepository()).create(CommunityPostViewModel::class.java)
    }
    
    /**
     * Reset all cached instances (useful for testing)
     */
    fun reset() {
        context = null
        receiptGenerator = null
        userRepository = null
        pemanfaatanRepository = null
        vendorRepository = null
        announcementRepository = null
        messageRepository = null
        communityPostRepository = null
        transactionRepository = null
        paymentGateway = null
        transactionDao = null
    }
}
