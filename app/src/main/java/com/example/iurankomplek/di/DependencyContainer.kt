package com.example.iurankomplek.di

import android.content.Context
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.PemanfaatanRepositoryFactory
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.data.repository.TransactionRepositoryFactory
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.data.repository.UserRepositoryFactory
import com.example.iurankomplek.domain.usecase.CalculateFinancialSummaryUseCase
import com.example.iurankomplek.domain.usecase.CalculateFinancialTotalsUseCase
import com.example.iurankomplek.domain.usecase.LoadFinancialDataUseCase
import com.example.iurankomplek.domain.usecase.LoadUsersUseCase
import com.example.iurankomplek.domain.usecase.PaymentSummaryIntegrationUseCase
import com.example.iurankomplek.domain.usecase.ValidateFinancialDataUseCase

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
    
    /**
     * Initialize the DI container with application context
     * Call once from Application.onCreate()
     */
    fun initialize(context: Context) {
        this.context = context.applicationContext
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
     * Reset all cached instances (useful for testing)
     */
    fun reset() {
        context = null
    }
}
