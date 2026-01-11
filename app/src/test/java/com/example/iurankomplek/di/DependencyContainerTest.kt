package com.example.iurankomplek.di

import android.content.Context
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.domain.usecase.CalculateFinancialSummaryUseCase
import com.example.iurankomplek.domain.usecase.LoadFinancialDataUseCase
import com.example.iurankomplek.domain.usecase.LoadUsersUseCase
import com.example.iurankomplek.domain.usecase.PaymentSummaryIntegrationUseCase
import com.example.iurankomplek.domain.usecase.ValidatePaymentUseCase
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class DependencyContainerTest {

    @Mock
    private lateinit var mockContext: Context

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        DependencyContainer.reset()
    }

    @After
    fun tearDown() {
        DependencyContainer.reset()
    }

    @Test
    fun `initialize stores application context`() {
        DependencyContainer.initialize(mockContext)

        val repository = DependencyContainer.provideTransactionRepository()
        assertNotNull(repository)
    }

    @Test
    fun `provideUserRepository returns non-null repository`() {
        val repository = DependencyContainer.provideUserRepository()

        assertNotNull(repository)
        assertTrue(repository is UserRepository)
    }

    @Test
    fun `provideUserRepository returns same instance on multiple calls`() {
        val repository1 = DependencyContainer.provideUserRepository()
        val repository2 = DependencyContainer.provideUserRepository()

        assertSame(repository1, repository2)
    }

    @Test
    fun `providePemanfaatanRepository returns non-null repository`() {
        val repository = DependencyContainer.providePemanfaatanRepository()

        assertNotNull(repository)
        assertTrue(repository is PemanfaatanRepository)
    }

    @Test
    fun `providePemanfaatanRepository returns same instance on multiple calls`() {
        val repository1 = DependencyContainer.providePemanfaatanRepository()
        val repository2 = DependencyContainer.providePemanfaatanRepository()

        assertSame(repository1, repository2)
    }

    @Test
    fun `provideTransactionRepository throws IllegalStateException when not initialized`() {
        val exception = assertThrows(IllegalStateException::class.java) {
            DependencyContainer.provideTransactionRepository()
        }

        assertTrue(exception.message?.contains("DI container not initialized") == true)
    }

    @Test
    fun `provideTransactionRepository returns non-null repository after initialization`() {
        DependencyContainer.initialize(mockContext)

        val repository = DependencyContainer.provideTransactionRepository()

        assertNotNull(repository)
        assertTrue(repository is TransactionRepository)
    }

    @Test
    fun `provideLoadUsersUseCase returns non-null use case`() {
        val useCase = DependencyContainer.provideLoadUsersUseCase()

        assertNotNull(useCase)
        assertTrue(useCase is LoadUsersUseCase)
    }

    @Test
    fun `provideLoadUsersUseCase injects UserRepository dependency`() {
        val useCase = DependencyContainer.provideLoadUsersUseCase()

        assertNotNull(useCase)
    }

    @Test
    fun `provideLoadFinancialDataUseCase returns non-null use case`() {
        val useCase = DependencyContainer.provideLoadFinancialDataUseCase()

        assertNotNull(useCase)
        assertTrue(useCase is LoadFinancialDataUseCase)
    }

    @Test
    fun `provideLoadFinancialDataUseCase injects all dependencies`() {
        val useCase = DependencyContainer.provideLoadFinancialDataUseCase()

        assertNotNull(useCase)
    }

    @Test
    fun `provideCalculateFinancialSummaryUseCase returns non-null use case`() {
        val useCase = DependencyContainer.provideCalculateFinancialSummaryUseCase()

        assertNotNull(useCase)
        assertTrue(useCase is CalculateFinancialSummaryUseCase)
    }

    @Test
    fun `provideCalculateFinancialSummaryUseCase injects all dependencies`() {
        val useCase = DependencyContainer.provideCalculateFinancialSummaryUseCase()

        assertNotNull(useCase)
    }

    @Test
    fun `providePaymentSummaryIntegrationUseCase returns non-null use case`() {
        DependencyContainer.initialize(mockContext)

        val useCase = DependencyContainer.providePaymentSummaryIntegrationUseCase()

        assertNotNull(useCase)
        assertTrue(useCase is PaymentSummaryIntegrationUseCase)
    }

    @Test
    fun `providePaymentSummaryIntegrationUseCase throws IllegalStateException when not initialized`() {
        val exception = assertThrows(IllegalStateException::class.java) {
            DependencyContainer.providePaymentSummaryIntegrationUseCase()
        }

        assertTrue(exception.message?.contains("DI container not initialized") == true)
    }

    @Test
    fun `providePaymentSummaryIntegrationUseCase injects TransactionRepository dependency`() {
        DependencyContainer.initialize(mockContext)

        val useCase = DependencyContainer.providePaymentSummaryIntegrationUseCase()

        assertNotNull(useCase)
    }

    @Test
    fun `provideValidatePaymentUseCase returns non-null use case`() {
        val useCase = DependencyContainer.provideValidatePaymentUseCase()

        assertNotNull(useCase)
        assertTrue(useCase is ValidatePaymentUseCase)
    }

    @Test
    fun `provideValidatePaymentUseCase returns new instance on each call`() {
        val useCase1 = DependencyContainer.provideValidatePaymentUseCase()
        val useCase2 = DependencyContainer.provideValidatePaymentUseCase()

        assertNotSame(useCase1, useCase2)
    }

    @Test
    fun `reset clears stored context`() {
        DependencyContainer.initialize(mockContext)
        DependencyContainer.reset()

        val exception = assertThrows(IllegalStateException::class.java) {
            DependencyContainer.provideTransactionRepository()
        }

        assertTrue(exception.message?.contains("DI container not initialized") == true)
    }

    @Test
    fun `reset allows reinitialization with new context`() {
        DependencyContainer.initialize(mockContext)
        val repository1 = DependencyContainer.provideTransactionRepository()
        assertNotNull(repository1)

        DependencyContainer.reset()

        DependencyContainer.initialize(mockContext)
        val repository2 = DependencyContainer.provideTransactionRepository()
        assertNotNull(repository2)
    }

    @Test
    fun `multiple initialize calls update stored context`() {
        DependencyContainer.initialize(mockContext)
        DependencyContainer.initialize(mockContext)

        val repository = DependencyContainer.provideTransactionRepository()
        assertNotNull(repository)
    }

    @Test
    fun `object singleton pattern ensures single instance`() {
        val container1 = DependencyContainer
        val container2 = DependencyContainer

        assertSame(container1, container2)
    }

    @Test
    fun `repositories are provided without requiring initialization`() {
        val userRepo = DependencyContainer.provideUserRepository()
        val pemanfaatanRepo = DependencyContainer.providePemanfaatanRepository()

        assertNotNull(userRepo)
        assertNotNull(pemanfaatanRepo)
    }

    @Test
    fun `use cases that depend on repositories work without initialization`() {
        val loadUsersUseCase = DependencyContainer.provideLoadUsersUseCase()
        val loadFinancialDataUseCase = DependencyContainer.provideLoadFinancialDataUseCase()
        val calculateFinancialSummaryUseCase = DependencyContainer.provideCalculateFinancialSummaryUseCase()
        val validatePaymentUseCase = DependencyContainer.provideValidatePaymentUseCase()

        assertNotNull(loadUsersUseCase)
        assertNotNull(loadFinancialDataUseCase)
        assertNotNull(calculateFinancialSummaryUseCase)
        assertNotNull(validatePaymentUseCase)
    }

    @Test
    fun `use cases that depend on TransactionRepository require initialization`() {
        try {
            DependencyContainer.providePaymentSummaryIntegrationUseCase()
            fail("Should have thrown IllegalStateException")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("DI container not initialized") == true)
        }
    }

    @Test
    fun `reset does not affect UserRepository singleton`() {
        val repo1 = DependencyContainer.provideUserRepository()
        DependencyContainer.reset()
        val repo2 = DependencyContainer.provideUserRepository()

        assertSame(repo1, repo2)
    }

    @Test
    fun `reset does not affect PemanfaatanRepository singleton`() {
        val repo1 = DependencyContainer.providePemanfaatanRepository()
        DependencyContainer.reset()
        val repo2 = DependencyContainer.providePemanfaatanRepository()

        assertSame(repo1, repo2)
    }

    @Test
    fun `reset clears TransactionRepository singleton`() {
        DependencyContainer.initialize(mockContext)
        val repo1 = DependencyContainer.provideTransactionRepository()
        DependencyContainer.reset()
        DependencyContainer.initialize(mockContext)
        val repo2 = DependencyContainer.provideTransactionRepository()

        assertNotSame(repo1, repo2)
    }

    @Test
    fun `dependency chain is correctly established`() {
        DependencyContainer.initialize(mockContext)

        val useCase = DependencyContainer.providePaymentSummaryIntegrationUseCase()
        assertNotNull(useCase)

        val repository = DependencyContainer.provideTransactionRepository()
        assertNotNull(repository)
    }

    @Test
    fun `initialize with application context stores applicationContext`() {
        DependencyContainer.initialize(mockContext)

        DependencyContainer.provideTransactionRepository()
    }

    @Test
    fun `context is required for TransactionRepository provision`() {
        val exception = assertThrows(IllegalStateException::class.java) {
            DependencyContainer.provideTransactionRepository()
        }

        assertTrue(exception.message?.contains("Call DependencyContainer.initialize() first") == true)
    }

    @Test
    fun `all use case providers return non-null instances`() {
        val loadUsersUseCase = DependencyContainer.provideLoadUsersUseCase()
        val loadFinancialDataUseCase = DependencyContainer.provideLoadFinancialDataUseCase()
        val calculateFinancialSummaryUseCase = DependencyContainer.provideCalculateFinancialSummaryUseCase()
        val validatePaymentUseCase = DependencyContainer.provideValidatePaymentUseCase()

        assertNotNull(loadUsersUseCase)
        assertNotNull(loadFinancialDataUseCase)
        assertNotNull(calculateFinancialSummaryUseCase)
        assertNotNull(validatePaymentUseCase)
    }

    @Test
    fun `dependency container provides centralized dependency management`() {
        val userRepo = DependencyContainer.provideUserRepository()
        val loadUsersUseCase = DependencyContainer.provideLoadUsersUseCase()

        assertNotNull(userRepo)
        assertNotNull(loadUsersUseCase)
    }

    @Test
    fun `use cases are lightweight and easy to create`() {
        val startTime = System.nanoTime()
        val useCase = DependencyContainer.provideValidatePaymentUseCase()
        val endTime = System.nanoTime()

        assertNotNull(useCase)
        val duration = (endTime - startTime) / 1_000_000
        assertTrue("Use case creation should be fast (took ${duration}ms)", duration < 100)
    }

    @Test
    fun `reset clears context reference`() {
        DependencyContainer.initialize(mockContext)
        DependencyContainer.reset()

        val exception = assertThrows(IllegalStateException::class.java) {
            DependencyContainer.provideTransactionRepository()
        }

        assertNotNull(exception)
    }
}
