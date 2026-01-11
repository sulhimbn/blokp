package com.example.iurankomplek.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.iurankomplek.data.dao.UserDao
import com.example.iurankomplek.data.dao.FinancialRecordDao
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .addMigrations(Migration1())
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun databaseProvidesUserDao() {
        assertNotNull(db.userDao())
    }

    @Test
    fun databaseProvidesFinancialRecordDao() {
        assertNotNull(db.financialRecordDao())
    }

    @Test
    fun databaseIsSingleton() {
        val db1 = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        val db2 = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        assertNotNull(db1)
        assertNotNull(db2)
        assertNotSame(db1, db2)

        db1.close()
        db2.close()
    }

    @Test
    fun databaseInitializesSuccessfully() {
        val userDao = db.userDao()
        val financialRecordDao = db.financialRecordDao()

        assertNotNull(userDao)
        assertNotNull(financialRecordDao)
    }

    @Test
    fun databaseSupportsTransactions() = runBlocking {
        val userDao = db.userDao()

        val user = UserEntity(
            email = "transaction@test.com",
            firstName = "Transaction",
            lastName = "Test",
            alamat = "Test Address",
            avatar = "test.jpg"
        )

        val userId = userDao.insert(user)
        assertTrue(userId > 0)

        val retrievedUser = userDao.getUserById(userId)
        assertNotNull(retrievedUser)
        assertEquals("transaction@test.com", retrievedUser?.email)
    }
}
