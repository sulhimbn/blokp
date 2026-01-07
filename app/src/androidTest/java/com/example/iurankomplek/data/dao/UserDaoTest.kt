package com.example.iurankomplek.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import java.util.Date
import java.util.UUID

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class UserDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .addMigrations(Migration1())
            .build()
        userDao = db.userDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndGetUserById() = runTest {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "Jalan Test 123",
            avatar = "https://example.com/avatar.jpg"
        )
        val userId = userDao.insert(user)

        val retrievedUser = userDao.getUserById(userId)
        assertNotNull(retrievedUser)
        assertEquals(userId, retrievedUser?.id)
        assertEquals(user.email, retrievedUser?.email)
        assertEquals(user.firstName, retrievedUser?.firstName)
        assertEquals(user.lastName, retrievedUser?.lastName)
    }

    @Test
    fun insertAndGetUserByEmail() = runTest {
        val user = UserEntity(
            email = "unique@example.com",
            firstName = "Jane",
            lastName = "Smith",
            alamat = "Jalan Unique 456",
            avatar = "https://example.com/jane.jpg"
        )
        userDao.insert(user)

        val retrievedUser = userDao.getUserByEmail("unique@example.com")
        assertNotNull(retrievedUser)
        assertEquals(user.email, retrievedUser?.email)
        assertEquals(user.firstName, retrievedUser?.firstName)
    }

    @Test
    fun getAllUsersReturnsEmptyInitially() = runTest {
        val users = userDao.getAllUsers().first()
        assertTrue(users.isEmpty())
    }

    @Test
    fun getAllUsersReturnsAllInsertedUsers() = runTest {
        val user1 = UserEntity(
            email = "user1@example.com",
            firstName = "Alice",
            lastName = "Johnson",
            alamat = "Address 1",
            avatar = "avatar1.jpg"
        )
        val user2 = UserEntity(
            email = "user2@example.com",
            firstName = "Bob",
            lastName = "Williams",
            alamat = "Address 2",
            avatar = "avatar2.jpg"
        )
        userDao.insert(user1)
        userDao.insert(user2)

        val users = userDao.getAllUsers().first()
        assertEquals(2, users.size)
    }

    @Test
    fun getAllUsersOrdersByLastNameThenFirstName() = runTest {
        val user1 = UserEntity(
            email = "user1@example.com",
            firstName = "Zoe",
            lastName = "Anderson",
            alamat = "Address 1",
            avatar = "avatar1.jpg"
        )
        val user2 = UserEntity(
            email = "user2@example.com",
            firstName = "Alice",
            lastName = "Brown",
            alamat = "Address 2",
            avatar = "avatar2.jpg"
        )
        val user3 = UserEntity(
            email = "user3@example.com",
            firstName = "Charlie",
            lastName = "Brown",
            alamat = "Address 3",
            avatar = "avatar3.jpg"
        )
        userDao.insert(user1)
        userDao.insert(user2)
        userDao.insert(user3)

        val users = userDao.getAllUsers().first()
        assertEquals(3, users.size)
        assertEquals("Brown", users[0].lastName)
        assertEquals("Alice", users[0].firstName)
        assertEquals("Brown", users[1].lastName)
        assertEquals("Charlie", users[1].firstName)
        assertEquals("Anderson", users[2].lastName)
    }

    @Test
    fun updateUser() = runTest {
        val user = UserEntity(
            email = "update@example.com",
            firstName = "Original",
            lastName = "Name",
            alamat = "Original Address",
            avatar = "original.jpg"
        )
        val userId = userDao.insert(user)

        val updatedUser = user.copy(
            id = userId,
            firstName = "Updated",
            lastName = "User",
            alamat = "Updated Address",
            updatedAt = Date()
        )
        userDao.update(updatedUser)

        val retrievedUser = userDao.getUserById(userId)
        assertEquals("Updated", retrievedUser?.firstName)
        assertEquals("Updated Address", retrievedUser?.alamat)
    }

    @Test
    fun deleteUser() = runTest {
        val user = UserEntity(
            email = "delete@example.com",
            firstName = "Delete",
            lastName = "Me",
            alamat = "Delete Address",
            avatar = "delete.jpg"
        )
        val userId = userDao.insert(user)
        userDao.delete(user)

        val retrievedUser = userDao.getUserById(userId)
        assertNull(retrievedUser)
    }

    @Test
    fun deleteById() = runTest {
        val user = UserEntity(
            email = "deletebyid@example.com",
            firstName = "Delete",
            lastName = "ById",
            alamat = "Delete By Id Address",
            avatar = "deletebyid.jpg"
        )
        val userId = userDao.insert(user)
        userDao.deleteById(userId)

        val retrievedUser = userDao.getUserById(userId)
        assertNull(retrievedUser)
    }

    @Test
    fun deleteAll() = runTest {
        val user1 = UserEntity(
            email = "delete1@example.com",
            firstName = "Delete1",
            lastName = "User",
            alamat = "Address 1",
            avatar = "delete1.jpg"
        )
        val user2 = UserEntity(
            email = "delete2@example.com",
            firstName = "Delete2",
            lastName = "User",
            alamat = "Address 2",
            avatar = "delete2.jpg"
        )
        userDao.insert(user1)
        userDao.insert(user2)
        userDao.deleteAll()

        val users = userDao.getAllUsers().first()
        assertTrue(users.isEmpty())
    }

    @Test
    fun getCount() = runTest {
        assertEquals(0, userDao.getCount())

        val user1 = UserEntity(
            email = "count1@example.com",
            firstName = "Count1",
            lastName = "User",
            alamat = "Address 1",
            avatar = "count1.jpg"
        )
        val user2 = UserEntity(
            email = "count2@example.com",
            firstName = "Count2",
            lastName = "User",
            alamat = "Address 2",
            avatar = "count2.jpg"
        )
        userDao.insert(user1)
        userDao.insert(user2)

        assertEquals(2, userDao.getCount())
    }

    @Test
    fun emailExistsReturnsFalseForNonExistentEmail() = runTest {
        assertFalse(userDao.emailExists("nonexistent@example.com"))
    }

    @Test
    fun emailExistsReturnsTrueForExistingEmail() = runTest {
        val user = UserEntity(
            email = "exists@example.com",
            firstName = "Exists",
            lastName = "User",
            alamat = "Address",
            avatar = "exists.jpg"
        )
        userDao.insert(user)

        assertTrue(userDao.emailExists("exists@example.com"))
    }

    @Test
    fun getUserWithFinancialRecordsReturnsUserWithRecords() = runTest {
        val user = UserEntity(
            email = "withrecords@example.com",
            firstName = "With",
            lastName = "Records",
            alamat = "Address",
            avatar = "withrecords.jpg"
        )
        val userId = userDao.insert(user)

        val financialRecord = FinancialRecordEntity(
            userId = userId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 50,
            totalIuranRekap = 250,
            pemanfaatanIuran = "Test Pemanfaatan"
        )
        db.financialRecordDao().insert(financialRecord)

        val userWithRecords = userDao.getUserWithFinancialRecords(userId)
        assertNotNull(userWithRecords)
        assertEquals(userId, userWithRecords?.user?.id)
        assertEquals(1, userWithRecords?.financialRecords?.size)
        assertEquals("Test Pemanfaatan", userWithRecords?.financialRecords?.get(0)?.pemanfaatanIuran)
    }

    @Test
    fun getUserWithFinancialRecordsReturnsUserWithNoRecords() = runTest {
        val user = UserEntity(
            email = "norecords@example.com",
            firstName = "No",
            lastName = "Records",
            alamat = "Address",
            avatar = "norecords.jpg"
        )
        val userId = userDao.insert(user)

        val userWithRecords = userDao.getUserWithFinancialRecords(userId)
        assertNotNull(userWithRecords)
        assertEquals(userId, userWithRecords?.user?.id)
        assertTrue(userWithRecords?.financialRecords?.isEmpty() == true)
    }

    @Test
    fun insertAll() = runTest {
        val users = listOf(
            UserEntity(
                email = "bulk1@example.com",
                firstName = "Bulk1",
                lastName = "User",
                alamat = "Address 1",
                avatar = "bulk1.jpg"
            ),
            UserEntity(
                email = "bulk2@example.com",
                firstName = "Bulk2",
                lastName = "User",
                alamat = "Address 2",
                avatar = "bulk2.jpg"
            ),
            UserEntity(
                email = "bulk3@example.com",
                firstName = "Bulk3",
                lastName = "User",
                alamat = "Address 3",
                avatar = "bulk3.jpg"
            )
        )
        userDao.insertAll(users)

        assertEquals(3, userDao.getCount())
    }

    @Test
    fun emailConstraintEnforcesUniqueness() = runTest {
        val user1 = UserEntity(
            email = "duplicate@example.com",
            firstName = "First",
            lastName = "User",
            alamat = "Address 1",
            avatar = "first.jpg"
        )
        val user2 = UserEntity(
            email = "duplicate@example.com",
            firstName = "Second",
            lastName = "User",
            alamat = "Address 2",
            avatar = "second.jpg"
        )
        val userId1 = userDao.insert(user1)
        val userId2 = userDao.insert(user2)

        val user1Retrieved = userDao.getUserById(userId1)
        val user2Retrieved = userDao.getUserById(userId2)
        
        assertNotNull(user1Retrieved)
        assertNotNull(user2Retrieved)
        assertEquals(userId2, user1Retrieved?.id)
    }

    @Test
    fun validationEmailCannotBeBlank() {
        assertThrows(IllegalArgumentException::class.java) {
            UserEntity(
                email = "",
                firstName = "Valid",
                lastName = "Name",
                alamat = "Valid Address",
                avatar = "valid.jpg"
            )
        }
    }

    @Test
    fun validationEmailMustContainAtSymbol() {
        assertThrows(IllegalArgumentException::class.java) {
            UserEntity(
                email = "invalidemail.com",
                firstName = "Valid",
                lastName = "Name",
                alamat = "Valid Address",
                avatar = "valid.jpg"
            )
        }
    }

    @Test
    fun validationFirstNameCannotBeBlank() {
        assertThrows(IllegalArgumentException::class.java) {
            UserEntity(
                email = "valid@example.com",
                firstName = "",
                lastName = "Name",
                alamat = "Valid Address",
                avatar = "valid.jpg"
            )
        }
    }

    @Test
    fun validationLastNameCannotBeBlank() {
        assertThrows(IllegalArgumentException::class.java) {
            UserEntity(
                email = "valid@example.com",
                firstName = "Valid",
                lastName = "",
                alamat = "Valid Address",
                avatar = "valid.jpg"
            )
        }
    }

    @Test
    fun validationAlamatCannotBeBlank() {
        assertThrows(IllegalArgumentException::class.java) {
            UserEntity(
                email = "valid@example.com",
                firstName = "Valid",
                lastName = "Name",
                alamat = "",
                avatar = "valid.jpg"
            )
        }
    }

    @Test
    fun fullNamePropertyReturnsCombinedName() {
        val user = UserEntity(
            email = "valid@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "Valid Address",
            avatar = "valid.jpg"
        )
        assertEquals("John Doe", user.fullName)
    }
}
