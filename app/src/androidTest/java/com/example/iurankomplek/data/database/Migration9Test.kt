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

@RunWith(AndroidJUnit4::class)
class Migration9Test {
    private val TEST_DB = "migration-test"
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )
    
    @Before
    fun setup() {
        // Helper will create database at version 8
    }
    
    @After
    fun tearDown() {
        helper.closeAllDatabases()
    }
    
    @Test
    fun migrate8To9_dropsFullIndexes() {
        // Create database at version 8 with full indexes
        var db = helper.createDatabase(TEST_DB, 8).apply {
            // Simulate full indexes created by entity annotations
            execSQL("CREATE INDEX IF NOT EXISTS index_transactions_user_id ON transactions(user_id)")
            execSQL("CREATE INDEX IF NOT EXISTS index_transactions_status ON transactions(status)")
            execSQL("CREATE INDEX IF NOT EXISTS index_transactions_user_id_status ON transactions(user_id, status)")
            execSQL("CREATE INDEX IF NOT EXISTS index_transactions_created_at ON transactions(created_at)")
            execSQL("CREATE INDEX IF NOT EXISTS index_transactions_updated_at ON transactions(updated_at)")
            
            // Insert test data
            execSQL("INSERT INTO users (id, email, first_name, last_name, alamat, avatar, is_deleted) VALUES (1, 'test@test.com', 'Test', 'User', 'Address', 'avatar.jpg', 0)")
            execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted) VALUES ('tx1', 1, 100000, 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Test payment', 0)")
            execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted) VALUES ('tx2', 1, 200000, 'IDR', 'PENDING', 'E_WALLET', 'Test payment 2', 1)")
            
            close()
        }
        
        // Run migration
        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, Migration9)
        
        // Verify full indexes are dropped and partial indexes exist
        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name LIKE 'idx_transactions_%'").use { cursor ->
            val indexNames = mutableListOf<String>()
            while (cursor.moveToNext()) {
                indexNames.add(cursor.getString(0))
            }
            // Should only have partial indexes
            assert(indexNames.contains("idx_transactions_user_id"))
            assert(indexNames.contains("idx_transactions_status"))
            assert(indexNames.contains("idx_transactions_user_status"))
            assert(indexNames.contains("idx_transactions_created"))
            assert(indexNames.contains("idx_transactions_updated"))
            // Should not have full indexes (old naming)
            assert(!indexNames.contains("index_transactions_user_id"))
            assert(!indexNames.contains("index_transactions_status"))
        }
    }
    
    @Test
    fun migrate8To9_partialIndexesWorkCorrectly() {
        var db = helper.createDatabase(TEST_DB, 8).apply {
            // Insert test data with mix of active and deleted
            execSQL("INSERT INTO users (id, email, first_name, last_name, alamat, avatar, is_deleted) VALUES (1, 'test@test.com', 'Test', 'User', 'Address', 'avatar.jpg', 0)")
            
            execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted) VALUES ('tx1', 1, 100000, 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Test 1', 0)")
            execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted) VALUES ('tx2', 1, 200000, 'IDR', 'PENDING', 'E_WALLET', 'Test 2', 0)")
            execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted) VALUES ('tx3', 1, 300000, 'IDR', 'COMPLETED', 'CREDIT_CARD', 'Deleted', 1)")
            
            close()
        }
        
        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, Migration9)
        
        // Verify queries using partial indexes return correct results
        db.query("SELECT COUNT(*) FROM transactions WHERE is_deleted = 0").use { cursor ->
            cursor.moveToFirst()
            val activeTransactions = cursor.getInt(0)
            assert(activeTransactions == 2) // Only active transactions
        }
        
        db.query("SELECT COUNT(*) FROM transactions WHERE status = 'COMPLETED' AND is_deleted = 0").use { cursor ->
            cursor.moveToFirst()
            val completedTransactions = cursor.getInt(0)
            assert(completedTransactions == 1) // Only one COMPLETED and not deleted
        }
    }
    
    @Test
    fun migrate8To9_preservesExistingData() {
        var db = helper.createDatabase(TEST_DB, 8).apply {
            // Insert comprehensive test data
            execSQL("INSERT INTO users (id, email, first_name, last_name, alamat, avatar, is_deleted, created_at, updated_at) VALUES (1, 'test@test.com', 'Test', 'User', '123 Main St', 'avatar.jpg', 0, 1000, 2000)")
            execSQL("INSERT INTO users (id, email, first_name, last_name, alamat, avatar, is_deleted, created_at, updated_at) VALUES (2, 'test2@test.com', 'Test2', 'User2', '456 Oak St', 'avatar2.jpg', 1, 1500, 2500)")
            
            execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('tx1', 1, 100000, 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Payment 1', 0, 1000, 2000, 'key1=value1')")
            execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted, created_at, updated_at, metadata) VALUES ('tx2', 2, 200000, 'IDR', 'PENDING', 'E_WALLET', 'Payment 2', 1, 1500, 2500, 'key2=value2')")
            
            close()
        }
        
        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, Migration9)
        
        // Verify all data preserved
        db.query("SELECT * FROM transactions").use { cursor ->
            val count = cursor.count
            assert(count == 2) // All transactions preserved
            
            // Check specific transaction data
            cursor.moveToFirst()
            assert(cursor.getString(cursor.getColumnIndexOrThrow("id")) == "tx1")
            assert(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")) == 1)
            assert(cursor.getString(cursor.getColumnIndexOrThrow("status")) == "COMPLETED")
            
            cursor.moveToNext()
            assert(cursor.getString(cursor.getColumnIndexOrThrow("id")) == "tx2")
            assert(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")) == 2)
            assert(cursor.getString(cursor.getColumnIndexOrThrow("status")) == "PENDING")
        }
    }
    
    @Test
    fun migrate8To9_handlesEmptyDatabase() {
        var db = helper.createDatabase(TEST_DB, 8).apply {
            // No data, just schema
            close()
        }
        
        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, Migration9)
        
        // Verify schema is correct
        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name LIKE 'idx_transactions_%'").use { cursor ->
            val indexNames = mutableListOf<String>()
            while (cursor.moveToNext()) {
                indexNames.add(cursor.getString(0))
            }
            // Should have all partial indexes
            assert(indexNames.contains("idx_transactions_user_id"))
            assert(indexNames.contains("idx_transactions_status"))
            assert(indexNames.contains("idx_transactions_user_status"))
            assert(indexNames.contains("idx_transactions_created"))
            assert(indexNames.contains("idx_transactions_updated"))
        }
    }
    
    @Test
    fun migrate9To8_revertsPartialIndexes() {
        // Create database at version 9 with partial indexes
        var db = helper.createDatabase(TEST_DB, 9).apply {
            // Insert test data
            execSQL("INSERT INTO users (id, email, first_name, last_name, alamat, avatar, is_deleted) VALUES (1, 'test@test.com', 'Test', 'User', 'Address', 'avatar.jpg', 0)")
            execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted) VALUES ('tx1', 1, 100000, 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Test', 0)")
            
            close()
        }
        
        // Run down migration
        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, Migration9Down)
        
        // Verify full indexes are restored
        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name LIKE 'index_transactions_%'").use { cursor ->
            val indexNames = mutableListOf<String>()
            while (cursor.moveToNext()) {
                indexNames.add(cursor.getString(0))
            }
            // Should have full indexes (old naming)
            assert(indexNames.contains("index_transactions_user_id"))
            assert(indexNames.contains("index_transactions_status"))
            assert(indexNames.contains("index_transactions_user_id_status"))
            assert(indexNames.contains("index_transactions_created_at"))
            assert(indexNames.contains("index_transactions_updated_at"))
        }
        
        // Verify partial indexes are dropped
        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name LIKE 'idx_transactions_%'").use { cursor ->
            val count = cursor.count
            assert(count == 0) // No partial indexes after rollback
        }
    }
    
    @Test
    fun migrate8To9_preservesTransactionIntegrity() {
        var db = helper.createDatabase(TEST_DB, 8).apply {
            // Insert users and transactions with foreign key relationship
            execSQL("INSERT INTO users (id, email, first_name, last_name, alamat, avatar, is_deleted) VALUES (1, 'user1@test.com', 'User1', 'Test', 'Address1', 'avatar1.jpg', 0)")
            execSQL("INSERT INTO users (id, email, first_name, last_name, alamat, avatar, is_deleted) VALUES (2, 'user2@test.com', 'User2', 'Test', 'Address2', 'avatar2.jpg', 0)")
            
            // Active transactions
            execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted) VALUES ('tx_active_1', 1, 100000, 'IDR', 'COMPLETED', 'BANK_TRANSFER', 'Active payment 1', 0)")
            execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted) VALUES ('tx_active_2', 2, 200000, 'IDR', 'PENDING', 'E_WALLET', 'Active payment 2', 0)")
            
            // Deleted transactions
            execSQL("INSERT INTO transactions (id, user_id, amount, currency, status, payment_method, description, is_deleted) VALUES ('tx_deleted_1', 1, 300000, 'IDR', 'FAILED', 'CREDIT_CARD', 'Deleted payment', 1)")
            
            close()
        }
        
        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, Migration9)
        
        // Verify foreign key relationships preserved
        db.query("SELECT t.id, t.user_id, u.email FROM transactions t JOIN users u ON t.user_id = u.id WHERE t.is_deleted = 0").use { cursor ->
            val count = cursor.count
            assert(count == 2) // Two active transactions
            
            cursor.moveToFirst()
            assert(cursor.getString(cursor.getColumnIndexOrThrow("email")) == "user1@test.com")
            
            cursor.moveToNext()
            assert(cursor.getString(cursor.getColumnIndexOrThrow("email")) == "user2@test.com")
        }
        
        // Verify deleted transactions excluded
        db.query("SELECT COUNT(*) FROM transactions WHERE user_id = 1 AND is_deleted = 0").use { cursor ->
            cursor.moveToFirst()
            val activeUser1Transactions = cursor.getInt(0)
            assert(activeUser1Transactions == 1) // Only one active for user 1
        }
    }
}
