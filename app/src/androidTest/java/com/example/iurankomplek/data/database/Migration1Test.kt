package com.example.iurankomplek.data.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class Migration1Test {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate0To1() {
        var db = helper.createDatabase(TEST_DB, 0)

        db.query("SELECT * FROM users").use { cursor ->
            assertEquals(0, cursor.count)
        }

        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1())

        db.query("SELECT * FROM users").use { cursor ->
            assertEquals(0, cursor.count)
        }

        db.query("SELECT * FROM financial_records").use { cursor ->
            assertEquals(0, cursor.count)
        }

        db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, created_at, updated_at) VALUES ('test@example.com', 'Test', 'User', 'Test Address', 'avatar.jpg', 1000000, 1000000)")
        
        val cursor = db.query("SELECT * FROM users WHERE email = 'test@example.com'")
        cursor.moveToFirst()
        assertEquals("test@example.com", cursor.getString(cursor.getColumnIndexOrThrow("email")))
        assertEquals("Test", cursor.getString(cursor.getColumnIndexOrThrow("first_name")))
        cursor.close()

        db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, created_at, updated_at) VALUES (1, 100, 200, 300, 50, 250, 'Test Pemanfaatan', 1000000, 1000000)")
        
        val financialCursor = db.query("SELECT * FROM financial_records WHERE user_id = 1")
        financialCursor.moveToFirst()
        assertEquals(1, financialCursor.getInt(financialCursor.getColumnIndexOrThrow("user_id")))
        assertEquals(100, financialCursor.getInt(financialCursor.getColumnIndexOrThrow("iuran_perwarga")))
        assertEquals("Test Pemanfaatan", financialCursor.getString(financialCursor.getColumnIndexOrThrow("pemanfaatan_iuran")))
        financialCursor.close()

        db.close()
    }

    @Test
    fun migrationCreatesCorrectSchema() {
        val db = helper.createDatabase(TEST_DB, 0)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1())

        migratedDb.query("PRAGMA table_info(users)").use { cursor ->
            val columns = mutableListOf<String>()
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(1))
            }
            assertTrue(columns.contains("id"))
            assertTrue(columns.contains("email"))
            assertTrue(columns.contains("first_name"))
            assertTrue(columns.contains("last_name"))
            assertTrue(columns.contains("alamat"))
            assertTrue(columns.contains("avatar"))
            assertTrue(columns.contains("created_at"))
            assertTrue(columns.contains("updated_at"))
        }

        migratedDb.query("PRAGMA table_info(financial_records)").use { cursor ->
            val columns = mutableListOf<String>()
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(1))
            }
            assertTrue(columns.contains("id"))
            assertTrue(columns.contains("user_id"))
            assertTrue(columns.contains("iuran_perwarga"))
            assertTrue(columns.contains("jumlah_iuran_bulanan"))
            assertTrue(columns.contains("total_iuran_individu"))
            assertTrue(columns.contains("pengeluaran_iuran_warga"))
            assertTrue(columns.contains("total_iuran_rekap"))
            assertTrue(columns.contains("pemanfaatan_iuran"))
            assertTrue(columns.contains("created_at"))
            assertTrue(columns.contains("updated_at"))
        }

        migratedDb.close()
    }

    @Test
    fun migrationCreatesIndexes() {
        val db = helper.createDatabase(TEST_DB, 0)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1())

        migratedDb.query("SELECT name FROM sqlite_master WHERE type='index' AND name LIKE 'idx_%'").use { cursor ->
            val indexes = mutableListOf<String>()
            while (cursor.moveToNext()) {
                indexes.add(cursor.getString(0))
            }
            assertTrue(indexes.contains("idx_users_email"))
            assertTrue(indexes.contains("idx_financial_user_id"))
            assertTrue(indexes.contains("idx_financial_updated_at"))
        }

        migratedDb.close()
    }

    @Test
    fun migrationCreatesForeignKeys() {
        val db = helper.createDatabase(TEST_DB, 0)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1())

        migratedDb.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, created_at, updated_at) VALUES ('test@example.com', 'Test', 'User', 'Test Address', 'avatar.jpg', 1000000, 1000000)")

        var exception: Exception? = null
        try {
            migratedDb.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, created_at, updated_at) VALUES (999, 100, 200, 300, 50, 250, 'Invalid User', 1000000, 1000000)")
        } catch (e: Exception) {
            exception = e
        }

        assertNotNull("Foreign key constraint should prevent inserting financial record with non-existent user", exception)
        
        migratedDb.close()
    }

    @Test
    fun migrationCreatesUniqueConstraintOnEmail() {
        val db = helper.createDatabase(TEST_DB, 0)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1())

        migratedDb.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, created_at, updated_at) VALUES ('duplicate@example.com', 'Test', 'User1', 'Address 1', 'avatar1.jpg', 1000000, 1000000)")

        var exception: Exception? = null
        try {
            migratedDb.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, created_at, updated_at) VALUES ('duplicate@example.com', 'Test', 'User2', 'Address 2', 'avatar2.jpg', 1000000, 1000000)")
        } catch (e: Exception) {
            exception = e
        }

        assertNotNull("Unique constraint should prevent duplicate emails", exception)
        
        migratedDb.close()
    }

    @Test
    fun migrationCreatesCheckConstraints() {
        val db = helper.createDatabase(TEST_DB, 0)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 1, true, Migration1())

        var exception: Exception? = null
        try {
            migratedDb.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, created_at, updated_at) VALUES (1, -100, 200, 300, 50, 250, 'Test', 1000000, 1000000)")
        } catch (e: Exception) {
            exception = e
        }

        assertNotNull("Check constraint should prevent negative values", exception)
        
        migratedDb.close()
    }
}
