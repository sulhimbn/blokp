package com.example.iurankomplek.di

import android.content.Context
import com.example.iurankomplek.data.repository.AnnouncementRepository
import com.example.iurankomplek.data.repository.AnnouncementRepositoryFactory
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.PemanfaatanRepositoryFactory
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.data.repository.TransactionRepositoryFactory
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.data.repository.UserRepositoryFactory
import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.data.repository.VendorRepositoryFactory
import com.example.iurankomplek.data.repository.MessageRepository
import com.example.iurankomplek.data.repository.MessageRepositoryFactory
import com.example.iurankomplek.data.repository.CommunityPostRepository
import com.example.iurankomplek.data.repository.CommunityPostRepositoryFactory
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
    
    /**
     * Initialize the DI container with application context
     * Call once from Application.onCreate()
     */
    fun initialize(context: Context) {
        this.context = context.applicationContext
        this.receiptGenerator = com.example.iurankomplek.utils.ReceiptGenerator()
    }
    
    /**
     * Provide UserRepository instance
     */
    fun provideUserRepository(): UserRepository {
        return UserRepositoryFactory.getInstance()
    }
    
    /**
     * Provide PemanfaatanRepository instance
     */
    fun providePemanfaatanRepository(): PemanfaatanRepository {
        return PemanfaatanRepositoryFactory.getInstance()
    }
    
    /**
     * Provide TransactionRepository instance
     */
    fun provideTransactionRepository(): TransactionRepository {
        return TransactionRepositoryFactory.getMockInstance(
            context ?: throw IllegalStateException("DI container not initialized. Call DependencyContainer.initialize() first.")
        )
    }
    
    fun provideAnnouncementRepository(): AnnouncementRepository {
        return AnnouncementRepositoryFactory.getInstance()
    }
    
    fun provideMessageRepository(): MessageRepository {
        return MessageRepositoryFactory.getInstance()
    }
    
    fun provideCommunityPostRepository(): CommunityPostRepository {
        return CommunityPostRepositoryFactory.getInstance()
    }
    
    fun provideVendorRepository(): VendorRepository {
        return VendorRepositoryFactory.getInstance()
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
        val validateFinancialDataUseCase = ValidateFinancialDataUseCase()
        val calculateFinancialTotalsUseCase = CalculateFinancialTotalsUseCase()
        val validateFinancialDataWithDeps = ValidateFinancialDataUseCase(calculateFinancialTotalsUseCase)
        return LoadFinancialDataUseCase(providePemanfaatanRepository(), validateFinancialDataWithDeps)
    }
    
    /**
     * Provide CalculateFinancialSummaryUseCase instance with all dependencies
     */
    fun provideCalculateFinancialSummaryUseCase(): CalculateFinancialSummaryUseCase {
        val validateFinancialDataUseCase = ValidateFinancialDataUseCase()
        val calculateFinancialTotalsUseCase = CalculateFinancialTotalsUseCase()
        val validateFinancialDataWithDeps = ValidateFinancialDataUseCase(calculateFinancialTotalsUseCase)
        return CalculateFinancialSummaryUseCase(validateFinancialDataWithDeps, calculateFinancialTotalsUseCase)
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
    }
}
