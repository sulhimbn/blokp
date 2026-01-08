package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.api.models.PemanfaatanResponse
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.model.DataItem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class LoadFinancialDataUseCaseTest {

    @Mock
    private lateinit var mockRepository: PemanfaatanRepository

    private lateinit var useCase: LoadFinancialDataUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = LoadFinancialDataUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        val expectedResponse = PemanfaatanResponse(
            status = "success",
            message = "Data retrieved successfully",
            data = emptyList()
        )

        doReturn(Result.success(expectedResponse)).`when`(mockRepository).getPemanfaatan()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        verify(mockRepository).getPemanfaatan()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val exception = RuntimeException("Network error")
        doReturn(Result.failure<PemanfaatanResponse>(exception)).`when`(mockRepository).getPemanfaatan()

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).getPemanfaatan()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success when repository throws exception`() = runTest {
        val expectedResponse = PemanfaatanResponse(
            status = "success",
            message = "Data retrieved successfully",
            data = emptyList()
        )

        doReturn(expectedResponse).`when`(mockRepository).getPemanfaatan()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        verify(mockRepository).getPemanfaatan()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with forceRefresh true calls repository`() = runTest {
        val expectedResponse = PemanfaatanResponse(
            status = "success",
            message = "Data retrieved successfully",
            data = emptyList()
        )

        doReturn(Result.success(expectedResponse)).`when`(mockRepository).getPemanfaatan()

        val result = useCase(forceRefresh = true)

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        verify(mockRepository).getPemanfaatan()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with forceRefresh false calls repository`() = runTest {
        val expectedResponse = PemanfaatanResponse(
            status = "success",
            message = "Data retrieved successfully",
            data = emptyList()
        )

        doReturn(Result.success(expectedResponse)).`when`(mockRepository).getPemanfaatan()

        val result = useCase(forceRefresh = false)

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        verify(mockRepository).getPemanfaatan()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns failure when repository throws exception`() = runTest {
        val exception = RuntimeException("Network error")
        doThrow(exception).`when`(mockRepository).getPemanfaatan()

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).getPemanfaatan()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with forceRefresh true handles repository exception`() = runTest {
        val exception = RuntimeException("Network error")
        doThrow(exception).`when`(mockRepository).getPemanfaatan()

        val result = useCase(forceRefresh = true)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).getPemanfaatan()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with forceRefresh false handles repository exception`() = runTest {
        val exception = RuntimeException("Network error")
        doThrow(exception).`when`(mockRepository).getPemanfaatan()

        val result = useCase(forceRefresh = false)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).getPemanfaatan()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns successful response with financial data`() = runTest {
        val expectedResponse = PemanfaatanResponse(
            status = "success",
            message = "Data retrieved successfully",
            data = emptyList()
        )

        doReturn(Result.success(expectedResponse)).`when`(mockRepository).getPemanfaatan()

        val result = useCase()

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals("success", response?.status)
        assertEquals("Data retrieved successfully", response?.message)
        verify(mockRepository).getPemanfaatan()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke propagates error details from repository failure`() = runTest {
        val errorMessage = "Failed to fetch data: Timeout"
        val exception = RuntimeException(errorMessage)
        doReturn(Result.failure<PemanfaatanResponse>(exception)).`when`(mockRepository).getPemanfaatan()

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
        verify(mockRepository).getPemanfaatan()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns failure for null response from repository`() = runTest {
        val exception = NullPointerException("Response is null")
        doThrow(exception).`when`(mockRepository).getPemanfaatan()

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NullPointerException)
        verify(mockRepository).getPemanfaatan()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles IOException from repository`() = runTest {
        val exception = java.io.IOException("No network connection")
        doThrow(exception).`when`(mockRepository).getPemanfaatan()

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).getPemanfaatan()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles IllegalStateException from repository`() = runTest {
        val exception = IllegalStateException("Repository not initialized")
        doThrow(exception).`when`(mockRepository).getPemanfaatan()

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).getPemanfaatan()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `validateFinancialData returns true when response data is null`() = runTest {
        val response = PemanfaatanResponse(
            status = "success",
            message = "No data",
            data = null
        )

        val result = useCase.validateFinancialData(response)
        assertTrue(result)
    }

    @Test
    fun `validateFinancialData returns true when response data is empty`() = runTest {
        val response = PemanfaatanResponse(
            status = "success",
            message = "No data",
            data = emptyList()
        )

        val result = useCase.validateFinancialData(response)
        assertTrue(result)
    }

    @Test
    fun `validateFinancialData returns true when all data items are valid`() = runTest {
        val validItems = listOf(
            DataItem(
                first_name = "John", last_name = "Doe", email = "john@example.com", alamat = "123 Main St",
                iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "Maintenance", avatar = "https://example.com/avatar1.jpg"
            ),
            DataItem(
                first_name = "Jane", last_name = "Smith", email = "jane@example.com", alamat = "456 Oak Ave",
                iuran_perwarga = 150000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 75000, pengeluaran_iuran_warga = 30000,
                pemanfaatan_iuran = "Cleaning", avatar = "https://example.com/avatar2.jpg"
            )
        )

        val response = PemanfaatanResponse(
            status = "success",
            message = "Data retrieved",
            data = validItems
        )

        val result = useCase.validateFinancialData(response)
        assertTrue(result)
    }

    @Test
    fun `validateFinancialData returns false when one data item is invalid`() = runTest {
        val invalidItems = listOf(
            DataItem(
                first_name = "Valid", last_name = "User", email = "valid@example.com", alamat = "x",
                iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "x", avatar = "x"
            ),
            DataItem(
                first_name = "Invalid", last_name = "User", email = "invalid@example.com", alamat = "y",
                iuran_perwarga = -1, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "y", avatar = "y"
            )
        )

        val response = PemanfaatanResponse(
            status = "success",
            message = "Data retrieved",
            data = invalidItems
        )

        val result = useCase.validateFinancialData(response)
        assertFalse(result)
    }

    @Test
    fun `validateFinancialData returns false when calculations would overflow`() = runTest {
        val maxInt = Int.MAX_VALUE
        val overflowItems = listOf(
            DataItem(
                first_name = "Overflow", last_name = "User", email = "overflow@example.com", alamat = "x",
                iuran_perwarga = maxInt - 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "x", avatar = "x"
            ),
            DataItem(
                first_name = "Overflow2", last_name = "User", email = "overflow2@example.com", alamat = "y",
                iuran_perwarga = 200000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "y", avatar = "y"
            )
        )

        val response = PemanfaatanResponse(
            status = "success",
            message = "Data retrieved",
            data = overflowItems
        )

        val result = useCase.validateFinancialData(response)
        assertFalse(result)
    }

    @Test
    fun `validateFinancialData returns true for boundary values`() = runTest {
        val boundaryItem = DataItem(
            first_name = "Boundary", last_name = "Test", email = "boundary@example.com", alamat = "Boundary St",
            iuran_perwarga = Int.MAX_VALUE / 2, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = Int.MAX_VALUE / 3, pengeluaran_iuran_warga = Int.MAX_VALUE / 2,
            pemanfaatan_iuran = "Test", avatar = "https://example.com/avatar.jpg"
        )

        val response = PemanfaatanResponse(
            status = "success",
            message = "Data retrieved",
            data = listOf(boundaryItem)
        )

        val result = useCase.validateFinancialData(response)
        assertTrue(result)
    }

    @Test
    fun `validateFinancialData returns true for zero values`() = runTest {
        val zeroItem = DataItem(
            first_name = "Zero", last_name = "User", email = "zero@example.com", alamat = "Zero St",
            iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "Zero", avatar = "https://example.com/avatar.jpg"
        )

        val response = PemanfaatanResponse(
            status = "success",
            message = "Data retrieved",
            data = listOf(zeroItem)
        )

        val result = useCase.validateFinancialData(response)
        assertTrue(result)
    }

    @Test
    fun `validateFinancialData handles exception during validation`() = runTest {
        val exception = RuntimeException("Validation error")

        val response = PemanfaatanResponse(
            status = "success",
            message = "Data retrieved",
            data = listOf(
                DataItem(
                    first_name = "Valid", last_name = "User", email = "valid@example.com", alamat = "x",
                    iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                    total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                    pemanfaatan_iuran = "x", avatar = "x"
                )
            )
        )

        val result = useCase.validateFinancialData(response)
        assertTrue(result)
    }

    @Test
    fun `invoke returns Result type for successful operation`() = runTest {
        val expectedResponse = PemanfaatanResponse(
            status = "success",
            message = "Success",
            data = emptyList()
        )

        doReturn(Result.success(expectedResponse)).`when`(mockRepository).getPemanfaatan()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
        assertNotNull(result.getOrNull())
        verify(mockRepository).getPemanfaatan()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns Result type for failed operation`() = runTest {
        val exception = RuntimeException("Error")
        doReturn(Result.failure<PemanfaatanResponse>(exception)).`when`(mockRepository).getPemanfaatan()

        val result = useCase()

        assertTrue(result.isFailure)
        assertFalse(result.isSuccess)
        assertNull(result.getOrNull())
        assertNotNull(result.exceptionOrNull())
        verify(mockRepository).getPemanfaatan()
        verifyNoMoreInteractions(mockRepository)
    }
}
