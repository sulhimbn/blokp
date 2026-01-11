package com.example.iurankomplek.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.iurankomplek.data.database.AppDatabase
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class FinancialRecordDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var financialRecordDao: FinancialRecordDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        userDao = database.userDao()
        financialRecordDao = database.financialRecordDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun createTestUser(userId: Long = 1): Long {
        val user = UserEntity(
            id = userId,
            email = "test$userId@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
        return userDao.insert(user)
    }

    private fun createTestRecord(userId: Long = 1, id: Long = 1): FinancialRecordEntity {
        return FinancialRecordEntity(
            id = id,
            userId = userId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )
    }

    @Test
    fun `insert financial record should return generated id`() = runTest {
        createTestUser()
        val record = createTestRecord()

        val id = financialRecordDao.insert(record)

        assertTrue(id > 0)
    }

    @Test
    fun `insert financial record should persist to database`() = runTest {
        createTestUser()
        val record = createTestRecord()

        financialRecordDao.insert(record)

        val allRecords = financialRecordDao.getAllFinancialRecords().first()
        assertEquals(1, allRecords.size)
        assertEquals("Maintenance", allRecords[0].pemanfaatanIuran)
    }

    @Test
    fun `insert multiple records should return list of ids`() = runTest {
        createTestUser()
        val records = listOf(
            createTestRecord(1, 1).copy(pemanfaatanIuran = "Maintenance"),
            createTestRecord(1, 2).copy(pemanfaatanIuran = "Repair")
        )

        val ids = financialRecordDao.insertAll(records)

        assertEquals(2, ids.size)
        assertTrue(ids.all { it > 0 })
    }

    @Test
    fun `getFinancialRecordById should return correct record`() = runTest {
        createTestUser()
        val record = createTestRecord()

        val id = financialRecordDao.insert(record)

        val retrieved = financialRecordDao.getFinancialRecordById(id)

        assertNotNull(retrieved)
        assertEquals(id, retrieved?.id)
        assertEquals("Maintenance", retrieved?.pemanfaatanIuran)
    }

    @Test
    fun `getFinancialRecordById with non-existent id should return null`() = runTest {
        val retrieved = financialRecordDao.getFinancialRecordById(999)

        assertNull(retrieved)
    }

    @Test
    fun `getFinancialRecordsByUserId should return records for specific user`() = runTest {
        val userId1 = createTestUser()
        val userId2 = createTestUser(2)

        val record1 = createTestRecord(userId1, 1).copy(pemanfaatanIuran = "Maintenance")
        val record2 = createTestRecord(userId1, 2).copy(pemanfaatanIuran = "Repair")
        val record3 = createTestRecord(userId2, 3).copy(pemanfaatanIuran = "Cleaning")

        financialRecordDao.insertAll(listOf(record1, record2, record3))

        val user1Records = financialRecordDao.getFinancialRecordsByUserId(userId1).first()
        val user2Records = financialRecordDao.getFinancialRecordsByUserId(userId2).first()

        assertEquals(2, user1Records.size)
        assertEquals(1, user2Records.size)
        assertTrue(user1Records.all { it.userId == userId1 })
        assertTrue(user2Records.all { it.userId == userId2 })
    }

    @Test
    fun `getFinancialRecordsByUserId should return empty flow when no records exist`() = runTest {
        val userId = createTestUser()

        val records = financialRecordDao.getFinancialRecordsByUserId(userId).first()

        assertTrue(records.isEmpty())
    }

    @Test
    fun `getLatestFinancialRecordByUserId should return most recent record`() = runTest {
        val userId = createTestUser()

        val now = Date()
        val earlier = Date(now.time - 1000000)
        val evenEarlier = Date(now.time - 2000000)

        val record1 = createTestRecord(userId, 1).copy(
            pemanfaatanIuran = "Old Record",
            updatedAt = evenEarlier
        )
        val record2 = createTestRecord(userId, 2).copy(
            pemanfaatanIuran = "Middle Record",
            updatedAt = earlier
        )
        val record3 = createTestRecord(userId, 3).copy(
            pemanfaatanIuran = "Newest Record",
            updatedAt = now
        )

        financialRecordDao.insertAll(listOf(record1, record2, record3))

        val latest = financialRecordDao.getLatestFinancialRecordByUserId(userId)

        assertNotNull(latest)
        assertEquals("Newest Record", latest?.pemanfaatanIuran)
    }

    @Test
    fun `getLatestFinancialRecordByUserId with no records should return null`() = runTest {
        val userId = createTestUser()

        val latest = financialRecordDao.getLatestFinancialRecordByUserId(userId)

        assertNull(latest)
    }

    @Test
    fun `searchFinancialRecordsByPemanfaatan should return matching records`() = runTest {
        val userId = createTestUser()

        val records = listOf(
            createTestRecord(userId, 1).copy(pemanfaatanIuran = "Maintenance of park"),
            createTestRecord(userId, 2).copy(pemanfaatanIuran = "Repair of fence"),
            createTestRecord(userId, 3).copy(pemanfaatanIuran = "Cleaning of common area"),
            createTestRecord(userId, 4).copy(pemanfaatanIuran = "Maintenance of lights")
        )

        financialRecordDao.insertAll(records)

        val searchResults = financialRecordDao.searchFinancialRecordsByPemanfaatan("maintenance").first()

        assertEquals(2, searchResults.size)
        assertTrue(searchResults.all { it.pemanfaatanIuran.contains("maintenance", ignoreCase = true) })
    }

    @Test
    fun `searchFinancialRecordsByPemanfaatan should be case insensitive`() = runTest {
        val userId = createTestUser()

        val record = createTestRecord(userId, 1).copy(pemanfaatanIuran = "Maintenance")

        financialRecordDao.insert(record)

        val searchResults1 = financialRecordDao.searchFinancialRecordsByPemanfaatan("maintenance").first()
        val searchResults2 = financialRecordDao.searchFinancialRecordsByPemanfaatan("MAINTENANCE").first()
        val searchResults3 = financialRecordDao.searchFinancialRecordsByPemanfaatan("MainTeNance").first()

        assertEquals(1, searchResults1.size)
        assertEquals(1, searchResults2.size)
        assertEquals(1, searchResults3.size)
    }

    @Test
    fun `searchFinancialRecordsByPemanfaatan with no matches should return empty list`() = runTest {
        val userId = createTestUser()

        val record = createTestRecord(userId, 1).copy(pemanfaatanIuran = "Maintenance")

        financialRecordDao.insert(record)

        val searchResults = financialRecordDao.searchFinancialRecordsByPemanfaatan("nonexistent").first()

        assertTrue(searchResults.isEmpty())
    }

    @Test
    fun `getAllFinancialRecords should return all records sorted by updated_at desc`() = runTest {
        val userId = createTestUser()

        val now = Date()
        val earlier = Date(now.time - 1000000)
        val evenEarlier = Date(now.time - 2000000)

        val record1 = createTestRecord(userId, 1).copy(
            pemanfaatanIuran = "Old",
            updatedAt = evenEarlier
        )
        val record2 = createTestRecord(userId, 2).copy(
            pemanfaatanIuran = "Middle",
            updatedAt = earlier
        )
        val record3 = createTestRecord(userId, 3).copy(
            pemanfaatanIuran = "New",
            updatedAt = now
        )

        financialRecordDao.insertAll(listOf(record1, record2, record3))

        val allRecords = financialRecordDao.getAllFinancialRecords().first()

        assertEquals(3, allRecords.size)
        assertEquals("New", allRecords[0].pemanfaatanIuran)
        assertEquals("Middle", allRecords[1].pemanfaatanIuran)
        assertEquals("Old", allRecords[2].pemanfaatanIuran)
    }

    @Test
    fun `getAllFinancialRecords should return empty flow when no records exist`() = runTest {
        val allRecords = financialRecordDao.getAllFinancialRecords().first()

        assertTrue(allRecords.isEmpty())
    }

    @Test
    fun `update financial record should modify existing record`() = runTest {
        val userId = createTestUser()
        val record = createTestRecord(userId, 1)

        val id = financialRecordDao.insert(record)

        val updatedRecord = record.copy(
            id = id,
            totalIuranRekap = 999,
            pemanfaatanIuran = "Updated"
        )

        financialRecordDao.update(updatedRecord)

        val retrieved = financialRecordDao.getFinancialRecordById(id)

        assertEquals(999, retrieved?.totalIuranRekap)
        assertEquals("Updated", retrieved?.pemanfaatanIuran)
    }

    @Test
    fun `delete financial record should remove from database`() = runTest {
        val userId = createTestUser()
        val record = createTestRecord(userId, 1)

        val id = financialRecordDao.insert(record)

        var allRecords = financialRecordDao.getAllFinancialRecords().first()
        assertEquals(1, allRecords.size)

        val recordToDelete = financialRecordDao.getFinancialRecordById(id)!!
        financialRecordDao.delete(recordToDelete)

        allRecords = financialRecordDao.getAllFinancialRecords().first()
        assertEquals(0, allRecords.size)
    }

    @Test
    fun `deleteById should remove record from database`() = runTest {
        val userId = createTestUser()
        val record = createTestRecord(userId, 1)

        val id = financialRecordDao.insert(record)

        var allRecords = financialRecordDao.getAllFinancialRecords().first()
        assertEquals(1, allRecords.size)

        financialRecordDao.deleteById(id)

        allRecords = financialRecordDao.getAllFinancialRecords().first()
        assertEquals(0, allRecords.size)
    }

    @Test
    fun `deleteByUserId should remove all records for user`() = runTest {
        val userId1 = createTestUser()
        val userId2 = createTestUser(2)

        val record1 = createTestRecord(userId1, 1).copy(pemanfaatanIuran = "Record 1")
        val record2 = createTestRecord(userId1, 2).copy(pemanfaatanIuran = "Record 2")
        val record3 = createTestRecord(userId2, 3).copy(pemanfaatanIuran = "Record 3")

        financialRecordDao.insertAll(listOf(record1, record2, record3))

        financialRecordDao.deleteByUserId(userId1)

        val allRecords = financialRecordDao.getAllFinancialRecords().first()
        assertEquals(1, allRecords.size)
        assertEquals("Record 3", allRecords[0].pemanfaatanIuran)
    }

    @Test
    fun `deleteAll should remove all financial records`() = runTest {
        val userId = createTestUser()

        val records = listOf(
            createTestRecord(userId, 1).copy(pemanfaatanIuran = "Record 1"),
            createTestRecord(userId, 2).copy(pemanfaatanIuran = "Record 2")
        )

        financialRecordDao.insertAll(records)

        var allRecords = financialRecordDao.getAllFinancialRecords().first()
        assertEquals(2, allRecords.size)

        financialRecordDao.deleteAll()

        allRecords = financialRecordDao.getAllFinancialRecords().first()
        assertTrue(allRecords.isEmpty())
    }

    @Test
    fun `getCount should return correct count`() = runTest {
        val userId = createTestUser()

        assertEquals(0, financialRecordDao.getCount())

        financialRecordDao.insert(createTestRecord(userId, 1))

        assertEquals(1, financialRecordDao.getCount())

        financialRecordDao.insert(createTestRecord(userId, 2))

        assertEquals(2, financialRecordDao.getCount())
    }

    @Test
    fun `getCountByUserId should return correct count for specific user`() = runTest {
        val userId1 = createTestUser()
        val userId2 = createTestUser(2)

        financialRecordDao.insertAll(listOf(
            createTestRecord(userId1, 1).copy(pemanfaatanIuran = "Record 1"),
            createTestRecord(userId1, 2).copy(pemanfaatanIuran = "Record 2"),
            createTestRecord(userId2, 3).copy(pemanfaatanIuran = "Record 3")
        ))

        assertEquals(2, financialRecordDao.getCountByUserId(userId1))
        assertEquals(1, financialRecordDao.getCountByUserId(userId2))
    }

    @Test
    fun `getTotalRekapByUserId should return sum of total_iuran_rekap`() = runTest {
        val userId = createTestUser()

        val records = listOf(
            createTestRecord(userId, 1).copy(totalIuranRekap = 100),
            createTestRecord(userId, 2).copy(totalIuranRekap = 200),
            createTestRecord(userId, 3).copy(totalIuranRekap = 300)
        )

        financialRecordDao.insertAll(records)

        val total = financialRecordDao.getTotalRekapByUserId(userId)

        assertEquals(600L, total)
    }

    @Test
    fun `getTotalRekapByUserId with no records should return null`() = runTest {
        val userId = createTestUser()

        val total = financialRecordDao.getTotalRekapByUserId(userId)

        assertNull(total)
    }

    @Test
    fun `getFinancialRecordsUpdatedSince should return records after specified time`() = runTest {
        val userId = createTestUser()

        val now = Date()
        val before = Date(now.time - 1000000)
        val after = Date(now.time - 500000)

        val record1 = createTestRecord(userId, 1).copy(
            pemanfaatanIuran = "Old",
            updatedAt = before
        )
        val record2 = createTestRecord(userId, 2).copy(
            pemanfaatanIuran = "New",
            updatedAt = after
        )

        financialRecordDao.insertAll(listOf(record1, record2))

        val records = financialRecordDao.getFinancialRecordsUpdatedSince(after.time - 1).first()

        assertEquals(1, records.size)
        assertEquals("New", records[0].pemanfaatanIuran)
    }

    @Test
    fun `getAllFinancialRecords should emit updates when data changes`() = runTest {
        val userId = createTestUser()

        var allRecords = financialRecordDao.getAllFinancialRecords().first()
        assertEquals(0, allRecords.size)

        financialRecordDao.insert(createTestRecord(userId, 1))

        allRecords = financialRecordDao.getAllFinancialRecords().first()
        assertEquals(1, allRecords.size)
    }

    @Test
    fun `financial record dates should be persisted correctly`() = runTest {
        val userId = createTestUser()

        val customDate = Date(1000000)

        val record = createTestRecord(userId, 1).copy(
            createdAt = customDate,
            updatedAt = customDate
        )

        val id = financialRecordDao.insert(record)

        val retrieved = financialRecordDao.getFinancialRecordById(id)

        assertEquals(customDate, retrieved?.createdAt)
        assertEquals(customDate, retrieved?.updatedAt)
    }

    @Test
    fun `insert with duplicate id should replace existing record`() = runTest {
        val userId = createTestUser()

        val record1 = createTestRecord(userId, 1).copy(
            pemanfaatanIuran = "Original",
            totalIuranRekap = 500
        )

        val id1 = financialRecordDao.insert(record1)

        val record2 = record1.copy(
            id = id1,
            pemanfaatanIuran = "Updated",
            totalIuranRekap = 999
        )

        val id2 = financialRecordDao.insert(record2)

        assertEquals(id1, id2)

        val retrieved = financialRecordDao.getFinancialRecordById(id1)
        assertEquals("Updated", retrieved?.pemanfaatanIuran)
        assertEquals(999, retrieved?.totalIuranRekap)
    }

    @Test
    fun `should handle large number of records efficiently`() = runTest {
        val userId = createTestUser()

        val records = (1..100).map { i ->
            createTestRecord(userId, i.toLong()).copy(
                pemanfaatanIuran = "Record $i",
                totalIuranRekap = i * 100,
                updatedAt = Date(System.currentTimeMillis() - i * 1000L)
            )
        }

        financialRecordDao.insertAll(records)

        val allRecords = financialRecordDao.getAllFinancialRecords().first()
        assertEquals(100, allRecords.size)

        val count = financialRecordDao.getCount()
        assertEquals(100, count)

        val userRecords = financialRecordDao.getFinancialRecordsByUserId(userId).first()
        assertEquals(100, userRecords.size)
    }
}
