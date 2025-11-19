package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for null safety checks in API response handling
 */
class NullSafetyTest {

    @Test
    fun testUserAdapterAddUser_withNullInput() {
        val adapter = UserAdapter(mutableListOf())
        val initialSize = adapter.itemCount
        
        // Test adding null user
        adapter.addUser(null)
        
        // Size should remain unchanged
        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun testUserAdapterAddUser_withBlankEmail() {
        val adapter = UserAdapter(mutableListOf())
        val initialSize = adapter.itemCount
        
        // Test adding user with blank email
        val userWithBlankEmail = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "",
            alamat = "123 Main St",
            iuran_perwarga = 100,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 0,
            total_iuran_individu = 0,
            pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "",
            avatar = ""
        )
        
        adapter.addUser(userWithBlankEmail)
        
        // Size should remain unchanged because email is blank
        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun testUserAdapterAddUser_withBlankFirstName() {
        val adapter = UserAdapter(mutableListOf())
        val initialSize = adapter.itemCount
        
        // Test adding user with blank first name
        val userWithBlankFirstName = DataItem(
            first_name = "",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "123 Main St",
            iuran_perwarga = 100,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 0,
            total_iuran_individu = 0,
            pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "",
            avatar = ""
        )
        
        adapter.addUser(userWithBlankFirstName)
        
        // Size should remain unchanged because first name is blank
        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun testUserAdapterAddUser_withValidUser() {
        val adapter = UserAdapter(mutableListOf())
        val initialSize = adapter.itemCount
        
        // Test adding valid user
        val validUser = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "123 Main St",
            iuran_perwarga = 100,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 0,
            total_iuran_individu = 0,
            pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "",
            avatar = ""
        )
        
        adapter.addUser(validUser)
        
        // Size should increase by 1
        assertEquals(initialSize + 1, adapter.itemCount)
    }

    @Test
    fun testUserAdapterAddUser_withWhitespaceEmail() {
        val adapter = UserAdapter(mutableListOf())
        val initialSize = adapter.itemCount
        
        // Test adding user with whitespace-only email
        val userWithWhitespaceEmail = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "   ",
            alamat = "123 Main St",
            iuran_perwarga = 100,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 0,
            total_iuran_individu = 0,
            pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "",
            avatar = ""
        )
        
        adapter.addUser(userWithWhitespaceEmail)
        
        // Size should remain unchanged because email is blank (whitespace-only)
        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun testUserAdapterAddUser_withWhitespaceFirstName() {
        val adapter = UserAdapter(mutableListOf())
        val initialSize = adapter.itemCount
        
        // Test adding user with whitespace-only first name
        val userWithWhitespaceFirstName = DataItem(
            first_name = "   ",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "123 Main St",
            iuran_perwarga = 100,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 0,
            total_iuran_individu = 0,
            pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "",
            avatar = ""
        )
        
        adapter.addUser(userWithWhitespaceFirstName)
        
        // Size should remain unchanged because first name is blank (whitespace-only)
        assertEquals(initialSize, adapter.itemCount)
    }
}