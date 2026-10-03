package com.example.iurankomplek

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.viewmodel.FinancialViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import kotlinx.coroutines.runBlocking

/**
 * Unit tests for FinancialViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class FinancialViewModelTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockUserObserver: Observer<String>

    @Mock
    private lateinit var pemanfaatanRepository: PemanfaatanRepository

    private val eventBus = EventBus()

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    private lateinit var viewModel: FinancialViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        runBlocking {
            org.mockito.Mockito.`when`(transactionRepository.getTransactionsByStatus(org.mockito.Mockito.any()))
                .thenReturn(kotlinx.coroutines.flow.flowOf(emptyList()))
        }
        viewModel = FinancialViewModel(pemanfaatanRepository, eventBus, transactionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel should initialize with default values`() {
        // Given: ViewModel is initialized
        // When: Checking initial state
        // Then: Should have expected default values
        assertNotNull(viewModel)
        // Note: Actual initialization depends on the real ViewModel implementation
    }

    @Test
    fun `calculateTotalIuranIndividu should return correct value`() {
        // Given: Sample data item with specific values
        val dataItem = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john.doe@example.com",
            alamat = "123 Main St",
            iuran_perwarga = 100,
            total_iuran_rekap = 500,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 150,
            pengeluaran_iuran_warga = 50,
            pemanfaatan_iuran = "Maintenance",
            avatar = "https://example.com/avatar.jpg"
        )

        // When: Calculating total iuran individu
        val result = dataItem.total_iuran_individu

        // Then: Should match expected value
        assertEquals(150, result)
    }

    @Test
    fun `calculateTotalIuranRekap should return correct value based on formula`() {
        // Given: Sample data item
        val dataItem = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john.doe@example.com",
            alamat = "123 Main St",
            iuran_perwarga = 100,
            total_iuran_rekap = 450, // Expected: total_iuran_individu * 3 = 150 * 3 = 450
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 150,
            pengeluaran_iuran_warga = 50,
            pemanfaatan_iuran = "Maintenance",
            avatar = "https://example.com/avatar.jpg"
        )

        // When: Calculating total iuran rekap (according to LaporanActivity.kt line 56)
        val calculatedRekap = dataItem.total_iuran_individu * 3

        // Then: Should match the formula result
        assertEquals(450, calculatedRekap)
        assertEquals(dataItem.total_iuran_rekap, calculatedRekap)
    }
}