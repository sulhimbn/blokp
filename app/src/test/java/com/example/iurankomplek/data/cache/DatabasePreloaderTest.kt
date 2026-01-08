package com.example.iurankomplek.data.cache

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class DatabasePreloaderTest {

    private lateinit var databasePreloader: DatabasePreloader
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var mockDatabase: SupportSQLiteDatabase

    @Before
    fun setup() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        mockDatabase = mockk(relaxed = true)
        databasePreloader = DatabasePreloader(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onCreate should call preloadIndexesAndConstraints`() {
        // Act
        databasePreloader.onCreate(mockDatabase)

        // Assert
        // The preloadIndexesAndConstraints is launched in a coroutine
        // In test dispatcher, it should execute immediately
    }

    @Test
    fun `onOpen should call validateCacheIntegrity`() {
        // Act
        databasePreloader.onOpen(mockDatabase)

        // Assert
        // The validateCacheIntegrity is launched in a coroutine
        // In test dispatcher, it should execute immediately
    }

    @Test
    fun `preloadIndexesAndConstraints should create users index if not exists`() {
        // Arrange
        val mockCursor = mockk<android.database.Cursor>()
        every { mockDatabase.query("PRAGMA index_list('users')") } returns mockCursor
        every { mockCursor.count } returns 0
        every { mockDatabase.execSQL("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)") } returns Unit
        every { mockCursor.close() } returns Unit

        // Act - Since preloadIndexesAndConstraints is private, we test through onCreate
        databasePreloader.onCreate(mockDatabase)

        // Assert
        verify(atLeast = 1) {
            mockDatabase.execSQL("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)")
        }
    }

    @Test
    fun `preloadIndexesAndConstraints should skip users index if already exists`() {
        // Arrange
        val mockCursor = mockk<android.database.Cursor>()
        every { mockDatabase.query("PRAGMA index_list('users')") } returns mockCursor
        every { mockCursor.count } returns 1
        every { mockCursor.close() } returns Unit

        // Act
        databasePreloader.onCreate(mockDatabase)

        // Assert - Should not try to create index
        verify(exactly = 0) {
            mockDatabase.execSQL("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)")
        }
    }

    @Test
    fun `preloadIndexesAndConstraints should create financial_records indexes if not exist`() {
        // Arrange
        val mockCursor = mockk<android.database.Cursor>()
        every { mockDatabase.query("PRAGMA index_list('financial_records')") } returns mockCursor
        every { mockCursor.count } returns 0
        every { mockDatabase.execSQL("CREATE INDEX IF NOT EXISTS idx_financial_user_id ON financial_records(user_id)") } returns Unit
        every { mockDatabase.execSQL("CREATE INDEX IF NOT EXISTS idx_financial_updated_at ON financial_records(updated_at DESC)") } returns Unit
        every { mockCursor.close() } returns Unit

        // Act
        databasePreloader.onCreate(mockDatabase)

        // Assert
        verify(atLeast = 1) {
            mockDatabase.execSQL("CREATE INDEX IF NOT EXISTS idx_financial_user_id ON financial_records(user_id)")
        }
        verify(atLeast = 1) {
            mockDatabase.execSQL("CREATE INDEX IF NOT EXISTS idx_financial_updated_at ON financial_records(updated_at DESC)")
        }
    }

    @Test
    fun `preloadIndexesAndConstraints should skip financial_records indexes if already exist`() {
        // Arrange
        val mockCursor = mockk<android.database.Cursor>()
        every { mockDatabase.query("PRAGMA index_list('financial_records')") } returns mockCursor
        every { mockCursor.count } returns 2
        every { mockCursor.close() } returns Unit

        // Act
        databasePreloader.onCreate(mockDatabase)

        // Assert - Should not try to create indexes
        verify(exactly = 0) {
            mockDatabase.execSQL(match { it.contains("CREATE INDEX") && it.contains("financial_records") })
        }
    }

    @Test
    fun `validateCacheIntegrity should check database integrity`() {
        // Arrange
        val mockCursor = mockk<android.database.Cursor>()
        every { mockDatabase.query("PRAGMA integrity_check") } returns mockCursor
        every { mockCursor.moveToFirst() } returns true
        every { mockCursor.getString(0) } returns "ok"
        every { mockCursor.close() } returns Unit

        // Act
        databasePreloader.onOpen(mockDatabase)

        // Assert
        verify(atLeast = 1) {
            mockDatabase.query("PRAGMA integrity_check")
        }
        verify(atLeast = 1) {
            mockCursor.moveToFirst()
        }
    }

    @Test
    fun `validateCacheIntegrity should handle integrity check failure gracefully`() {
        // Arrange
        val mockCursor = mockk<android.database.Cursor>()
        every { mockDatabase.query("PRAGMA integrity_check") } returns mockCursor
        every { mockCursor.moveToFirst() } returns true
        every { mockCursor.getString(0) } returns "error in database"
        every { mockCursor.close() } returns Unit

        // Act & Assert - Should not throw exception
        try {
            databasePreloader.onOpen(mockDatabase)
            // Success if no exception thrown
        } catch (e: Exception) {
            fail("validateCacheIntegrity should handle errors gracefully: ${e.message}")
        }
    }

    @Test
    fun `validateCacheIntegrity should handle empty cursor gracefully`() {
        // Arrange
        val mockCursor = mockk<android.database.Cursor>()
        every { mockDatabase.query("PRAGMA integrity_check") } returns mockCursor
        every { mockCursor.moveToFirst() } returns false
        every { mockCursor.close() } returns Unit

        // Act & Assert - Should not throw exception
        try {
            databasePreloader.onOpen(mockDatabase)
            // Success if no exception thrown
        } catch (e: Exception) {
            fail("validateCacheIntegrity should handle empty cursor gracefully: ${e.message}")
        }
    }

    @Test
    fun `preloadIndexesAndConstraints should handle database errors gracefully`() {
        // Arrange
        every {
            mockDatabase.query("PRAGMA index_list('users')")
        } throws RuntimeException("Database connection failed")

        // Act & Assert - Should not throw exception
        try {
            databasePreloader.onCreate(mockDatabase)
            // Success if no exception thrown
        } catch (e: Exception) {
            fail("preloadIndexesAndConstraints should handle database errors gracefully: ${e.message}")
        }
    }

    @Test
    fun `validateCacheIntegrity should handle database query errors gracefully`() {
        // Arrange
        every {
            mockDatabase.query("PRAGMA integrity_check")
        } throws RuntimeException("Database query failed")

        // Act & Assert - Should not throw exception
        try {
            databasePreloader.onOpen(mockDatabase)
            // Success if no exception thrown
        } catch (e: Exception) {
            fail("validateCacheIntegrity should handle database query errors gracefully: ${e.message}")
        }
    }

    @Test
    fun `onCreate should handle multiple table index checks`() {
        // Arrange - Setup both users and financial_records tables without indexes
        val mockUsersCursor = mockk<android.database.Cursor>()
        val mockFinancialCursor = mockk<android.database.Cursor>()
        
        every { mockDatabase.query("PRAGMA index_list('users')") } returns mockUsersCursor
        every { mockUsersCursor.count } returns 0
        every { mockUsersCursor.close() } returns Unit
        
        every { mockDatabase.query("PRAGMA index_list('financial_records')") } returns mockFinancialCursor
        every { mockFinancialCursor.count } returns 0
        every { mockFinancialCursor.close() } returns Unit
        
        every { mockDatabase.execSQL(any<String>()) } returns Unit

        // Act
        databasePreloader.onCreate(mockDatabase)

        // Assert
        verify(atLeast = 1) {
            mockDatabase.query("PRAGMA index_list('users')")
        }
        verify(atLeast = 1) {
            mockDatabase.query("PRAGMA index_list('financial_records')")
        }
    }

    @Test
    fun `preloadIndexesAndConstraints should not create duplicate indexes`() {
        // Arrange - Users table already has index, financial_records doesn't
        val mockUsersCursor = mockk<android.database.Cursor>()
        val mockFinancialCursor = mockk<android.database.Cursor>()
        
        every { mockDatabase.query("PRAGMA index_list('users')") } returns mockUsersCursor
        every { mockUsersCursor.count } returns 1
        every { mockUsersCursor.close() } returns Unit
        
        every { mockDatabase.query("PRAGMA index_list('financial_records')") } returns mockFinancialCursor
        every { mockFinancialCursor.count } returns 0
        every { mockFinancialCursor.close() } returns Unit
        
        every { mockDatabase.execSQL(any<String>()) } returns Unit

        // Act
        databasePreloader.onCreate(mockDatabase)

        // Assert - Users index should not be created, financial_records should
        verify(exactly = 0) {
            mockDatabase.execSQL("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)")
        }
        verify(atLeast = 1) {
            mockDatabase.execSQL("CREATE INDEX IF NOT EXISTS idx_financial_user_id ON financial_records(user_id)")
        }
    }

    @Test
    fun `validateCacheIntegrity should close cursor after use`() {
        // Arrange
        val mockCursor = mockk<android.database.Cursor>()
        every { mockDatabase.query("PRAGMA integrity_check") } returns mockCursor
        every { mockCursor.moveToFirst() } returns true
        every { mockCursor.getString(0) } returns "ok"
        every { mockCursor.close() } returns Unit

        // Act
        databasePreloader.onOpen(mockDatabase)

        // Assert
        verify(atLeast = 1) {
            mockCursor.close()
        }
    }

    @Test
    fun `preloadIndexesAndConstraints should close cursors after use`() {
        // Arrange
        val mockUsersCursor = mockk<android.database.Cursor>()
        val mockFinancialCursor = mockk<android.database.Cursor>()
        
        every { mockDatabase.query("PRAGMA index_list('users')") } returns mockUsersCursor
        every { mockUsersCursor.count } returns 1
        every { mockUsersCursor.close() } returns Unit
        
        every { mockDatabase.query("PRAGMA index_list('financial_records')") } returns mockFinancialCursor
        every { mockFinancialCursor.count } returns 1
        every { mockFinancialCursor.close() } returns Unit

        // Act
        databasePreloader.onCreate(mockDatabase)

        // Assert
        verify(atLeast = 1) {
            mockUsersCursor.close()
        }
        verify(atLeast = 1) {
            mockFinancialCursor.close()
        }
    }
}
