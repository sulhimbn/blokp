package com.example.iurankomplek.data.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.iurankomplek.data.dao.FinancialRecordDao
import com.example.iurankomplek.data.dao.TransactionDao
import com.example.iurankomplek.data.dao.UserDao
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test Migration22: Remove Redundant Full Indexes (Cleanup)
 *
 * Tests:
 * 1. Index drop: Redundant full indexes dropped successfully
 * 2. Partial indexes preserved: All partial indexes still exist
 * 3. Data integrity: All existing data preserved
 * 4. Query performance: Queries still work correctly with partial indexes
 * 5. Rollback: Migration22Down recreates dropped indexes
 */
@RunWith(AndroidJUnit4::class)
class Migration22Test {

    private lateinit var dbVersion21: AppDatabase
    private lateinit var dbVersion22: AppDatabase
    private lateinit var dbVersion21Down: AppDatabase

    private lateinit var userDao: UserDao
    private lateinit var financialRecordDao: FinancialRecordDao
    private lateinit var transactionDao: TransactionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        dbVersion21 = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(*getMigrationsUpTo21())
            .build()

        dbVersion22 = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(*getMigrationsUpTo22())
            .build()

        dbVersion21Down = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(*getMigrationsUpTo21(), Migration22(), Migration22Down)
            .build()

        userDao = dbVersion21.userDao()
        financialRecordDao = dbVersion21.financialRecordDao()
        transactionDao = dbVersion21.transactionDao()
    }

    @After
    fun tearDown() {
        dbVersion21.close()
        dbVersion22.close()
        dbVersion21Down.close()
    }

    @Test
    fun migrate21_to22_dropsRedundantUserIndexes() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(Migration22())
            .build()

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='users'", null)
            .use { cursor ->
                val indexNames = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    indexNames.add(cursor.getString(0))
                }
                // Partial indexes should exist
                assertTrue(indexNames.contains("idx_users_active"), "idx_users_active should exist")
                assertTrue(indexNames.contains("idx_users_name_active"), "idx_users_name_active should exist")
                assertTrue(indexNames.contains("idx_users_email_active"), "idx_users_email_active should exist")
                // Full index should be dropped
                assertFalse(indexNames.contains("index_users_last_name_first_name"), "index_users_last_name_first_name should be dropped")
            }

        db.close()
    }

    @Test
    fun migrate21_to22_dropsRedundantFinancialRecordIndexes() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(Migration22())
            .build()

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='financial_records'", null)
            .use { cursor ->
                val indexNames = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    indexNames.add(cursor.getString(0))
                }
                // Partial indexes should exist
                assertTrue(indexNames.contains("idx_financial_records_active"), "idx_financial_records_active should exist")
                assertTrue(indexNames.contains("idx_financial_records_user_updated_active"), "idx_financial_records_user_updated_active should exist")
                // Full index should be dropped
                assertFalse(indexNames.contains("index_financial_records_user_id_updated_at"), "index_financial_records_user_id_updated_at should be dropped")
            }

        db.close()
    }

    @Test
    fun migrate21_to22_dropsRedundantTransactionIndexes() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(Migration22())
            .build()

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='transactions'", null)
            .use { cursor ->
                val indexNames = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    indexNames.add(cursor.getString(0))
                }
                // Partial indexes should exist
                assertTrue(indexNames.contains("idx_transactions_active"), "idx_transactions_active should exist")
                assertTrue(indexNames.contains("idx_transactions_user_id_active"), "idx_transactions_user_id_active should exist")
                assertTrue(indexNames.contains("idx_transactions_status_active"), "idx_transactions_status_active should exist")
                // Full indexes should be dropped
                assertFalse(indexNames.contains("index_transactions_user_id"), "index_transactions_user_id should be dropped")
                assertFalse(indexNames.contains("index_transactions_status"), "index_transactions_status should be dropped")
                assertFalse(indexNames.contains("index_transactions_user_id_status"), "index_transactions_user_id_status should be dropped")
                assertFalse(indexNames.contains("index_transactions_created_at"), "index_transactions_created_at should be dropped")
                assertFalse(indexNames.contains("index_transactions_updated_at"), "index_transactions_updated_at should be dropped")
            }

        db.close()
    }

    @Test
    fun migrate21_to22_preservesUserData() = runBlocking {
        val testUser = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "Test Address",
            avatar = "https://example.com/avatar.jpg"
        )

        val insertedId = userDao.insert(testUser)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val migratedDb = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(Migration22())
            .build()

        val retrievedUser = migratedDb.userDao().getUserById(insertedId)

        assertNotNull(retrievedUser)
        assertEquals(insertedId, retrievedUser?.id)
        assertEquals("test@example.com", retrievedUser?.email)
        assertEquals("John", retrievedUser?.firstName)
        assertEquals("Doe", retrievedUser?.lastName)
        assertEquals(false, retrievedUser?.isDeleted)

        migratedDb.close()
    }

    @Test
    fun migrate21_to22_preservesFinancialRecordData() = runBlocking {
        val testUser = UserEntity(
            email = "financial@example.com",
            firstName = "Jane",
            lastName = "Smith",
            alamat = "Financial Test Address",
            avatar = "https://example.com/financial.jpg"
        )

        val userId = userDao.insert(testUser)

        val testRecord = FinancialRecordEntity(
            userId = userId,
            iuranPerwarga = 100000,
            jumlahIuranBulanan = 50000,
            totalIuranIndividu = 150000,
            pengeluaranIuranWarga = 20000,
            totalIuranRekap = 430000,
            pemanfaatanIuran = "Test pemanfaatan"
        )

        val recordId = financialRecordDao.insert(testRecord)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val migratedDb = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(Migration22())
            .build()

        val retrievedRecord = migratedDb.financialRecordDao().getFinancialRecordById(recordId)

        assertNotNull(retrievedRecord)
        assertEquals(recordId, retrievedRecord?.id)
        assertEquals(userId, retrievedRecord?.userId)
        assertEquals(100000, retrievedRecord?.iuranPerwarga)
        assertEquals(430000, retrievedRecord?.totalIuranRekap)
        assertEquals("Test pemanfaatan", retrievedRecord?.pemanfaatanIuran)
        assertEquals(false, retrievedRecord?.isDeleted)

        migratedDb.close()
    }

    @Test
    fun migrate21_to22_preservesTransactionData() = runBlocking {
        val testUser = UserEntity(
            email = "transaction@example.com",
            firstName = "Bob",
            lastName = "Wilson",
            alamat = "Transaction Test Address",
            avatar = "https://example.com/transaction.jpg"
        )

        val userId = userDao.insert(testUser)

        val testTransaction = Transaction(
            id = "txn-test-001",
            userId = userId,
            amount = 15000L, // 150.00 IDR in cents
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            description = "Test transaction"
        )

        transactionDao.insert(testTransaction)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val migratedDb = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(Migration22())
            .build()

        val retrievedTransaction = migratedDb.transactionDao().getTransactionById("txn-test-001")

        assertNotNull(retrievedTransaction)
        assertEquals("txn-test-001", retrievedTransaction?.id)
        assertEquals(userId, retrievedTransaction?.userId)
        assertEquals(15000L, retrievedTransaction?.amount)
        assertEquals(PaymentStatus.COMPLETED, retrievedTransaction?.status)
        assertEquals(PaymentMethod.BANK_TRANSFER, retrievedTransaction?.paymentMethod)
        assertEquals(false, retrievedTransaction?.isDeleted)

        migratedDb.close()
    }

    @Test
    fun migrate21_to22_queriesWorkWithPartialIndexes() = runBlocking {
        val users = (1..5).map { i ->
            UserEntity(
                email = "user$i@example.com",
                firstName = "User",
                lastName = "$i",
                alamat = "Address $i",
                avatar = "https://example.com/avatar$i.jpg"
            )
        }

        userDao.insertAll(users)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val migratedDb = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(Migration22())
            .build()

        val allUsers = migratedDb.userDao().getAllUsers().first()

        assertEquals(5, allUsers.size)
        val sortedUsers = allUsers.sortedBy { "${it.lastName}${it.firstName}" }
        assertEquals("User1", sortedUsers[0].firstName)
        assertEquals("User5", sortedUsers[4].firstName)

        migratedDb.close()
    }

    @Test
    fun migrate21_to22_softDeleteFilteringWorks() = runBlocking {
        val users = (1..3).map { i ->
            UserEntity(
                email = "softdelete$i@example.com",
                firstName = "User",
                lastName = "SD$i",
                alamat = "Address $i",
                avatar = "https://example.com/sd$i.jpg"
            )
        }

        val insertedIds = userDao.insertAll(users)
        userDao.softDeleteById(insertedIds[0])

        val context = ApplicationProvider.getApplicationContext<Context>()
        val migratedDb = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(Migration22())
            .build()

        val activeUsers = migratedDb.userDao().getAllUsers().first()
        assertEquals(2, activeUsers.size)

        val deletedUsers = migratedDb.userDao().getDeletedUsers().first()
        assertEquals(1, deletedUsers.size)

        migratedDb.close()
    }

    @Test
    fun rollback_migration22_recreatesDroppedIndexes() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(Migration22(), Migration22Down)
            .build()

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='users'", null)
            .use { cursor ->
                val indexNames = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    indexNames.add(cursor.getString(0))
                }
                // Dropped index should be recreated
                assertTrue(indexNames.contains("index_users_last_name_first_name"), "index_users_last_name_first_name should be recreated")
            }

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='financial_records'", null)
            .use { cursor ->
                val indexNames = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    indexNames.add(cursor.getString(0))
                }
                // Dropped index should be recreated
                assertTrue(indexNames.contains("index_financial_records_user_id_updated_at"), "index_financial_records_user_id_updated_at should be recreated")
            }

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='transactions'", null)
            .use { cursor ->
                val indexNames = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    indexNames.add(cursor.getString(0))
                }
                // Dropped indexes should be recreated
                assertTrue(indexNames.contains("index_transactions_user_id"), "index_transactions_user_id should be recreated")
                assertTrue(indexNames.contains("index_transactions_status"), "index_transactions_status should be recreated")
                assertTrue(indexNames.contains("index_transactions_user_id_status"), "index_transactions_user_id_status should be recreated")
                assertTrue(indexNames.contains("index_transactions_created_at"), "index_transactions_created_at should be recreated")
                assertTrue(indexNames.contains("index_transactions_updated_at"), "index_transactions_updated_at should be recreated")
            }

        db.close()
    }

    @Test
    fun rollback_migration22_preservesData() = runBlocking {
        val testUser = UserEntity(
            email = "rollback@example.com",
            firstName = "Rollback",
            lastName = "Test",
            alamat = "Rollback Address",
            avatar = "https://example.com/rollback.jpg"
        )

        val insertedId = userDao.insert(testUser)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val rolledBackDb = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addMigrations(Migration22(), Migration22Down)
            .build()

        val retrievedUser = rolledBackDb.userDao().getUserById(insertedId)

        assertNotNull(retrievedUser)
        assertEquals("rollback@example.com", retrievedUser?.email)
        assertEquals("Rollback", retrievedUser?.firstName)

        rolledBackDb.close()
    }

    private fun getMigrationsUpTo21(): Array<androidx.room.migration.Migration> {
        return arrayOf(
            Migration1(), Migration1Down, Migration2, Migration2Down,
            Migration3(), Migration3Down, Migration4(), Migration4Down,
            Migration5(), Migration5Down, Migration6(), Migration6Down,
            Migration7(), Migration7Down, Migration8(), Migration8Down,
            Migration9(), Migration9Down, Migration10(), Migration10Down,
            Migration11(), Migration11Down, Migration12(), Migration12Down,
            Migration13(), Migration13Down, Migration14(), Migration14Down,
            Migration15(), Migration15Down, Migration16(), Migration16Down,
            Migration17(), Migration17Down, Migration18(), Migration18Down,
            Migration19(), Migration19Down, Migration20(), Migration20Down,
            Migration21(), Migration21Down
        )
    }

    private fun getMigrationsUpTo22(): Array<androidx.room.migration.Migration> {
        return arrayOf(
            Migration1(), Migration1Down, Migration2, Migration2Down,
            Migration3(), Migration3Down, Migration4(), Migration4Down,
            Migration5(), Migration5Down, Migration6(), Migration6Down,
            Migration7(), Migration7Down, Migration8(), Migration8Down,
            Migration9(), Migration9Down, Migration10(), Migration10Down,
            Migration11(), Migration11Down, Migration12(), Migration12Down,
            Migration13(), Migration13Down, Migration14(), Migration14Down,
            Migration15(), Migration15Down, Migration16(), Migration16Down,
            Migration17(), Migration17Down, Migration18(), Migration18Down,
            Migration19(), Migration19Down, Migration20(), Migration20Down,
            Migration21(), Migration21Down, Migration22()
        )
    }

    private fun <T> Any.getDatabase(): SupportSQLiteDatabase {
        return (this as AppDatabase).openHelper.writableDatabase
    }
}
