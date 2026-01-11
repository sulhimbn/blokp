package com.example.iurankomplek.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Migration 19 Test Suite
 *
 * Tests:
 * - CHECK constraints added to users table
 * - CHECK constraints added to financial_records table
 * - CHECK constraints added to transactions table
 * - Data integrity preserved during migration
 * - Invalid data rejected by CHECK constraints
 * - Rollback (Migration19Down) works correctly
 */
@RunWith(AndroidJUnit4::class)
class Migration19Test {
    private lateinit var helper: MigrationTestHelper
    private lateinit var db: androidx.room.RoomDatabase

    @Before
    fun setUp() {
        helper = MigrationTestHelper(
            ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java.canonicalName,
            androidx.arch.core.executor.testing.InstantTaskExecutorRule()
        )
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        helper.closeAllDatabases()
    }

    @Test
    fun migration19_should_create_check_constraints_for_users_table() {
        val db = helper.createDatabase("test-db", 19)

        val cursor = db.query("SELECT sql FROM sqlite_master WHERE type='table' AND name='users'")
        cursor.moveToFirst()
        val tableSql = cursor.getString(0)

        cursor.close()
        db.close()

        assert(tableSql.contains("CHECK"))
        assert(tableSql.contains("email LIKE '%@%'"))
        assert(tableSql.contains("length(first_name) > 0"))
        assert(tableSql.contains("length(last_name) > 0"))
        assert(tableSql.contains("length(alamat) > 0"))
    }

    @Test
    fun migration19_should_create_check_constraints_for_financial_records_table() {
        val db = helper.createDatabase("test-db", 19)

        val cursor = db.query("SELECT sql FROM sqlite_master WHERE type='table' AND name='financial_records'")
        cursor.moveToFirst()
        val tableSql = cursor.getString(0)

        cursor.close()
        db.close()

        assert(tableSql.contains("CHECK"))
        assert(tableSql.contains("user_id > 0"))
        assert(tableSql.contains("iuran_perwarga >= 0"))
        assert(tableSql.contains("total_iuran_rekap >= 0"))
        assert(tableSql.contains("length(pemanfaatan_iuran) > 0"))
    }

    @Test
    fun migration19_should_create_check_constraints_for_transactions_table() {
        val db = helper.createDatabase("test-db", 19)

        val cursor = db.query("SELECT sql FROM sqlite_master WHERE type='table' AND name='transactions'")
        cursor.moveToFirst()
        val tableSql = cursor.getString(0)

        cursor.close()
        db.close()

        assert(tableSql.contains("CHECK"))
        assert(tableSql.contains("amount > 0"))
        assert(tableSql.contains("status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED')"))
        assert(tableSql.contains("payment_method IN ('CREDIT_CARD', 'BANK_TRANSFER', 'E_WALLET', 'VIRTUAL_ACCOUNT')"))
        assert(tableSql.contains("length(description) > 0"))
    }

    @Test
    fun migration19_should_preserve_existing_data() {
        val db18 = helper.createDatabase("test-db", 18)

        db18.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted, created_at, updated_at) VALUES ('test@example.com', 'John', 'Doe', 'Address', 'avatar.jpg', 0, 1000000, 1000000)")
        db18.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted, created_at, updated_at) VALUES (1, 100, 100, 100, 100, 100, 'Test', 0, 1000000, 1000000)")
        db18.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('test-id-123', 1, 100.50, 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Test transaction', 0, 1000000, 1000000, '')")

        db18.close()

        val db19 = helper.runMigrationsAndValidate("test-db", 19, true, Migration19())

        val userCursor = db19.query("SELECT * FROM users WHERE email = 'test@example.com'")
        assert(userCursor.count == 1)
        userCursor.close()

        val financialCursor = db19.query("SELECT * FROM financial_records WHERE user_id = 1")
        assert(financialCursor.count == 1)
        financialCursor.close()

        val transactionCursor = db19.query("SELECT * FROM transactions WHERE id = 'test-id-123'")
        assert(transactionCursor.count == 1)
        transactionCursor.close()

        db19.close()
    }

    @Test(expected = Exception::class)
    fun migration19_should_reject_invalid_email_format() {
        val db = helper.createDatabase("test-db", 19)

        try {
            db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted, created_at, updated_at) VALUES ('invalid-email', 'John', 'Doe', 'Address', 'avatar.jpg', 0, 1000000, 1000000)")
        } finally {
            db.close()
        }
    }

    @Test(expected = Exception::class)
    fun migration19_should_reject_empty_first_name() {
        val db = helper.createDatabase("test-db", 19)

        try {
            db.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted, created_at, updated_at) VALUES ('test@example.com', '', 'Doe', 'Address', 'avatar.jpg', 0, 1000000, 1000000)")
        } finally {
            db.close()
        }
    }

    @Test(expected = Exception::class)
    fun migration19_should_reject_negative_financial_values() {
        val db = helper.createDatabase("test-db", 19)

        try {
            db.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted, created_at, updated_at) VALUES (1, -100, 100, 100, 100, 100, 'Test', 0, 1000000, 1000000)")
        } finally {
            db.close()
        }
    }

    @Test(expected = Exception::class)
    fun migration19_should_reject_zero_or_negative_transaction_amount() {
        val db = helper.createDatabase("test-db", 19)

        try {
            db.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('test-id-123', 1, 0, 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Test transaction', 0, 1000000, 1000000, '')")
        } finally {
            db.close()
        }
    }

    @Test(expected = Exception::class)
    fun migration19_should_reject_invalid_transaction_status() {
        val db = helper.createDatabase("test-db", 19)

        try {
            db.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('test-id-123', 1, 100.50, 'IDR', 'INVALID_STATUS', 'BANK_TRANSFER', 'Test transaction', 0, 1000000, 1000000, '')")
        } finally {
            db.close()
        }
    }

    @Test(expected = Exception::class)
    fun migration19_should_reject_invalid_payment_method() {
        val db = helper.createDatabase("test-db", 19)

        try {
            db.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('test-id-123', 1, 100.50, 'IDR', 'COMPLETED', 'INVALID_METHOD', 'Test transaction', 0, 1000000, 1000000, '')")
        } finally {
            db.close()
        }
    }

    @Test
    fun migration19Down_should_remove_check_constraints() {
        val db18 = helper.createDatabase("test-db", 18)

        db18.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted, created_at, updated_at) VALUES ('test@example.com', 'John', 'Doe', 'Address', 'avatar.jpg', 0, 1000000, 1000000)")
        db18.close()

        val db19 = helper.runMigrationsAndValidate("test-db", 19, true, Migration19())
        db19.close()

        val db18Again = helper.runMigrationsAndValidate("test-db", 18, true, Migration19Down())

        val cursor = db18Again.query("SELECT sql FROM sqlite_master WHERE type='table' AND name='users'")
        cursor.moveToFirst()
        val tableSql = cursor.getString(0)

        cursor.close()
        db18Again.close()

        assert(!tableSql.contains("CHECK"))
    }

    @Test
    fun migration19Down_should_preserve_existing_data() {
        val db18 = helper.createDatabase("test-db", 18)

        db18.execSQL("INSERT INTO users (email, first_name, last_name, alamat, avatar, is_deleted, created_at, updated_at) VALUES ('test@example.com', 'John', 'Doe', 'Address', 'avatar.jpg', 0, 1000000, 1000000)")
        db18.execSQL("INSERT INTO financial_records (user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted, created_at, updated_at) VALUES (1, 100, 100, 100, 100, 100, 'Test', 0, 1000000, 1000000)")
        db18.execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('test-id-123', 1, 100.50, 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Test transaction', 0, 1000000, 1000000, '')")
        db18.close()

        val db19 = helper.runMigrationsAndValidate("test-db", 19, true, Migration19())
        db19.close()

        val db18Again = helper.runMigrationsAndValidate("test-db", 18, true, Migration19Down())

        val userCursor = db18Again.query("SELECT * FROM users WHERE email = 'test@example.com'")
        assert(userCursor.count == 1)
        userCursor.close()

        val financialCursor = db18Again.query("SELECT * FROM financial_records WHERE user_id = 1")
        assert(financialCursor.count == 1)
        financialCursor.close()

        val transactionCursor = db18Again.query("SELECT * FROM transactions WHERE id = 'test-id-123'")
        assert(transactionCursor.count == 1)
        transactionCursor.close()

        db18Again.close()
    }
}
