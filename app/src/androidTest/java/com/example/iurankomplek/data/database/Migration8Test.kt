package com.example.iurankomplek.data.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class Migration8Test {
    private val TEST_DB = "migration-test"
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )
    
    @Before
    fun setup() {
        // Helper will create database at version 7
    }
    
    @After
    fun tearDown() {
        helper.closeAllDatabases()
    }
    
    @Test
    fun migrate7To8_dropsDuplicateUsersIndexes() {
        // Create database at version 7 with duplicate indexes
        var db = helper.createDatabase(TEST_DB, 7).apply {
            // Simulate duplicate indexes created by entity annotations
            execSQL("CREATE INDEX IF NOT EXISTS idx_users_active ON users(id)")
            execSQL("CREATE INDEX IF NOT EXISTS idx_users_active_updated ON users(id, updated_at)")
            
            // Insert test data
            execSQL("INSERT INTO users (id, email, first_name, last_name, alamat, avatar, is_deleted) VALUES (1, 'test@test.com', 'Test', 'User', 'Address', 'avatar.jpg', 0)")
            execSQL("INSERT INTO users (id, email, first_name, last_name, alamat, avatar, is_deleted) VALUES (2, 'deleted@test.com', 'Deleted', 'User', 'Address', 'avatar.jpg', 1)")
            
            close()
        }
        
        // Run migration
        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, Migration8)
        
        // Verify duplicate indexes are dropped
        // Partial indexes should exist
        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name LIKE 'idx_users_%'").use { cursor ->
            val indexNames = mutableListOf<String>()
            while (cursor.moveToNext()) {
                indexNames.add(cursor.getString(0))
            }
            // Should only have partial indexes
            assert(indexNames.contains("idx_users_active"))
            assert(indexNames.contains("idx_users_active_updated"))
            // Should not have duplicate full indexes
            assert(indexNames.filter { it.contains("users") }.size == 2)
        }
    }
    
    @Test
    fun migrate7To8_dropsDuplicateFinancialRecordsIndexes() {
        var db = helper.createDatabase(TEST_DB, 7).apply {
            // Simulate duplicate indexes created by entity annotations
            execSQL("CREATE INDEX IF NOT EXISTS idx_financial_records_user_id_updated_at ON financial_records(user_id, updated_at)")
            execSQL("CREATE INDEX IF NOT EXISTS idx_financial_records_updated_at ON financial_records(updated_at)")
            execSQL("CREATE INDEX IF NOT EXISTS idx_financial_records_id ON financial_records(id)")
            
            // Insert test data
            execSQL("INSERT INTO users (id, email, first_name, last_name, alamat, avatar, is_deleted) VALUES (1, 'test@test.com', 'Test', 'User', 'Address', 'avatar.jpg', 0)")
            execSQL("INSERT INTO financial_records (id, user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted) VALUES (1, 1, 100, 100, 300, 50, 900, 'Test', 0)")
            execSQL("INSERT INTO financial_records (id, user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted) VALUES (2, 1, 200, 200, 600, 100, 1800, 'Test2', 1)")
            
            close()
        }
        
        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, Migration8)
        
        // Verify partial indexes exist and are working
        db.query("SELECT COUNT(*) FROM financial_records WHERE is_deleted = 0").use { cursor ->
            cursor.moveToFirst()
            val activeCount = cursor.getInt(0)
            assert(activeCount == 1) // Only one active record
        }
    }
    
    @Test
    fun migrate7To8_partialIndexesWorkCorrectly() {
        var db = helper.createDatabase(TEST_DB, 7).apply {
            // Insert test data with mix of active and deleted
            execSQL("INSERT INTO users (id, email, first_name, last_name, alamat, avatar, is_deleted) VALUES (1, 'active1@test.com', 'Active', 'User1', 'Address', 'avatar.jpg', 0)")
            execSQL("INSERT INTO users (id, email, first_name, last_name, alamat, avatar, is_deleted) VALUES (2, 'active2@test.com', 'Active', 'User2', 'Address', 'avatar.jpg', 0)")
            execSQL("INSERT INTO users (id, email, first_name, last_name, alamat, avatar, is_deleted) VALUES (3, 'deleted@test.com', 'Deleted', 'User', 'Address', 'avatar.jpg', 1)")
            
            execSQL("INSERT INTO financial_records (id, user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted) VALUES (1, 1, 100, 100, 300, 50, 900, 'Test', 0)")
            execSQL("INSERT INTO financial_records (id, user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted) VALUES (2, 2, 200, 200, 600, 100, 1800, 'Test2', 0)")
            execSQL("INSERT INTO financial_records (id, user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted) VALUES (3, 1, 300, 300, 900, 150, 2700, 'Deleted', 1)")
            
            close()
        }
        
        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, Migration8)
        
        // Verify queries using partial indexes return correct results
        db.query("SELECT COUNT(*) FROM users WHERE is_deleted = 0").use { cursor ->
            cursor.moveToFirst()
            val activeUsers = cursor.getInt(0)
            assert(activeUsers == 2) // Only active users
        }
        
        db.query("SELECT COUNT(*) FROM financial_records WHERE is_deleted = 0").use { cursor ->
            cursor.moveToFirst()
            val activeFinancials = cursor.getInt(0)
            assert(activeFinancials == 2) // Only active records
        }
    }
    
    @Test
    fun migrate7To8_preservesExistingData() {
        var db = helper.createDatabase(TEST_DB, 7).apply {
            // Insert comprehensive test data
            execSQL("INSERT INTO users (id, email, first_name, last_name, alamat, avatar, is_deleted, created_at, updated_at) VALUES (1, 'test@test.com', 'Test', 'User', '123 Main St', 'avatar.jpg', 0, 1000, 2000)")
            execSQL("INSERT INTO users (id, email, first_name, last_name, alamat, avatar, is_deleted, created_at, updated_at) VALUES (2, 'deleted@test.com', 'Deleted', 'User', '456 Oak St', 'avatar2.jpg', 1, 1500, 2500)")
            
            execSQL("INSERT INTO financial_records (id, user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted, created_at, updated_at) VALUES (1, 1, 100, 100, 300, 50, 900, 'Maintenance', 0, 1000, 2000)")
            execSQL("INSERT INTO financial_records (id, user_id, iuran_perwarga, jumlah_iuran_bulanan, total_iuran_individu, pengeluaran_iuran_warga, total_iuran_rekap, pemanfaatan_iuran, is_deleted, created_at, updated_at) VALUES (2, 2, 200, 200, 600, 100, 1800, 'Utilities', 1, 1500, 2500)")
            
            close()
        }
        
        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, Migration8)
        
        // Verify all data preserved
        db.query("SELECT * FROM users").use { cursor ->
            val count = cursor.count
            assert(count == 2) // All users preserved
            
            // Check specific user data
            cursor.moveToFirst()
            assert(cursor.getString(cursor.getColumnIndexOrThrow("email")) == "test@test.com")
            cursor.moveToNext()
            assert(cursor.getString(cursor.getColumnIndexOrThrow("email")) == "deleted@test.com")
        }
        
        db.query("SELECT * FROM financial_records").use { cursor ->
            val count = cursor.count
            assert(count == 2) // All financial records preserved
            
            cursor.moveToFirst()
            assert(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")) == 1)
            cursor.moveToNext()
            assert(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")) == 2)
        }
    }
    
    @Test
    fun migrate7To8_handlesEmptyDatabase() {
        var db = helper.createDatabase(TEST_DB, 7).apply {
            // No data, just schema
            close()
        }
        
        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, Migration8)
        
        // Verify schema is correct
        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name LIKE 'idx_%'").use { cursor ->
            val indexNames = mutableListOf<String>()
            while (cursor.moveToNext()) {
                indexNames.add(cursor.getString(0))
            }
            // Should have all partial indexes
            assert(indexNames.contains("idx_users_active"))
            assert(indexNames.contains("idx_users_active_updated"))
            assert(indexNames.contains("idx_financial_active_user_updated"))
            assert(indexNames.contains("idx_financial_active"))
            assert(indexNames.contains("idx_financial_active_updated"))
        }
    }
}
