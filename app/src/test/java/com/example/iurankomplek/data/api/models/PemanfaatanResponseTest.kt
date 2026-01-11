package com.example.iurankomplek.data.api.models

import com.example.iurankomplek.data.dto.LegacyDataItemDto
import org.junit.Assert.*
import org.junit.Test

class PemanfaatanResponseTest {

    @Test
    fun `PemanfaatanResponse should contain data list`() {
        val data = listOf(
            LegacyDataItemDto(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "123 Main St",
                iuran_perwarga = 100000,
                total_iuran_rekap = 300000,
                jumlah_iuran_bulanan = 3,
                total_iuran_individu = 100000,
                pengeluaran_iuran_warga = 50000,
                pemanfaatan_iuran = "Maintenance",
                avatar = "https://example.com/avatar1.jpg"
            ),
            LegacyDataItemDto(
                first_name = "Jane",
                last_name = "Smith",
                email = "jane@example.com",
                alamat = "456 Oak Ave",
                iuran_perwarga = 150000,
                total_iuran_rekap = 450000,
                jumlah_iuran_bulanan = 3,
                total_iuran_individu = 150000,
                pengeluaran_iuran_warga = 75000,
                pemanfaatan_iuran = "Utilities",
                avatar = "https://example.com/avatar2.jpg"
            )
        )

        val response = PemanfaatanResponse(data)

        assertEquals(2, response.data.size)
        assertEquals("John", response.data[0].first_name)
        assertEquals("Smith", response.data[1].last_name)
    }

    @Test
    fun `PemanfaatanResponse should handle empty list`() {
        val response = PemanfaatanResponse(emptyList())

        assertTrue(response.data.isEmpty())
    }

    @Test
    fun `PemanfaatanResponse should handle single item`() {
        val data = listOf(
            LegacyDataItemDto(
                first_name = "Bob",
                last_name = "Wilson",
                email = "bob@example.com",
                alamat = "789 Pine Rd",
                iuran_perwarga = 200000,
                total_iuran_rekap = 600000,
                jumlah_iuran_bulanan = 3,
                total_iuran_individu = 200000,
                pengeluaran_iuran_warga = 100000,
                pemanfaatan_iuran = "Repairs",
                avatar = "https://example.com/avatar3.jpg"
            )
        )

        val response = PemanfaatanResponse(data)

        assertEquals(1, response.data.size)
        assertEquals("Bob", response.data[0].first_name)
    }
}

class UserResponseTest {

    @Test
    fun `UserResponse should contain data list`() {
        val data = listOf(
            LegacyDataItemDto(
                first_name = "Alice",
                last_name = "Johnson",
                email = "alice@example.com",
                alamat = "321 Elm St",
                iuran_perwarga = 120000,
                total_iuran_rekap = 360000,
                jumlah_iuran_bulanan = 3,
                total_iuran_individu = 120000,
                pengeluaran_iuran_warga = 60000,
                pemanfaatan_iuran = "General",
                avatar = "https://example.com/avatar4.jpg"
            ),
            LegacyDataItemDto(
                first_name = "Charlie",
                last_name = "Brown",
                email = "charlie@example.com",
                alamat = "654 Maple Dr",
                iuran_perwarga = 180000,
                total_iuran_rekap = 540000,
                jumlah_iuran_bulanan = 3,
                total_iuran_individu = 180000,
                pengeluaran_iuran_warga = 90000,
                pemanfaatan_iuran = "Special",
                avatar = "https://example.com/avatar5.jpg"
            )
        )

        val response = UserResponse(data)

        assertEquals(2, response.data.size)
        assertEquals("Alice", response.data[0].first_name)
        assertEquals("Brown", response.data[1].last_name)
    }

    @Test
    fun `UserResponse should handle empty list`() {
        val response = UserResponse(emptyList())

        assertTrue(response.data.isEmpty())
    }

    @Test
    fun `UserResponse should handle single item`() {
        val data = listOf(
            LegacyDataItemDto(
                first_name = "David",
                last_name = "Lee",
                email = "david@example.com",
                alamat = "987 Cedar Ln",
                iuran_perwarga = 250000,
                total_iuran_rekap = 750000,
                jumlah_iuran_bulanan = 3,
                total_iuran_individu = 250000,
                pengeluaran_iuran_warga = 125000,
                pemanfaatan_iuran = "Emergency",
                avatar = "https://example.com/avatar6.jpg"
            )
        )

        val response = UserResponse(data)

        assertEquals(1, response.data.size)
        assertEquals("David", response.data[0].first_name)
    }
}
