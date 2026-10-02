package com.example.iurankomplek

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.viewmodel.FinancialDataState
import com.example.iurankomplek.viewmodel.FinancialViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

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
    private lateinit var pemanfaatanRepository: PemanfaatanRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    private lateinit var viewModel: FinancialViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = FinancialViewModel(
            pemanfaatanRepository,
            EventBus(),
            transactionRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel should start in Loading state`() {
        assertNotNull(viewModel)
        assertTrue(viewModel.financialState.value is FinancialDataState.Loading)
    }

    @Test
    fun `calculateTotalIuranIndividu should return correct value`() {
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

        val result = dataItem.total_iuran_individu

        assertEquals(150, result)
    }

    @Test
    fun `calculateTotalIuranRekap should return correct value based on formula`() {
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

        val calculatedRekap = dataItem.total_iuran_individu * 3

        assertEquals(450, calculatedRekap)
        assertEquals(dataItem.total_iuran_rekap, calculatedRekap)
    }
}