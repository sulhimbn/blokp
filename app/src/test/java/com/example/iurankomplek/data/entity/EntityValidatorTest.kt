package com.example.iurankomplek.data.entity

import org.junit.Test
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EntityValidatorTest {
    
    @Test
    fun `validateUser should accept valid user`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
        
        val (isValid, error) = EntityValidator.validateUser(user)
        
        assertTrue(isValid)
        assertEquals(null, error)
    }
    
    @Test
    fun `validateUser should reject blank email`() {
        val user = UserEntity(
            id = 1,
            email = "",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
        
        val (isValid, error) = EntityValidator.validateUser(user)
        
        assertFalse(isValid)
        assertEquals("Email cannot be blank", error)
    }
    
    @Test
    fun `validateUser should reject invalid email format`() {
        val user = UserEntity(
            id = 1,
            email = "invalid-email",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
        
        val (isValid, error) = EntityValidator.validateUser(user)
        
        assertFalse(isValid)
        assertEquals("Email format is invalid", error)
    }
    
    @Test
    fun `validateUser should reject blank first name`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
        
        val (isValid, error) = EntityValidator.validateUser(user)
        
        assertFalse(isValid)
        assertEquals("First name cannot be blank", error)
    }
    
    @Test
    fun `validateUser should reject blank last name`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
        
        val (isValid, error) = EntityValidator.validateUser(user)
        
        assertFalse(isValid)
        assertEquals("Last name cannot be blank", error)
    }
    
    @Test
    fun `validateUser should reject blank alamat`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "",
            avatar = "https://example.com/avatar.jpg"
        )
        
        val (isValid, error) = EntityValidator.validateUser(user)
        
        assertFalse(isValid)
        assertEquals("Alamat cannot be blank", error)
    }
    
    @Test
    fun `validateUser should reject invalid avatar URL`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "invalid-url"
        )
        
        val (isValid, error) = EntityValidator.validateUser(user)
        
        assertFalse(isValid)
        assertEquals("Avatar URL is invalid", error)
    }
    
    @Test
    fun `validateUser should reject too long first name`() {
        val longName = "a".repeat(101)
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = longName,
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
        
        val (isValid, error) = EntityValidator.validateUser(user)
        
        assertFalse(isValid)
        assertEquals("First name exceeds maximum length (100)", error)
    }
    
    @Test
    fun `validateFinancialRecord should accept valid record`() {
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 50,
            totalIuranIndividu = 150,
            pengeluaranIuranWarga = 200,
            totalIuranRekap = 300,
            pemanfaatanIuran = "Maintenance"
        )
        
        val (isValid, error) = EntityValidator.validateFinancialRecord(record)
        
        assertTrue(isValid)
        assertEquals(null, error)
    }
    
    @Test
    fun `validateFinancialRecord should reject negative iuranPerwarga`() {
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = -1,
            jumlahIuranBulanan = 50,
            totalIuranIndividu = 150,
            pengeluaranIuranWarga = 200,
            totalIuranRekap = 300,
            pemanfaatanIuran = "Maintenance"
        )
        
        val (isValid, error) = EntityValidator.validateFinancialRecord(record)
        
        assertFalse(isValid)
        assertEquals("Iuran perwarga cannot be negative", error)
    }
    
    @Test
    fun `validateFinancialRecord should reject zero userId`() {
        val record = FinancialRecordEntity(
            id = 1,
            userId = 0,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 50,
            totalIuranIndividu = 150,
            pengeluaranIuranWarga = 200,
            totalIuranRekap = 300,
            pemanfaatanIuran = "Maintenance"
        )
        
        val (isValid, error) = EntityValidator.validateFinancialRecord(record)
        
        assertFalse(isValid)
        assertEquals("User ID must be positive", error)
    }
    
    @Test
    fun `validateFinancialRecord should reject blank pemanfaatanIuran`() {
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 50,
            totalIuranIndividu = 150,
            pengeluaranIuranWarga = 200,
            totalIuranRekap = 300,
            pemanfaatanIuran = ""
        )
        
        val (isValid, error) = EntityValidator.validateFinancialRecord(record)
        
        assertFalse(isValid)
        assertEquals("Pemanfaatan iuran cannot be blank", error)
    }
    
    @Test
    fun `validateFinancialRecord should accept zero values`() {
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 0,
            jumlahIuranBulanan = 0,
            totalIuranIndividu = 0,
            pengeluaranIuranWarga = 0,
            totalIuranRekap = 0,
            pemanfaatanIuran = "Initial record"
        )
        
        val (isValid, error) = EntityValidator.validateFinancialRecord(record)
        
        assertTrue(isValid)
        assertEquals(null, error)
    }
    
    @Test
    fun `validateUserWithFinancials should accept valid combination`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
        
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 50,
            totalIuranIndividu = 150,
            pengeluaranIuranWarga = 200,
            totalIuranRekap = 300,
            pemanfaatanIuran = "Maintenance"
        )
        
        val userWithFinancials = UserWithFinancialRecords(user, listOf(record))
        
        val (isValid, error) = EntityValidator.validateUserWithFinancials(userWithFinancials)
        
        assertTrue(isValid)
        assertEquals(null, error)
    }
    
    @Test
    fun `validateUserWithFinancials should reject empty financial records`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
        
        val userWithFinancials = UserWithFinancialRecords(user, emptyList())
        
        val (isValid, error) = EntityValidator.validateUserWithFinancials(userWithFinancials)
        
        assertFalse(isValid)
        assertEquals("User must have at least one financial record", error)
    }
    
    @Test
    fun `validateFinancialRecordOwnership should accept matching userId`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
        
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 50,
            totalIuranIndividu = 150,
            pengeluaranIuranWarga = 200,
            totalIuranRekap = 300,
            pemanfaatanIuran = "Maintenance"
        )
        
        val (isValid, error) = EntityValidator.validateFinancialRecordOwnership(user, listOf(record))
        
        assertTrue(isValid)
        assertEquals(null, error)
    }
    
    @Test
    fun `validateFinancialRecordOwnership should reject mismatching userId`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
        
        val record = FinancialRecordEntity(
            id = 1,
            userId = 2,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 50,
            totalIuranIndividu = 150,
            pengeluaranIuranWarga = 200,
            totalIuranRekap = 300,
            pemanfaatanIuran = "Maintenance"
        )
        
        val (isValid, error) = EntityValidator.validateFinancialRecordOwnership(user, listOf(record))
        
        assertFalse(isValid)
        assertEquals("Financial record ID 1 does not belong to user ID 1", error)
    }
    
    @Test
    fun `validateUserList should accept all valid users`() {
        val users = listOf(
            UserEntity(
                id = 1,
                email = "test1@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = "https://example.com/avatar1.jpg"
            ),
            UserEntity(
                id = 2,
                email = "test2@example.com",
                firstName = "Jane",
                lastName = "Smith",
                alamat = "456 Oak Ave",
                avatar = "https://example.com/avatar2.jpg"
            )
        )
        
        val (isValid, errors) = EntityValidator.validateUserList(users)
        
        assertTrue(isValid)
        assertTrue(errors.isEmpty())
    }
    
    @Test
    fun `validateUserList should reject list with invalid user`() {
        val users = listOf(
            UserEntity(
                id = 1,
                email = "test@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = "https://example.com/avatar.jpg"
            ),
            UserEntity(
                id = 2,
                email = "",
                firstName = "Jane",
                lastName = "Smith",
                alamat = "456 Oak Ave",
                avatar = "https://example.com/avatar2.jpg"
            )
        )
        
        val (isValid, errors) = EntityValidator.validateUserList(users)
        
        assertFalse(isValid)
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("User at index 1"))
    }
}
