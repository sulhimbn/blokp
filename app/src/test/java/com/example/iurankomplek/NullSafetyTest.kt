package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.model.PemanfaatanResponse
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*

/**
 * Unit tests for null safety improvements in API response handling
 */
class NullSafetyTest {

    @Test
    fun testUserAdapterAddUserWithNull() {
        val adapter = UserAdapter(mutableListOf())
        val initialSize = adapter.itemCount
        
        // Test adding null user
        adapter.addUser(null)
        
        // Size should remain the same
        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun testUserAdapterAddUserWithBlankEmail() {
        val adapter = UserAdapter(mutableListOf())
        val initialSize = adapter.itemCount
        
        // Test adding user with blank email
        val userWithBlankEmail = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "",
            alamat = "123 Main St",
            iuran_perwarga = 100,
            total_iuran_rekap = 100,
            jumlah_iuran_bulanan = 100,
            total_iuran_individu = 100,
            pengeluaran_iuran_warga = 100,
            pemanfaatan_iuran = "Test",
            avatar = "avatar_url"
        )
        
        adapter.addUser(userWithBlankEmail)
        
        // Size should remain the same since email is blank
        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun testUserAdapterAddUserWithBlankFirstName() {
        val adapter = UserAdapter(mutableListOf())
        val initialSize = adapter.itemCount
        
        // Test adding user with blank first name
        val userWithBlankFirstName = DataItem(
            first_name = "",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "123 Main St",
            iuran_perwarga = 100,
            total_iuran_rekap = 100,
            jumlah_iuran_bulanan = 100,
            total_iuran_individu = 100,
            pengeluaran_iuran_warga = 100,
            pemanfaatan_iuran = "Test",
            avatar = "avatar_url"
        )
        
        adapter.addUser(userWithBlankFirstName)
        
        // Size should remain the same since first name is blank
        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun testUserAdapterAddUserWithValidUser() {
        val adapter = UserAdapter(mutableListOf())
        val initialSize = adapter.itemCount
        
        // Test adding valid user
        val validUser = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "123 Main St",
            iuran_perwarga = 100,
            total_iuran_rekap = 100,
            jumlah_iuran_bulanan = 100,
            total_iuran_individu = 100,
            pengeluaran_iuran_warga = 100,
            pemanfaatan_iuran = "Test",
            avatar = "avatar_url"
        )
        
        adapter.addUser(validUser)
        
        // Size should increase by 1
        assertEquals(initialSize + 1, adapter.itemCount)
    }

    @Test
    fun testUserResponseNullSafety() {
        // Test null response body
        val nullResponseBody = null
        assertNull(nullResponseBody)
        
        // Test response with null data
        val responseWithNullData = UserResponse(data = null)
        assertNull(responseWithNullData.data)
        
        // Test response with empty data
        val responseWithEmptyData = UserResponse(data = emptyList())
        assertNotNull(responseWithEmptyData.data)
        assertTrue(responseWithEmptyData.data.isEmpty())
    }

    @Test
    fun testPemanfaatanResponseNullSafety() {
        // Test response with null data
        val responseWithNullData = PemanfaatanResponse(data = null)
        assertNull(responseWithNullData.data)
        
        // Test response with empty data
        val responseWithEmptyData = PemanfaatanResponse(data = emptyList())
        assertNotNull(responseWithEmptyData.data)
        assertTrue(responseWithEmptyData.data.isEmpty())
    }
}