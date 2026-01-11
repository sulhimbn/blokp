package com.example.iurankomplek.data.constraints

import org.junit.Assert.*
import org.junit.Test

class UserConstraintsTest {

    @Test
    fun `TABLE_NAME should be 'users'`() {
        assertEquals("users", UserConstraints.TABLE_NAME)
    }

    @Test
    fun `Columns should contain all required fields`() {
        val expectedColumns = listOf(
            UserConstraints.Columns.ID,
            UserConstraints.Columns.EMAIL,
            UserConstraints.Columns.FIRST_NAME,
            UserConstraints.Columns.LAST_NAME,
            UserConstraints.Columns.ALAMAT,
            UserConstraints.Columns.AVATAR,
            UserConstraints.Columns.IS_DELETED,
            UserConstraints.Columns.CREATED_AT,
            UserConstraints.Columns.UPDATED_AT
        )

        assertEquals(9, expectedColumns.size)
        assertEquals("id", UserConstraints.Columns.ID)
        assertEquals("email", UserConstraints.Columns.EMAIL)
        assertEquals("first_name", UserConstraints.Columns.FIRST_NAME)
        assertEquals("last_name", UserConstraints.Columns.LAST_NAME)
        assertEquals("alamat", UserConstraints.Columns.ALAMAT)
        assertEquals("avatar", UserConstraints.Columns.AVATAR)
        assertEquals("is_deleted", UserConstraints.Columns.IS_DELETED)
        assertEquals("created_at", UserConstraints.Columns.CREATED_AT)
        assertEquals("updated_at", UserConstraints.Columns.UPDATED_AT)
    }

    @Test
    fun `Constraints MAX_EMAIL_LENGTH should be 255`() {
        assertEquals(255, UserConstraints.Constraints.MAX_EMAIL_LENGTH)
    }

    @Test
    fun `Constraints MAX_NAME_LENGTH should be 100`() {
        assertEquals(100, UserConstraints.Constraints.MAX_NAME_LENGTH)
    }

    @Test
    fun `Constraints MAX_ALAMAT_LENGTH should be 500`() {
        assertEquals(500, UserConstraints.Constraints.MAX_ALAMAT_LENGTH)
    }

    @Test
    fun `Constraints MAX_AVATAR_LENGTH should be 2048`() {
        assertEquals(2048, UserConstraints.Constraints.MAX_AVATAR_LENGTH)
    }

    @Test
    fun `Indexes should contain all required indexes`() {
        val expectedIndexes = listOf(
            UserConstraints.Indexes.IDX_EMAIL,
            UserConstraints.Indexes.IDX_ACTIVE_USERS,
            UserConstraints.Indexes.IDX_ACTIVE_USERS_UPDATED
        )

        assertEquals(3, expectedIndexes.size)
        assertEquals("idx_users_email", UserConstraints.Indexes.IDX_EMAIL)
        assertEquals("idx_users_active", UserConstraints.Indexes.IDX_ACTIVE_USERS)
        assertEquals("idx_users_active_updated", UserConstraints.Indexes.IDX_ACTIVE_USERS_UPDATED)
    }

    @Test
    fun `TABLE_SQL should create users table with all columns`() {
        val tableSql = UserConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should contain CREATE TABLE", tableSql.contains("CREATE TABLE"))
        assertTrue("TABLE_SQL should contain users table name", tableSql.contains(UserConstraints.TABLE_NAME))
        assertTrue("TABLE_SQL should contain id column", tableSql.contains(UserConstraints.Columns.ID))
        assertTrue("TABLE_SQL should contain email column", tableSql.contains(UserConstraints.Columns.EMAIL))
        assertTrue("TABLE_SQL should contain first_name column", tableSql.contains(UserConstraints.Columns.FIRST_NAME))
        assertTrue("TABLE_SQL should contain last_name column", tableSql.contains(UserConstraints.Columns.LAST_NAME))
        assertTrue("TABLE_SQL should contain alamat column", tableSql.contains(UserConstraints.Columns.ALAMAT))
        assertTrue("TABLE_SQL should contain avatar column", tableSql.contains(UserConstraints.Columns.AVATAR))
        assertTrue("TABLE_SQL should contain is_deleted column", tableSql.contains(UserConstraints.Columns.IS_DELETED))
        assertTrue("TABLE_SQL should contain created_at column", tableSql.contains(UserConstraints.Columns.CREATED_AT))
        assertTrue("TABLE_SQL should contain updated_at column", tableSql.contains(UserConstraints.Columns.UPDATED_AT))
    }

    @Test
    fun `TABLE_SQL should enforce email uniqueness constraint`() {
        val tableSql = UserConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should contain UNIQUE constraint on email", tableSql.contains("UNIQUE"))
        assertTrue("TABLE_SQL should reference email column in UNIQUE constraint", 
            tableSql.contains(UserConstraints.Columns.EMAIL) && tableSql.contains("UNIQUE"))
    }

    @Test
    fun `TABLE_SQL should enforce max length constraints on text fields`() {
        val tableSql = UserConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should enforce max email length", 
            tableSql.contains("CHECK(length(${UserConstraints.Columns.EMAIL}) <= ${UserConstraints.Constraints.MAX_EMAIL_LENGTH})"))
        assertTrue("TABLE_SQL should enforce max first_name length", 
            tableSql.contains("CHECK(length(${UserConstraints.Columns.FIRST_NAME}) <= ${UserConstraints.Constraints.MAX_NAME_LENGTH})"))
        assertTrue("TABLE_SQL should enforce max last_name length", 
            tableSql.contains("CHECK(length(${UserConstraints.Columns.LAST_NAME}) <= ${UserConstraints.Constraints.MAX_NAME_LENGTH})"))
        assertTrue("TABLE_SQL should enforce max alamat length", 
            tableSql.contains("CHECK(length(${UserConstraints.Columns.ALAMAT}) <= ${UserConstraints.Constraints.MAX_ALAMAT_LENGTH})"))
        assertTrue("TABLE_SQL should enforce max avatar length", 
            tableSql.contains("CHECK(length(${UserConstraints.Columns.AVATAR}) <= ${UserConstraints.Constraints.MAX_AVATAR_LENGTH})"))
    }

    @Test
    fun `TABLE_SQL should enforce is_deleted values (0 or 1)`() {
        val tableSql = UserConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should enforce is_deleted IN (0, 1)", 
            tableSql.contains("CHECK(${UserConstraints.Columns.IS_DELETED} IN (0, 1))"))
    }

    @Test
    fun `TABLE_SQL should enforce NOT NULL on required fields`() {
        val tableSql = UserConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should enforce NOT NULL on email", 
            tableSql.contains("${UserConstraints.Columns.EMAIL} TEXT NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on first_name", 
            tableSql.contains("${UserConstraints.Columns.FIRST_NAME} TEXT NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on last_name", 
            tableSql.contains("${UserConstraints.Columns.LAST_NAME} TEXT NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on alamat", 
            tableSql.contains("${UserConstraints.Columns.ALAMAT} TEXT NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on avatar", 
            tableSql.contains("${UserConstraints.Columns.AVATAR} TEXT NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on is_deleted", 
            tableSql.contains("${UserConstraints.Columns.IS_DELETED} INTEGER NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on created_at", 
            tableSql.contains("${UserConstraints.Columns.CREATED_AT} INTEGER NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on updated_at", 
            tableSql.contains("${UserConstraints.Columns.UPDATED_AT} INTEGER NOT NULL"))
    }

    @Test
    fun `TABLE_SQL should set default timestamp for created_at`() {
        val tableSql = UserConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should set default for created_at", 
            tableSql.contains("${UserConstraints.Columns.CREATED_AT} INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))"))
    }

    @Test
    fun `TABLE_SQL should set default timestamp for updated_at`() {
        val tableSql = UserConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should set default for updated_at", 
            tableSql.contains("${UserConstraints.Columns.UPDATED_AT} INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))"))
    }

    @Test
    fun `TABLE_SQL should set default is_deleted to 0`() {
        val tableSql = UserConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should set default is_deleted to 0", 
            tableSql.contains("${UserConstraints.Columns.IS_DELETED} INTEGER NOT NULL DEFAULT 0"))
    }

    @Test
    fun `INDEX_EMAIL_SQL should create index on email column`() {
        val indexSql = UserConstraints.INDEX_EMAIL_SQL

        assertTrue("INDEX_EMAIL_SQL should contain CREATE INDEX", indexSql.contains("CREATE INDEX"))
        assertTrue("INDEX_EMAIL_SQL should contain index name", indexSql.contains(UserConstraints.Indexes.IDX_EMAIL))
        assertTrue("INDEX_EMAIL_SQL should contain users table name", indexSql.contains(UserConstraints.TABLE_NAME))
        assertTrue("INDEX_EMAIL_SQL should reference email column", indexSql.contains(UserConstraints.Columns.EMAIL))
    }

    @Test
    fun `constraint values should be reasonable`() {
        assertTrue("MAX_EMAIL_LENGTH should be positive", UserConstraints.Constraints.MAX_EMAIL_LENGTH > 0)
        assertTrue("MAX_NAME_LENGTH should be positive", UserConstraints.Constraints.MAX_NAME_LENGTH > 0)
        assertTrue("MAX_ALAMAT_LENGTH should be positive", UserConstraints.Constraints.MAX_ALAMAT_LENGTH > 0)
        assertTrue("MAX_AVATAR_LENGTH should be positive", UserConstraints.Constraints.MAX_AVATAR_LENGTH > 0)

        assertTrue("MAX_EMAIL_LENGTH should be at least 255", UserConstraints.Constraints.MAX_EMAIL_LENGTH >= 255)
        assertTrue("MAX_NAME_LENGTH should be at least 50", UserConstraints.Constraints.MAX_NAME_LENGTH >= 50)
        assertTrue("MAX_ALAMAT_LENGTH should be at least 200", UserConstraints.Constraints.MAX_ALAMAT_LENGTH >= 200)
        assertTrue("MAX_AVATAR_LENGTH should be at least 1024", UserConstraints.Constraints.MAX_AVATAR_LENGTH >= 1024)
    }
}
