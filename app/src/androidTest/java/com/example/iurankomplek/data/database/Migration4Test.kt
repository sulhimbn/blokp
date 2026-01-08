package com.example.iurankomplek.data.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
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
class Migration4Test {

    private lateinit var database: androidx.room.RoomDatabase

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val TEST_DB = "migration-test-4"

    @Before
    fun setup() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun migrate3To4_shouldCreateCompositeIndex() {
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
        )

        val db = helper.createDatabase(TEST_DB, 3).apply {
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB, 4, true,
            Migration1(), Migration1Down, Migration2, Migration2Down,
            Migration3, Migration3Down, Migration4
        )

        migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='financial_records'"
        ).use { cursor ->
            val indexes = mutableListOf<String>()
            while (cursor.moveToNext()) {
                indexes.add(cursor.getString(0))
            }

            assertTrue(
                indexes.contains("idx_financial_user_rekap"),
                "Index idx_financial_user_rekap should exist"
            )
        }

        migratedDb.close()
    }

    @Test
    fun migrate3To4_shouldHaveCorrectIndexColumns() {
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
        )

        val db = helper.createDatabase(TEST_DB, 3).apply {
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB, 4, true,
            Migration1(), Migration1Down, Migration2, Migration2Down,
            Migration3, Migration3Down, Migration4
        )

        migratedDb.query(
            "PRAGMA index_info(idx_financial_user_rekap)"
        ).use { cursor ->
            val columns = mutableListOf<String>()
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(2))
            }

            assertTrue(
                columns.contains("user_id"),
                "Index should contain user_id column"
            )
            assertTrue(
                columns.contains("total_iuran_rekap"),
                "Index should contain total_iuran_rekap column"
            )
        }

        migratedDb.close()
    }

    @Test
    fun migrate4To3_shouldDropIndex() {
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
        )

        val db = helper.createDatabase(TEST_DB, 3).apply {
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB, 4, true,
            Migration1(), Migration1Down, Migration2, Migration2Down,
            Migration3, Migration3Down, Migration4
        )

        val downMigratedDb = helper.runMigrationsAndValidate(
            TEST_DB, 3, true,
            Migration1(), Migration1Down, Migration2, Migration2Down,
            Migration3, Migration3Down, Migration4, Migration4Down
        )

        downMigratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='financial_records' AND name='idx_financial_user_rekap'"
        ).use { cursor ->
            val count = cursor.count
            assertTrue(count == 0, "Index idx_financial_user_rekap should be dropped")
        }

        downMigratedDb.close()
    }

    @Test
    fun migratedDatabase_shouldAllowFinancialOperations() {
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

        val financialRecordDao = database.financialRecordDao()

        val record = com.example.iurankomplek.data.entity.FinancialRecordEntity(
            userId = 1,
            iuranPerwarga = 100000,
            jumlahIuranBulanan = 10000,
            totalIuranIndividu = 300000,
            pengeluaranIuranWarga = 50000,
            totalIuranRekap = 250000,
            pemanfaatanIuran = "Maintenance fasilitas"
        )

        runBlocking {
            val id = financialRecordDao.insert(record)
            assertTrue(id > 0, "Insert should succeed")

            val retrieved = financialRecordDao.getFinancialRecordById(id)
            assertNotNull(retrieved, "Record should be retrievable")

            val total = financialRecordDao.getTotalRekapByUserId(1)
            assertNotNull(total, "SUM query should work with new index")
            assertTrue(total == 250000L, "SUM should return correct value")
        }

        database.close()
    }
}
