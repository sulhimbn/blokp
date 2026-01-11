package com.example.iurankomplek.data.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
class Migration4DownTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val TEST_DB = "migration-test-4-down"

    @Before
    fun setup() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun migrate4To3_shouldPreserveData() {
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
        )

        val db = helper.createDatabase(TEST_DB, 3).apply {
            close()
        }

        helper.runMigrationsAndValidate(
            TEST_DB, 4, true,
            Migration1(), Migration1Down, Migration2, Migration2Down,
            Migration3, Migration3Down, Migration4
        )

        val database = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
            TEST_DB
        ).addMigrations(
            Migration1(), Migration1Down, Migration2, Migration2Down,
            Migration3, Migration3Down, Migration4
        ).build()

        val userDao = database.userDao()
        val financialRecordDao = database.financialRecordDao()

        runBlocking {
            val user = com.example.iurankomplek.data.entity.UserEntity(
                email = "test@example.com",
                firstName = "Test",
                lastName = "User",
                alamat = "Test Address",
                avatar = "https://example.com/avatar.jpg"
            )
            val userId = userDao.insert(user)

            val record = com.example.iurankomplek.data.entity.FinancialRecordEntity(
                userId = userId,
                iuranPerwarga = 100000,
                jumlahIuranBulanan = 10000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 50000,
                totalIuranRekap = 250000,
                pemanfaatanIuran = "Maintenance fasilitas"
            )
            financialRecordDao.insert(record)
        }

        database.close()

        helper.runMigrationsAndValidate(
            TEST_DB, 3, true,
            Migration1(), Migration1Down, Migration2, Migration2Down,
            Migration3, Migration3Down, Migration4, Migration4Down
        )

        val downgradedDb = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
            TEST_DB
        ).addMigrations(
            Migration1(), Migration1Down, Migration2, Migration2Down,
            Migration3, Migration3Down, Migration4, Migration4Down
        ).build()

        runBlocking {
            val allUsers = userDao.getAllUsers()
            assertTrue(allUsers.size == 1, "User data should be preserved")

            val total = financialRecordDao.getTotalRekapByUserId(1)
            assertNotNull(total, "Financial record data should be preserved")
            assertTrue(total == 250000L, "Financial data should remain intact")
        }

        downgradedDb.close()
    }

    @Test
    fun migrate4To3_shouldRemoveIndex() {
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
        )

        val db = helper.createDatabase(TEST_DB, 3).apply {
            close()
        }

        helper.runMigrationsAndValidate(
            TEST_DB, 4, true,
            Migration1(), Migration1Down, Migration2, Migration2Down,
            Migration3, Migration3Down, Migration4
        )

        val downgradedDb = helper.runMigrationsAndValidate(
            TEST_DB, 3, true,
            Migration1(), Migration1Down, Migration2, Migration2Down,
            Migration3, Migration3Down, Migration4, Migration4Down
        )

        downgradedDb.query(
            "SELECT name FROM sqlite_master WHERE type='index' AND name='idx_financial_user_rekap'"
        ).use { cursor ->
            val count = cursor.count
            assertTrue(count == 0, "Index should be removed in down migration")
        }

        downgradedDb.close()
    }
}
