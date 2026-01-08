package com.example.iurankomplek.data.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.iurankomplek.data.cache.CacheManager
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.dao.UserDao
import com.example.iurankomplek.data.dao.FinancialRecordDao
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class Migration7Test {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate6To7() {
        var db = helper.createDatabase(TEST_DB, 6).apply {
            execSQL(
                """
                INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted, created_at, updated_at)
                VALUES ('test@example.com', 'John', 'Doe', 'Address', 'avatar.png', 0, 1234567890, 1234567890)
                """
            )
            execSQL(
                """
                INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted, created_at, updated_at)
                VALUES (1, 100, 200, 300, 400, 500, 'Test', 0, 1234567890, 1234567890)
                """
            )
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, Migration7)

        db.query("SELECT * FROM users").use { cursor ->
            assert(cursor.count == 1)
            assert(cursor.moveToFirst())
            assert(cursor.getString(cursor.getColumnIndexOrThrow("email")) == "test@example.com")
        }

        db.query("SELECT * FROM financial_records").use { cursor ->
            assert(cursor.count == 1)
            assert(cursor.moveToFirst())
            assert(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")) == 1)
        }

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name LIKE 'idx_%_active%'").use { cursor ->
            val indexes = mutableListOf<String>()
            while (cursor.moveToNext()) {
                indexes.add(cursor.getString(0))
            }
            assert(indexes.contains("idx_users_active"))
            assert(indexes.contains("idx_users_active_updated"))
            assert(indexes.contains("idx_financial_active"))
            assert(indexes.contains("idx_financial_active_updated"))
            assert(indexes.contains("idx_financial_active_user_updated"))
        }
    }

    @Test
    fun migrate7To6() {
        var db = helper.createDatabase(TEST_DB, 6).apply {
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, Migration7)

        execSQL(
            """
            INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted, created_at, updated_at)
            VALUES ('test@example.com', 'John', 'Doe', 'Address', 'avatar.png', 0, 1234567890, 1234567890)
            """
        )
        execSQL(
            """
            INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted, created_at, updated_at)
            VALUES (1, 100, 200, 300, 400, 500, 'Test', 0, 1234567890, 1234567890)
            """
        )
        close()

        db = helper.runMigrationsAndValidate(TEST_DB, 6, false, Migration7Down)

        db.query("SELECT * FROM users").use { cursor ->
            assert(cursor.count == 1)
        }

        db.query("SELECT * FROM financial_records").use { cursor ->
            assert(cursor.count == 1)
        }

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name LIKE 'idx_%_active%'").use { cursor ->
            assert(cursor.count == 0)
        }
    }
}
