package com.example.iurankomplek.data.cache

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.iurankomplek.data.dao.FinancialRecordDao
import com.example.iurankomplek.data.dao.UserDao
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CacheManagerTest {
    
    private lateinit var context: Context
    private lateinit var userDao: UserDao
    private lateinit var financialRecordDao: FinancialRecordDao
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        
        val database = Room.inMemoryDatabaseBuilder(
            context,
            com.example.iurankomplek.data.database.AppDatabase::class.java
        ).build()
        
        userDao = database.userDao()
        financialRecordDao = database.financialRecordDao()
    }
    
    @After
    fun tearDown() {
        userDao.deleteAll()
        financialRecordDao.deleteAll()
    }
    
    @Test
    fun isCacheFresh_returnsTrueForFreshData() = runTest {
        val recentTimestamp = System.currentTimeMillis() - 2 * 60 * 1000L // 2 minutes ago
        assertTrue(CacheManager.isCacheFresh(recentTimestamp))
    }
    
    @Test
    fun isCacheFresh_returnsFalseForStaleData() = runTest {
        val oldTimestamp = System.currentTimeMillis() - 10 * 60 * 1000L // 10 minutes ago
        assertFalse(CacheManager.isCacheFresh(oldTimestamp))
    }
    
    @Test
    fun isCacheFresh_returnsFalseForBoundaryData() = runTest {
        val boundaryTimestamp = System.currentTimeMillis() - 5 * 60 * 1000L // exactly 5 minutes ago
        assertFalse(CacheManager.isCacheFresh(boundaryTimestamp))
    }
    
    @Test
    fun setCacheFreshnessThreshold_updatesThreshold() = runTest {
        val customThreshold = 10 * 60 * 1000L // 10 minutes
        CacheManager.setCacheFreshnessThreshold(customThreshold)
        
        assertEquals(customThreshold, CacheManager.getCacheFreshnessThreshold())
    }
    
    @Test
    fun customThreshold_respectsNewValue() = runTest {
        val customThreshold = 15 * 60 * 1000L // 15 minutes
        CacheManager.setCacheFreshnessThreshold(customThreshold)
        
        val recentTimestamp = System.currentTimeMillis() - 12 * 60 * 1000L // 12 minutes ago
        assertTrue(CacheManager.isCacheFresh(recentTimestamp))
    }
    
    @Test
    fun insertUser_savesSuccessfully() = runTest {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "Test Address",
            avatar = "http://example.com/avatar.jpg",
            createdAt = Date(),
            updatedAt = Date()
        )
        
        val userId = userDao.insert(user)
        assertTrue(userId > 0)
        
        val savedUser = userDao.getUserById(userId)
        assertNotNull(savedUser)
        assertEquals(user.email, savedUser?.email)
        assertEquals(user.firstName, savedUser?.firstName)
    }
    
    @Test
    fun insertFinancialRecord_savesSuccessfully() = runTest {
        val userId = userDao.insert(
            UserEntity(
                email = "test@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "Test Address",
                avatar = "http://example.com/avatar.jpg",
                createdAt = Date(),
                updatedAt = Date()
            )
        )
        
        val financialRecord = FinancialRecordEntity(
            userId = userId,
            iuranPerwarga = 100000,
            jumlahIuranBulanan = 50000,
            totalIuranIndividu = 150000,
            pengeluaranIuranWarga = 50000,
            totalIuranRekap = 450000,
            pemanfaatanIuran = "Test pemanfaatan",
            createdAt = Date(),
            updatedAt = Date()
        )
        
        val recordId = financialRecordDao.insert(financialRecord)
        assertTrue(recordId > 0)
        
        val savedRecord = financialRecordDao.getFinancialRecordById(recordId)
        assertNotNull(savedRecord)
        assertEquals(userId, savedRecord?.userId)
        assertEquals(financialRecord.pemanfaatanIuran, savedRecord?.pemanfaatanIuran)
    }
    
    @Test
    fun getUserByEmail_retrievesCorrectUser() = runTest {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "Test Address",
            avatar = "http://example.com/avatar.jpg",
            createdAt = Date(),
            updatedAt = Date()
        )
        
        userDao.insert(user)
        
        val retrievedUser = userDao.getUserByEmail("test@example.com")
        assertNotNull(retrievedUser)
        assertEquals(user.email, retrievedUser?.email)
    }
    
    @Test
    fun emailExists_returnsTrueForExistingEmail() = runTest {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "Test Address",
            avatar = "http://example.com/avatar.jpg",
            createdAt = Date(),
            updatedAt = Date()
        )
        
        userDao.insert(user)
        assertTrue(userDao.emailExists("test@example.com"))
    }
    
    @Test
    fun emailExists_returnsFalseForNonExistingEmail() = runTest {
        assertFalse(userDao.emailExists("nonexistent@example.com"))
    }
    
    @Test
    fun getFinancialRecordsByUserId_returnsCorrectRecords() = runTest {
        val userId = userDao.insert(
            UserEntity(
                email = "test@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "Test Address",
                avatar = "http://example.com/avatar.jpg",
                createdAt = Date(),
                updatedAt = Date()
            )
        )
        
        val record1 = FinancialRecordEntity(
            userId = userId,
            iuranPerwarga = 100000,
            jumlahIuranBulanan = 50000,
            totalIuranIndividu = 150000,
            pengeluaranIuranWarga = 50000,
            totalIuranRekap = 450000,
            pemanfaatanIuran = "Test 1",
            createdAt = Date(),
            updatedAt = Date()
        )
        
        val record2 = FinancialRecordEntity(
            userId = userId,
            iuranPerwarga = 200000,
            jumlahIuranBulanan = 100000,
            totalIuranIndividu = 300000,
            pengeluaranIuranWarga = 100000,
            totalIuranRekap = 900000,
            pemanfaatanIuran = "Test 2",
            createdAt = Date(),
            updatedAt = Date()
        )
        
        financialRecordDao.insert(record1)
        financialRecordDao.insert(record2)
        
        val records = financialRecordDao.getFinancialRecordsByUserId(userId)
        assertEquals(2, records.size)
    }
    
    @Test
    fun searchFinancialRecordsByPemanfaatan_returnsMatchingRecords() = runTest {
        val userId = userDao.insert(
            UserEntity(
                email = "test@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "Test Address",
                avatar = "http://example.com/avatar.jpg",
                createdAt = Date(),
                updatedAt = Date()
            )
        )
        
        val record1 = FinancialRecordEntity(
            userId = userId,
            iuranPerwarga = 100000,
            jumlahIuranBulanan = 50000,
            totalIuranIndividu = 150000,
            pengeluaranIuranWarga = 50000,
            totalIuranRekap = 450000,
            pemanfaatanIuran = "Maintenance fasilitas umum",
            createdAt = Date(),
            updatedAt = Date()
        )
        
        val record2 = FinancialRecordEntity(
            userId = userId,
            iuranPerwarga = 200000,
            jumlahIuranBulanan = 100000,
            totalIuranIndividu = 300000,
            pengeluaranIuranWarga = 100000,
            totalIuranRekap = 900000,
            pemanfaatanIuran = "Perbaikan jalan",
            createdAt = Date(),
            updatedAt = Date()
        )
        
        financialRecordDao.insert(record1)
        financialRecordDao.insert(record2)
        
        val records = financialRecordDao.searchFinancialRecordsByPemanfaatan("fasilitas")
        assertEquals(1, records.size)
        assertEquals("Maintenance fasilitas umum", records[0].pemanfaatanIuran)
    }
    
    @Test
    fun getTotalRekapByUserId_calculatesCorrectSum() = runTest {
        val userId = userDao.insert(
            UserEntity(
                email = "test@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "Test Address",
                avatar = "http://example.com/avatar.jpg",
                createdAt = Date(),
                updatedAt = Date()
            )
        )
        
        val record1 = FinancialRecordEntity(
            userId = userId,
            iuranPerwarga = 100000,
            jumlahIuranBulanan = 50000,
            totalIuranIndividu = 150000,
            pengeluaranIuranWarga = 50000,
            totalIuranRekap = 450000,
            pemanfaatanIuran = "Test 1",
            createdAt = Date(),
            updatedAt = Date()
        )
        
        val record2 = FinancialRecordEntity(
            userId = userId,
            iuranPerwarga = 200000,
            jumlahIuranBulanan = 100000,
            totalIuranIndividu = 300000,
            pengeluaranIuranWarga = 100000,
            totalIuranRekap = 900000,
            pemanfaatanIuran = "Test 2",
            createdAt = Date(),
            updatedAt = Date()
        )
        
        financialRecordDao.insert(record1)
        financialRecordDao.insert(record2)
        
        val totalRekap = financialRecordDao.getTotalRekapByUserId(userId)
        assertEquals(1350000L, totalRekap)
    }
}
