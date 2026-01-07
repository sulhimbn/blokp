package com.example.iurankomplek.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.iurankomplek.data.dao.FinancialRecordDao
import com.example.iurankomplek.data.dao.UserDao
import com.example.iurankomplek.data.database.AppDatabase
import com.example.iurankomplek.data.database.Migration1
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class FinancialRecordDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var financialRecordDao: FinancialRecordDao
    private lateinit var userDao: UserDao

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private var testUserId: Long = 0

    @Before
    fun setup() = runTest {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .addMigrations(Migration1())
            .build()
        financialRecordDao = db.financialRecordDao()
        userDao = db.userDao()

        val user = UserEntity(
            email = "testuser@example.com",
            firstName = "Test",
            lastName = "User",
            alamat = "Test Address",
            avatar = "testavatar.jpg"
        )
        testUserId = userDao.insert(user)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndGetFinancialRecordById() = runTest {
        val record = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 50,
            totalIuranRekap = 250,
            pemanfaatanIuran = "Test Pemanfaatan"
        )
        val recordId = financialRecordDao.insert(record)

        val retrievedRecord = financialRecordDao.getFinancialRecordById(recordId)
        assertNotNull(retrievedRecord)
        assertEquals(recordId, retrievedRecord?.id)
        assertEquals(testUserId, retrievedRecord?.userId)
        assertEquals(100, retrievedRecord?.iuranPerwarga)
        assertEquals("Test Pemanfaatan", retrievedRecord?.pemanfaatanIuran)
    }

    @Test
    fun getAllFinancialRecordsReturnsEmptyInitially() = runTest {
        val records = financialRecordDao.getAllFinancialRecords().first()
        assertTrue(records.isEmpty())
    }

    @Test
    fun getAllFinancialRecordsReturnsAllInsertedRecords() = runTest {
        val record1 = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 50,
            totalIuranRekap = 250,
            pemanfaatanIuran = "Record 1"
        )
        val record2 = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 150,
            jumlahIuranBulanan = 250,
            totalIuranIndividu = 400,
            pengeluaranIuranWarga = 75,
            totalIuranRekap = 325,
            pemanfaatanIuran = "Record 2"
        )
        financialRecordDao.insert(record1)
        financialRecordDao.insert(record2)

        val records = financialRecordDao.getAllFinancialRecords().first()
        assertEquals(2, records.size)
    }

    @Test
    fun getFinancialRecordsByUserId() = runTest {
        val user2 = UserEntity(
            email = "user2@example.com",
            firstName = "User",
            lastName = "Two",
            alamat = "Address 2",
            avatar = "user2.jpg"
        )
        val userId2 = userDao.insert(user2)

        val record1 = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 50,
            totalIuranRekap = 250,
            pemanfaatanIuran = "User 1 Record"
        )
        val record2 = FinancialRecordEntity(
            userId = userId2,
            iuranPerwarga = 150,
            jumlahIuranBulanan = 250,
            totalIuranIndividu = 400,
            pengeluaranIuranWarga = 75,
            totalIuranRekap = 325,
            pemanfaatanIuran = "User 2 Record"
        )
        financialRecordDao.insert(record1)
        financialRecordDao.insert(record2)

        val user1Records = financialRecordDao.getFinancialRecordsByUserId(testUserId).first()
        assertEquals(1, user1Records.size)
        assertEquals(testUserId, user1Records[0].userId)
    }

    @Test
    fun getLatestFinancialRecordByUserId() = runTest {
        val oldRecord = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 50,
            totalIuranRekap = 250,
            pemanfaatanIuran = "Old Record"
        )
        Thread.sleep(10)
        val newRecord = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 150,
            jumlahIuranBulanan = 250,
            totalIuranIndividu = 400,
            pengeluaranIuranWarga = 75,
            totalIuranRekap = 325,
            pemanfaatanIuran = "New Record"
        )
        financialRecordDao.insert(oldRecord)
        financialRecordDao.insert(newRecord)

        val latestRecord = financialRecordDao.getLatestFinancialRecordByUserId(testUserId)
        assertNotNull(latestRecord)
        assertEquals("New Record", latestRecord?.pemanfaatanIuran)
    }

    @Test
    fun searchFinancialRecordsByPemanfaatan() = runTest {
        val record1 = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 50,
            totalIuranRekap = 250,
            pemanfaatanIuran = "Pemeliharaan Taman"
        )
        val record2 = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 150,
            jumlahIuranBulanan = 250,
            totalIuranIndividu = 400,
            pengeluaranIuranWarga = 75,
            totalIuranRekap = 325,
            pemanfaatanIuran = "Keamanan"
        )
        val record3 = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 200,
            jumlahIuranBulanan = 300,
            totalIuranIndividu = 500,
            pengeluaranIuranWarga = 100,
            totalIuranRekap = 400,
            pemanfaatanIuran = "Pemeliharaan Fasilitas"
        )
        financialRecordDao.insert(record1)
        financialRecordDao.insert(record2)
        financialRecordDao.insert(record3)

        val searchResults = financialRecordDao.searchFinancialRecordsByPemanfaatan("Pemeliharaan").first()
        assertEquals(2, searchResults.size)
    }

    @Test
    fun updateFinancialRecord() = runTest {
        val record = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 50,
            totalIuranRekap = 250,
            pemanfaatanIuran = "Original"
        )
        val recordId = financialRecordDao.insert(record)

        val updatedRecord = record.copy(
            id = recordId,
            iuranPerwarga = 200,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Updated"
        )
        financialRecordDao.update(updatedRecord)

        val retrievedRecord = financialRecordDao.getFinancialRecordById(recordId)
        assertEquals(200, retrievedRecord?.iuranPerwarga)
        assertEquals(500, retrievedRecord?.totalIuranRekap)
        assertEquals("Updated", retrievedRecord?.pemanfaatanIuran)
    }

    @Test
    fun deleteFinancialRecord() = runTest {
        val record = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 50,
            totalIuranRekap = 250,
            pemanfaatanIuran = "Delete Me"
        )
        val recordId = financialRecordDao.insert(record)
        financialRecordDao.delete(record)

        val retrievedRecord = financialRecordDao.getFinancialRecordById(recordId)
        assertNull(retrievedRecord)
    }

    @Test
    fun deleteById() = runTest {
        val record = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 50,
            totalIuranRekap = 250,
            pemanfaatanIuran = "Delete By Id"
        )
        val recordId = financialRecordDao.insert(record)
        financialRecordDao.deleteById(recordId)

        val retrievedRecord = financialRecordDao.getFinancialRecordById(recordId)
        assertNull(retrievedRecord)
    }

    @Test
    fun deleteByUserId() = runTest {
        val user2 = UserEntity(
            email = "user2@example.com",
            firstName = "User",
            lastName = "Two",
            alamat = "Address 2",
            avatar = "user2.jpg"
        )
        val userId2 = userDao.insert(user2)

        val record1 = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 50,
            totalIuranRekap = 250,
            pemanfaatanIuran = "User 1"
        )
        val record2 = FinancialRecordEntity(
            userId = userId2,
            iuranPerwarga = 150,
            jumlahIuranBulanan = 250,
            totalIuranIndividu = 400,
            pengeluaranIuranWarga = 75,
            totalIuranRekap = 325,
            pemanfaatanIuran = "User 2"
        )
        financialRecordDao.insert(record1)
        financialRecordDao.insert(record2)
        financialRecordDao.deleteByUserId(userId2)

        val allRecords = financialRecordDao.getAllFinancialRecords().first()
        assertEquals(1, allRecords.size)
        assertEquals(testUserId, allRecords[0].userId)
    }

    @Test
    fun deleteAll() = runTest {
        val record1 = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 50,
            totalIuranRekap = 250,
            pemanfaatanIuran = "Delete All 1"
        )
        val record2 = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 150,
            jumlahIuranBulanan = 250,
            totalIuranIndividu = 400,
            pengeluaranIuranWarga = 75,
            totalIuranRekap = 325,
            pemanfaatanIuran = "Delete All 2"
        )
        financialRecordDao.insert(record1)
        financialRecordDao.insert(record2)
        financialRecordDao.deleteAll()

        val allRecords = financialRecordDao.getAllFinancialRecords().first()
        assertTrue(allRecords.isEmpty())
    }

    @Test
    fun getCount() = runTest {
        assertEquals(0, financialRecordDao.getCount())

        val record1 = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 50,
            totalIuranRekap = 250,
            pemanfaatanIuran = "Count 1"
        )
        val record2 = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 150,
            jumlahIuranBulanan = 250,
            totalIuranIndividu = 400,
            pengeluaranIuranWarga = 75,
            totalIuranRekap = 325,
            pemanfaatanIuran = "Count 2"
        )
        financialRecordDao.insert(record1)
        financialRecordDao.insert(record2)

        assertEquals(2, financialRecordDao.getCount())
    }

    @Test
    fun getCountByUserId() = runTest {
        val user2 = UserEntity(
            email = "user2@example.com",
            firstName = "User",
            lastName = "Two",
            alamat = "Address 2",
            avatar = "user2.jpg"
        )
        val userId2 = userDao.insert(user2)

        val record1 = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 50,
            totalIuranRekap = 250,
            pemanfaatanIuran = "Count By User 1"
        )
        val record2 = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 150,
            jumlahIuranBulanan = 250,
            totalIuranIndividu = 400,
            pengeluaranIuranWarga = 75,
            totalIuranRekap = 325,
            pemanfaatanIuran = "Count By User 2"
        )
        val record3 = FinancialRecordEntity(
            userId = userId2,
            iuranPerwarga = 200,
            jumlahIuranBulanan = 300,
            totalIuranIndividu = 500,
            pengeluaranIuranWarga = 100,
            totalIuranRekap = 400,
            pemanfaatanIuran = "Count By User 3"
        )
        financialRecordDao.insert(record1)
        financialRecordDao.insert(record2)
        financialRecordDao.insert(record3)

        assertEquals(2, financialRecordDao.getCountByUserId(testUserId))
        assertEquals(1, financialRecordDao.getCountByUserId(userId2))
    }

    @Test
    fun getTotalRekapByUserId() = runTest {
        val record1 = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 50,
            totalIuranRekap = 250,
            pemanfaatanIuran = "Total 1"
        )
        val record2 = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 150,
            jumlahIuranBulanan = 250,
            totalIuranIndividu = 400,
            pengeluaranIuranWarga = 75,
            totalIuranRekap = 325,
            pemanfaatanIuran = "Total 2"
        )
        val record3 = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 200,
            jumlahIuranBulanan = 300,
            totalIuranIndividu = 500,
            pengeluaranIuranWarga = 100,
            totalIuranRekap = 400,
            pemanfaatanIuran = "Total 3"
        )
        financialRecordDao.insert(record1)
        financialRecordDao.insert(record2)
        financialRecordDao.insert(record3)

        val totalRekap = financialRecordDao.getTotalRekapByUserId(testUserId)
        assertEquals(975L, totalRekap)
    }

    @Test
    fun getFinancialRecordsUpdatedSince() = runTest {
        val oldRecord = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 50,
            totalIuranRekap = 250,
            pemanfaatanIuran = "Old"
        )
        Thread.sleep(10)
        val timestamp = System.currentTimeMillis()
        val newRecord = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 150,
            jumlahIuranBulanan = 250,
            totalIuranIndividu = 400,
            pengeluaranIuranWarga = 75,
            totalIuranRekap = 325,
            pemanfaatanIuran = "New"
        )
        financialRecordDao.insert(oldRecord)
        financialRecordDao.insert(newRecord)

        val recentRecords = financialRecordDao.getFinancialRecordsUpdatedSince(timestamp).first()
        assertEquals(1, recentRecords.size)
        assertEquals("New", recentRecords[0].pemanfaatanIuran)
    }

    @Test
    fun validationUserIdMustBePositive() {
        assertThrows(IllegalArgumentException::class.java) {
            FinancialRecordEntity(
                userId = 0,
                iuranPerwarga = 100,
                jumlahIuranBulanan = 200,
                totalIuranIndividu = 300,
                pengeluaranIuranWarga = 50,
                totalIuranRekap = 250,
                pemanfaatanIuran = "Valid"
            )
        }
    }

    @Test
    fun validationNumericFieldsCannotBeNegative() {
        assertThrows(IllegalArgumentException::class.java) {
            FinancialRecordEntity(
                userId = 1,
                iuranPerwarga = -100,
                jumlahIuranBulanan = 200,
                totalIuranIndividu = 300,
                pengeluaranIuranWarga = 50,
                totalIuranRekap = 250,
                pemanfaatanIuran = "Valid"
            )
        }
    }

    @Test
    fun validationPemanfaatanIuranCannotBeBlank() {
        assertThrows(IllegalArgumentException::class.java) {
            FinancialRecordEntity(
                userId = 1,
                iuranPerwarga = 100,
                jumlahIuranBulanan = 200,
                totalIuranIndividu = 300,
                pengeluaranIuranWarga = 50,
                totalIuranRekap = 250,
                pemanfaatanIuran = ""
            )
        }
    }

    @Test
    fun cascadeDeleteOnUser() = runTest {
        val record = FinancialRecordEntity(
            userId = testUserId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 50,
            totalIuranRekap = 250,
            pemanfaatanIuran = "Cascade Test"
        )
        financialRecordDao.insert(record)
        assertEquals(1, financialRecordDao.getCount())

        userDao.deleteById(testUserId)
        
        assertEquals(0, financialRecordDao.getCount())
    }
}
