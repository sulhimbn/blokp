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
class UserDaoTest {

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

    @Test
    fun `insert user should return generated id`() = runTest {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val id = userDao.insert(user)

        assertTrue(id > 0)
    }

    @Test
    fun `insert user should persist to database`() = runTest {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        userDao.insert(user)

        val allUsers = userDao.getAllUsers().first()
        assertEquals(1, allUsers.size)
        assertEquals("test@example.com", allUsers[0].email)
    }

    @Test
    fun `insert multiple users should return list of ids`() = runTest {
        val users = listOf(
            UserEntity(
                email = "test1@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = "https://example.com/avatar.jpg"
            ),
            UserEntity(
                email = "test2@example.com",
                firstName = "Jane",
                lastName = "Smith",
                alamat = "456 Oak Ave",
                avatar = "https://example.com/avatar2.jpg"
            )
        )

        val ids = userDao.insertAll(users)

        assertEquals(2, ids.size)
        assertTrue(ids.all { it > 0 })
    }

    @Test
    fun `getUserById should return correct user`() = runTest {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val id = userDao.insert(user)

        val retrieved = userDao.getUserById(id)

        assertNotNull(retrieved)
        assertEquals(id, retrieved?.id)
        assertEquals("test@example.com", retrieved?.email)
        assertEquals("John", retrieved?.firstName)
    }

    @Test
    fun `getUserById with non-existent id should return null`() = runTest {
        val retrieved = userDao.getUserById(999)

        assertNull(retrieved)
    }

    @Test
    fun `getUserByEmail should return correct user`() = runTest {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        userDao.insert(user)

        val retrieved = userDao.getUserByEmail("test@example.com")

        assertNotNull(retrieved)
        assertEquals("test@example.com", retrieved?.email)
        assertEquals("John", retrieved?.firstName)
    }

    @Test
    fun `getUserByEmail with non-existent email should return null`() = runTest {
        val retrieved = userDao.getUserByEmail("nonexistent@example.com")

        assertNull(retrieved)
    }

    @Test
    fun `emailExists should return true for existing email`() = runTest {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        userDao.insert(user)

        assertTrue(userDao.emailExists("test@example.com"))
    }

    @Test
    fun `emailExists should return false for non-existent email`() = runTest {
        assertFalse(userDao.emailExists("nonexistent@example.com"))
    }

    @Test
    fun `getAllUsers should return all users sorted by last name, first name`() = runTest {
        val users = listOf(
            UserEntity(
                email = "john@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = "https://example.com/avatar.jpg"
            ),
            UserEntity(
                email = "alice@example.com",
                firstName = "Alice",
                lastName = "Smith",
                alamat = "456 Oak Ave",
                avatar = "https://example.com/avatar2.jpg"
            ),
            UserEntity(
                email = "bob@example.com",
                firstName = "Bob",
                lastName = "Johnson",
                alamat = "789 Pine Rd",
                avatar = "https://example.com/avatar3.jpg"
            )
        )

        userDao.insertAll(users)

        val allUsers = userDao.getAllUsers().first()

        assertEquals(3, allUsers.size)
        assertEquals("Doe", allUsers[0].lastName)
        assertEquals("Johnson", allUsers[1].lastName)
        assertEquals("Smith", allUsers[2].lastName)
    }

    @Test
    fun `getAllUsers should return empty flow when no users exist`() = runTest {
        val allUsers = userDao.getAllUsers().first()

        assertTrue(allUsers.isEmpty())
    }

    @Test
    fun `getUserWithFinancialRecords should return user with records`() = runTest {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val userId = userDao.insert(user)

        val record1 = FinancialRecordEntity(
            userId = userId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )

        val record2 = FinancialRecordEntity(
            userId = userId,
            iuranPerwarga = 150,
            jumlahIuranBulanan = 250,
            totalIuranIndividu = 350,
            pengeluaranIuranWarga = 450,
            totalIuranRekap = 550,
            pemanfaatanIuran = "Repair"
        )

        financialRecordDao.insertAll(listOf(record1, record2))

        val userWithRecords = userDao.getUserWithFinancialRecords(userId)

        assertNotNull(userWithRecords)
        assertEquals("John", userWithRecords?.user?.firstName)
        assertEquals(2, userWithRecords?.financialRecords?.size)
    }

    @Test
    fun `getUserWithFinancialRecords with no records should return user with empty list`() = runTest {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val userId = userDao.insert(user)

        val userWithRecords = userDao.getUserWithFinancialRecords(userId)

        assertNotNull(userWithRecords)
        assertEquals("John", userWithRecords?.user?.firstName)
        assertTrue(userWithRecords?.financialRecords?.isEmpty() == true)
    }

    @Test
    fun `getAllUsersWithFinancialRecords should return all users with their records`() = runTest {
        val user1 = UserEntity(
            email = "user1@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val user2 = UserEntity(
            email = "user2@example.com",
            firstName = "Jane",
            lastName = "Smith",
            alamat = "456 Oak Ave",
            avatar = "https://example.com/avatar2.jpg"
        )

        val userId1 = userDao.insert(user1)
        val userId2 = userDao.insert(user2)

        val record1 = FinancialRecordEntity(
            userId = userId1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )

        val record2 = FinancialRecordEntity(
            userId = userId2,
            iuranPerwarga = 150,
            jumlahIuranBulanan = 250,
            totalIuranIndividu = 350,
            pengeluaranIuranWarga = 450,
            totalIuranRekap = 550,
            pemanfaatanIuran = "Repair"
        )

        financialRecordDao.insertAll(listOf(record1, record2))

        val allUsersWithRecords = userDao.getAllUsersWithFinancialRecords().first()

        assertEquals(2, allUsersWithRecords.size)
        assertTrue(allUsersWithRecords.all { it.financialRecords.isNotEmpty() })
    }

    @Test
    fun `update user should modify existing record`() = runTest {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val id = userDao.insert(user)

        val updatedUser = UserEntity(
            id = id,
            email = "newemail@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "456 Oak Ave",
            avatar = "https://example.com/avatar2.jpg"
        )

        userDao.update(updatedUser)

        val retrieved = userDao.getUserById(id)

        assertEquals("newemail@example.com", retrieved?.email)
        assertEquals("456 Oak Ave", retrieved?.alamat)
    }

    @Test
    fun `delete user should remove from database`() = runTest {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val id = userDao.insert(user)

        var allUsers = userDao.getAllUsers().first()
        assertEquals(1, allUsers.size)

        val userToDelete = userDao.getUserById(id)!!
        userDao.delete(userToDelete)

        allUsers = userDao.getAllUsers().first()
        assertEquals(0, allUsers.size)
    }

    @Test
    fun `deleteById should remove user and cascade to financial records`() = runTest {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val userId = userDao.insert(user)

        val record = FinancialRecordEntity(
            userId = userId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )

        val recordId = financialRecordDao.insert(record)

        var records = financialRecordDao.getAllFinancialRecords().first()
        assertEquals(1, records.size)

        userDao.deleteById(userId)

        val user = userDao.getUserById(userId)
        assertNull(user)

        records = financialRecordDao.getAllFinancialRecords().first()
        assertTrue(records.isEmpty())
    }

    @Test
    fun `deleteAll should remove all users`() = runTest {
        val users = listOf(
            UserEntity(
                email = "test1@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = "https://example.com/avatar.jpg"
            ),
            UserEntity(
                email = "test2@example.com",
                firstName = "Jane",
                lastName = "Smith",
                alamat = "456 Oak Ave",
                avatar = "https://example.com/avatar2.jpg"
            )
        )

        userDao.insertAll(users)

        var allUsers = userDao.getAllUsers().first()
        assertEquals(2, allUsers.size)

        userDao.deleteAll()

        allUsers = userDao.getAllUsers().first()
        assertTrue(allUsers.isEmpty())
    }

    @Test
    fun `getCount should return correct count`() = runTest {
        assertEquals(0, userDao.getCount())

        userDao.insert(
            UserEntity(
                email = "test1@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = "https://example.com/avatar.jpg"
            )
        )

        assertEquals(1, userDao.getCount())

        userDao.insert(
            UserEntity(
                email = "test2@example.com",
                firstName = "Jane",
                lastName = "Smith",
                alamat = "456 Oak Ave",
                avatar = "https://example.com/avatar2.jpg"
            )
        )

        assertEquals(2, userDao.getCount())
    }

    @Test
    fun `insert with duplicate email should replace existing record`() = runTest {
        val user1 = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val id1 = userDao.insert(user1)

        val user2 = UserEntity(
            id = id1,
            email = "test@example.com",
            firstName = "Jane",
            lastName = "Smith",
            alamat = "456 Oak Ave",
            avatar = "https://example.com/avatar2.jpg"
        )

        val id2 = userDao.insert(user2)

        assertEquals(id1, id2)

        val retrieved = userDao.getUserByEmail("test@example.com")
        assertEquals("Jane", retrieved?.firstName)
        assertEquals("Smith", retrieved?.lastName)
    }

    @Test
    fun `getAllUsers should emit updates when data changes`() = runTest {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        var allUsers = userDao.getAllUsers().first()
        assertEquals(0, allUsers.size)

        userDao.insert(user)

        allUsers = userDao.getAllUsers().first()
        assertEquals(1, allUsers.size)
    }

    @Test
    fun `user dates should be persisted correctly`() = runTest {
        val customDate = Date(1000000)

        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg",
            createdAt = customDate,
            updatedAt = customDate
        )

        val id = userDao.insert(user)

        val retrieved = userDao.getUserById(id)

        assertEquals(customDate, retrieved?.createdAt)
        assertEquals(customDate, retrieved?.updatedAt)
    }
}
