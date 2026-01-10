package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.dto.LegacyDataItemDto
import com.example.iurankomplek.data.api.models.PemanfaatanResponse
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class LoadFinancialDataUseCaseEdgeCaseTest {

    @Mock
    private lateinit var mockRepository: PemanfaatanRepository

    private lateinit var validateFinancialDataUseCase: ValidateFinancialDataUseCase
    private lateinit var useCase: LoadFinancialDataUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        validateFinancialDataUseCase = ValidateFinancialDataUseCase()
        useCase = LoadFinancialDataUseCase(
            mockRepository,
            validateFinancialDataUseCase
        )
    }

    @Test
    fun `invoke should handle empty response successfully`() = runTest {
        val emptyResponse = PemanfaatanResponse(
            success = true,
            data = emptyList()
        )

        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.success(emptyResponse))

        val result = useCase(false)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response?.data?.isEmpty() == true)
    }

    @Test
    fun `invoke should handle single item response`() = runTest {
        val singleItem = listOf(
            LegacyDataItemDto(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test",
                iuran_perwarga = 100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val response = PemanfaatanResponse(
            success = true,
            data = singleItem
        )

        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.success(response))

        val result = useCase(false)

        assertTrue(result.isSuccess)
        val data = result.getOrNull()?.data
        assertEquals(1, data?.size)
        assertEquals("John", data?.get(0)?.first_name)
    }

    @Test
    fun `invoke should handle large item list`() = runTest {
        val largeItemList = (1..100).map { index ->
            LegacyDataItemDto(
                first_name = "User$index",
                last_name = "Test",
                email = "user$index@example.com",
                alamat = "Jl. Test $index",
                iuran_perwarga = 1000,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 500,
                pengeluaran_iuran_warga = 200,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        }

        val response = PemanfaatanResponse(
            success = true,
            data = largeItemList
        )

        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.success(response))

        val result = useCase(false)

        assertTrue(result.isSuccess)
        val data = result.getOrNull()?.data
        assertEquals(100, data?.size)
    }

    @Test
    fun `invoke should handle item with zero values`() = runTest {
        val zeroValueItem = listOf(
            LegacyDataItemDto(
                first_name = "Zero",
                last_name = "Values",
                email = "zero@example.com",
                alamat = "Jl. Test",
                iuran_perwarga = 0,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 0,
                pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "Zero expense",
                avatar = ""
            )
        )

        val response = PemanfaatanResponse(
            success = true,
            data = zeroValueItem
        )

        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.success(response))

        val result = useCase(false)

        assertTrue(result.isSuccess)
        val data = result.getOrNull()?.data
        assertEquals(1, data?.size)
        assertEquals(0, data?.get(0)?.iuran_perwarga)
    }

    @Test
    fun `invoke should handle item with boundary values`() = runTest {
        val boundaryItem = listOf(
            LegacyDataItemDto(
                first_name = "Boundary",
                last_name = "Values",
                email = "boundary@example.com",
                alamat = "Jl. Test",
                iuran_perwarga = Int.MAX_VALUE / 2,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = Int.MAX_VALUE / 3,
                pengeluaran_iuran_warga = Int.MAX_VALUE / 2,
                pemanfaatan_iuran = "Boundary expense",
                avatar = ""
            )
        )

        val response = PemanfaatanResponse(
            success = true,
            data = boundaryItem
        )

        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.success(response))

        val result = useCase(false)

        assertTrue(result.isSuccess)
        val data = result.getOrNull()?.data
        assertEquals(1, data?.size)
        assertEquals(Int.MAX_VALUE / 2, data?.get(0)?.iuran_perwarga)
    }

    @Test
    fun `invoke should handle item with very long strings`() = runTest {
        val longString = "A".repeat(100)

        val longStringItem = listOf(
            LegacyDataItemDto(
                first_name = longString,
                last_name = longString,
                email = "verylongemailaddress@verylongdomainname.com",
                alamat = longString + " " + longString,
                iuran_perwarga = 100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = longString,
                avatar = ""
            )
        )

        val response = PemanfaatanResponse(
            success = true,
            data = longStringItem
        )

        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.success(response))

        val result = useCase(false)

        assertTrue(result.isSuccess)
        val data = result.getOrNull()?.data
        assertEquals(1, data?.size)
        assertEquals(longString, data?.get(0)?.first_name)
    }

    @Test
    fun `invoke should handle item with special characters in name`() = runTest {
        val specialCharItem = listOf(
            LegacyDataItemDto(
                first_name = "José María",
                last_name = "O'Connor",
                email = "jose@example.com",
                alamat = "Jl. Äßçd",
                iuran_perwarga = 100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Åßçéñt",
                avatar = ""
            )
        )

        val response = PemanfaatanResponse(
            success = true,
            data = specialCharItem
        )

        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.success(response))

        val result = useCase(false)

        assertTrue(result.isSuccess)
        val data = result.getOrNull()?.data
        assertEquals(1, data?.size)
        assertEquals("José María", data?.get(0)?.first_name)
    }

    @Test
    fun `invoke should handle items with mixed valid and invalid data`() = runTest {
        val mixedItems = listOf(
            LegacyDataItemDto(
                first_name = "Valid",
                last_name = "User",
                email = "valid@example.com",
                alamat = "Jl. Test",
                iuran_perwarga = 100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            ),
            LegacyDataItemDto(
                first_name = "Invalid",
                last_name = "User",
                email = "invalid@example.com",
                alamat = "Jl. Test",
                iuran_perwarga = -100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val response = PemanfaatanResponse(
            success = true,
            data = mixedItems
        )

        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.success(response))

        val result = useCase(false)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke should handle repository error`() = runTest {
        val exception = Exception("Network error")
        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.failure(exception))

        val result = useCase(false)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertEquals(exception, error)
    }

    @Test
    fun `invoke should pass forceRefresh to repository`() = runTest {
        val response = PemanfaatanResponse(
            success = true,
            data = emptyList()
        )

        whenever(mockRepository.getPemanfaatan(true)).thenReturn(Result.success(response))

        val result = useCase(true)

        assertTrue(result.isSuccess)
        verify(mockRepository).getPemanfaatan(true)
    }

    @Test
    fun `invoke should not force refresh when flag is false`() = runTest {
        val response = PemanfaatanResponse(
            success = true,
            data = emptyList()
        )

        whenever(mockRepository.getPemanfaatan(false)).thenReturn(Result.success(response))

        val result = useCase(false)

        assertTrue(result.isSuccess)
        verify(mockRepository).getPemanfaatan(false)
    }

    @Test
    fun `invoke should handle response with null data`() = runTest {
        val response = PemanfaatanResponse(
            success = true,
            data = null
        )

        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.success(response))

        val result = useCase(false)

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull()?.data)
    }

    @Test
    fun `invoke should handle items with decimal financial values`() = runTest {
        val decimalItems = listOf(
            LegacyDataItemDto(
                first_name = "Decimal",
                last_name = "User",
                email = "decimal@example.com",
                alamat = "Jl. Test",
                iuran_perwarga = 100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Decimal expense",
                avatar = ""
            )
        )

        val response = PemanfaatanResponse(
            success = true,
            data = decimalItems
        )

        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.success(response))

        val result = useCase(false)

        assertTrue(result.isSuccess)
        val data = result.getOrNull()?.data
        assertEquals(1, data?.size)
    }

    @Test
    fun `invoke should handle duplicate items`() = runTest {
        val duplicateItem = LegacyDataItemDto(
            first_name = "Duplicate",
            last_name = "User",
            email = "duplicate@example.com",
            alamat = "Jl. Test",
            iuran_perwarga = 100,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 0,
            total_iuran_individu = 50,
            pengeluaran_iuran_warga = 25,
            pemanfaatan_iuran = "Test",
            avatar = ""
        )

        val duplicateItems = listOf(duplicateItem, duplicateItem)

        val response = PemanfaatanResponse(
            success = true,
            data = duplicateItems
        )

        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.success(response))

        val result = useCase(false)

        assertTrue(result.isSuccess)
        val data = result.getOrNull()?.data
        assertEquals(2, data?.size)
    }

    @Test
    fun `invoke should validate data after loading`() = runTest {
        val validItem = listOf(
            LegacyDataItemDto(
                first_name = "Valid",
                last_name = "User",
                email = "valid@example.com",
                alamat = "Jl. Test",
                iuran_perwarga = 100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val response = PemanfaatanResponse(
            success = true,
            data = validItem
        )

        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.success(response))

        val result = useCase(false)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke should handle IOException from repository`() = runTest {
        val exception = java.io.IOException("Network error")
        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.failure(exception))

        val result = useCase(false)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is java.io.IOException)
    }

    @Test
    fun `invoke should handle RuntimeException from repository`() = runTest {
        val exception = RuntimeException("Unexpected error")
        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.failure(exception))

        val result = useCase(false)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is RuntimeException)
    }

    @Test
    fun `invoke should handle items with maximum integer values`() = runTest {
        val maxIntegerItem = listOf(
            LegacyDataItemDto(
                first_name = "Max",
                last_name = "Integer",
                email = "max@example.com",
                alamat = "Jl. Test",
                iuran_perwarga = Int.MAX_VALUE / 2 - 1,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = Int.MAX_VALUE / 3 - 1,
                pengeluaran_iuran_warga = Int.MAX_VALUE / 2 - 1,
                pemanfaatan_iuran = "Max expense",
                avatar = ""
            )
        )

        val response = PemanfaatanResponse(
            success = true,
            data = maxIntegerItem
        )

        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.success(response))

        val result = useCase(false)

        assertTrue(result.isSuccess)
        val data = result.getOrNull()?.data
        assertEquals(1, data?.size)
    }

    @Test
    fun `invoke should handle items with very small positive values`() = runTest {
        val smallValueItem = listOf(
            LegacyDataItemDto(
                first_name = "Small",
                last_name = "Values",
                email = "small@example.com",
                alamat = "Jl. Test",
                iuran_perwarga = 1,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 1,
                pengeluaran_iuran_warga = 1,
                pemanfaatan_iuran = "Small expense",
                avatar = ""
            )
        )

        val response = PemanfaatanResponse(
            success = true,
            data = smallValueItem
        )

        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.success(response))

        val result = useCase(false)

        assertTrue(result.isSuccess)
        val data = result.getOrNull()?.data
        assertEquals(1, data?.size)
        assertEquals(1, data?.get(0)?.iuran_perwarga)
    }

    @Test
    fun `invoke should handle response with success false`() = runTest {
        val response = PemanfaatanResponse(
            success = false,
            data = emptyList()
        )

        whenever(mockRepository.getPemanfaatan(anyBoolean())).thenReturn(Result.success(response))

        val result = useCase(false)

        assertTrue(result.isSuccess)
        val resultData = result.getOrNull()
        assertNotNull(resultData)
    }
}
