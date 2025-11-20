package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.model.PemanfaatanResponse
import org.junit.Test
import org.junit.Assert.*

class DataModelTest {

    @Test
    fun `DataItem should be created with correct values`() {
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

        assertEquals("John", dataItem.first_name)
        assertEquals("Doe", dataItem.last_name)
        assertEquals("john.doe@example.com", dataItem.email)
        assertEquals("123 Main St", dataItem.alamat)
        assertEquals(100, dataItem.iuran_perwarga)
        assertEquals(500, dataItem.total_iuran_rekap)
        assertEquals(200, dataItem.jumlah_iuran_bulanan)
        assertEquals(150, dataItem.total_iuran_individu)
        assertEquals(50, dataItem.pengeluaran_iuran_warga)
        assertEquals("Maintenance", dataItem.pemanfaatan_iuran)
        assertEquals("https://example.com/avatar.jpg", dataItem.avatar)
    }

    @Test
    fun `DataItem should support equality comparison`() {
        val dataItem1 = DataItem(
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

        val dataItem2 = DataItem(
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

        assertEquals(dataItem1, dataItem2)
        assertEquals(dataItem1.hashCode(), dataItem2.hashCode())
    }

    @Test
    fun `DataItem should have different hash codes for different values`() {
        val dataItem1 = DataItem(
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

        val dataItem2 = DataItem(
            first_name = "Jane", // Different first name
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

        assertNotEquals(dataItem1, dataItem2)
    }

    @Test
    fun `UserResponse should be created with correct values`() {
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
        val userResponse = UserResponse(listOf(dataItem))

        assertEquals(1, userResponse.data.size)
        assertEquals("john.doe@example.com", userResponse.data[0].email)
    }

    @Test
    fun `PemanfaatanResponse should be created with correct values`() {
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
        val pemanfaatanResponse = PemanfaatanResponse(listOf(dataItem))

        assertEquals(1, pemanfaatanResponse.data.size)
        assertEquals("Maintenance", pemanfaatanResponse.data[0].pemanfaatan_iuran)
    }

    @Test
    fun `UserResponse should handle empty data list`() {
        val userResponse = UserResponse(emptyList())

        assertEquals(0, userResponse.data.size)
        assertTrue(userResponse.data.isEmpty())
    }

    @Test
    fun `PemanfaatanResponse should handle empty data list`() {
        val pemanfaatanResponse = PemanfaatanResponse(emptyList())

        assertEquals(0, pemanfaatanResponse.data.size)
        assertTrue(pemanfaatanResponse.data.isEmpty())
    }

    @Test
    fun `DataItem should handle zero and negative values correctly`() {
        val dataItemWithZero = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john.doe@example.com",
            alamat = "123 Main St",
            iuran_perwarga = 0, // Zero value
            total_iuran_rekap = 500,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 0, // Zero value
            pengeluaran_iuran_warga = 50,
            pemanfaatan_iuran = "Maintenance",
            avatar = "https://example.com/avatar.jpg"
        )

        assertEquals(0, dataItemWithZero.iuran_perwarga)
        assertEquals(0, dataItemWithZero.total_iuran_individu)
    }

    @Test
    fun `DataItem should handle empty string values correctly`() {
        val dataItemWithEmptyStrings = DataItem(
            first_name = "",
            last_name = "",
            email = "",
            alamat = "",
            iuran_perwarga = 100,
            total_iuran_rekap = 500,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 150,
            pengeluaran_iuran_warga = 50,
            pemanfaatan_iuran = "",
            avatar = ""
        )

        assertEquals("", dataItemWithEmptyStrings.first_name)
        assertEquals("", dataItemWithEmptyStrings.last_name)
        assertEquals("", dataItemWithEmptyStrings.email)
        assertEquals("", dataItemWithEmptyStrings.alamat)
        assertEquals("", dataItemWithEmptyStrings.pemanfaatan_iuran)
        assertEquals("", dataItemWithEmptyStrings.avatar)
    }
}