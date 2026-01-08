package com.example.iurankomplek.data.constraints

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class TransactionConstraintsTest {

    @Test
    fun `TABLE_NAME should be 'transactions'`() {
        assertEquals("transactions", TransactionConstraints.TABLE_NAME)
    }

    @Test
    fun `Columns should contain all required fields`() {
        val expectedColumns = listOf(
            TransactionConstraints.Columns.ID,
            TransactionConstraints.Columns.USER_ID,
            TransactionConstraints.Columns.AMOUNT,
            TransactionConstraints.Columns.CURRENCY,
            TransactionConstraints.Columns.STATUS,
            TransactionConstraints.Columns.PAYMENT_METHOD,
            TransactionConstraints.Columns.DESCRIPTION,
            TransactionConstraints.Columns.IS_DELETED,
            TransactionConstraints.Columns.CREATED_AT,
            TransactionConstraints.Columns.UPDATED_AT,
            TransactionConstraints.Columns.METADATA
        )

        assertEquals(11, expectedColumns.size)
        assertEquals("id", TransactionConstraints.Columns.ID)
        assertEquals("user_id", TransactionConstraints.Columns.USER_ID)
        assertEquals("amount", TransactionConstraints.Columns.AMOUNT)
        assertEquals("currency", TransactionConstraints.Columns.CURRENCY)
        assertEquals("status", TransactionConstraints.Columns.STATUS)
        assertEquals("payment_method", TransactionConstraints.Columns.PAYMENT_METHOD)
        assertEquals("description", TransactionConstraints.Columns.DESCRIPTION)
        assertEquals("is_deleted", TransactionConstraints.Columns.IS_DELETED)
        assertEquals("created_at", TransactionConstraints.Columns.CREATED_AT)
        assertEquals("updated_at", TransactionConstraints.Columns.UPDATED_AT)
        assertEquals("metadata", TransactionConstraints.Columns.METADATA)
    }

    @Test
    fun `Constraints MAX_AMOUNT should be 999999999.99`() {
        assertEquals(BigDecimal("999999999.99"), TransactionConstraints.Constraints.MAX_AMOUNT)
    }

    @Test
    fun `Constraints MAX_CURRENCY_LENGTH should be 3`() {
        assertEquals(3, TransactionConstraints.Constraints.MAX_CURRENCY_LENGTH)
    }

    @Test
    fun `Constraints MAX_DESCRIPTION_LENGTH should be 500`() {
        assertEquals(500, TransactionConstraints.Constraints.MAX_DESCRIPTION_LENGTH)
    }

    @Test
    fun `Constraints MAX_METADATA_LENGTH should be 2000`() {
        assertEquals(2000, TransactionConstraints.Constraints.MAX_METADATA_LENGTH)
    }

    @Test
    fun `Indexes should contain all required indexes`() {
        val expectedIndexes = listOf(
            TransactionConstraints.Indexes.IDX_USER_ID,
            TransactionConstraints.Indexes.IDX_STATUS,
            TransactionConstraints.Indexes.IDX_USER_STATUS,
            TransactionConstraints.Indexes.IDX_STATUS_DELETED,
            TransactionConstraints.Indexes.IDX_CREATED_AT,
            TransactionConstraints.Indexes.IDX_UPDATED_AT
        )

        assertEquals(6, expectedIndexes.size)
        assertEquals("idx_transactions_user_id", TransactionConstraints.Indexes.IDX_USER_ID)
        assertEquals("idx_transactions_status", TransactionConstraints.Indexes.IDX_STATUS)
        assertEquals("idx_transactions_user_status", TransactionConstraints.Indexes.IDX_USER_STATUS)
        assertEquals("idx_transactions_status_deleted", TransactionConstraints.Indexes.IDX_STATUS_DELETED)
        assertEquals("idx_transactions_created_at", TransactionConstraints.Indexes.IDX_CREATED_AT)
        assertEquals("idx_transactions_updated_at", TransactionConstraints.Indexes.IDX_UPDATED_AT)
    }

    @Test
    fun `TABLE_SQL should create transactions table with all columns`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should contain CREATE TABLE", tableSql.contains("CREATE TABLE"))
        assertTrue("TABLE_SQL should contain transactions table name", tableSql.contains(TransactionConstraints.TABLE_NAME))
        assertTrue("TABLE_SQL should contain id column", tableSql.contains(TransactionConstraints.Columns.ID))
        assertTrue("TABLE_SQL should contain user_id column", tableSql.contains(TransactionConstraints.Columns.USER_ID))
        assertTrue("TABLE_SQL should contain amount column", tableSql.contains(TransactionConstraints.Columns.AMOUNT))
        assertTrue("TABLE_SQL should contain currency column", tableSql.contains(TransactionConstraints.Columns.CURRENCY))
        assertTrue("TABLE_SQL should contain status column", tableSql.contains(TransactionConstraints.Columns.STATUS))
        assertTrue("TABLE_SQL should contain payment_method column", tableSql.contains(TransactionConstraints.Columns.PAYMENT_METHOD))
        assertTrue("TABLE_SQL should contain description column", tableSql.contains(TransactionConstraints.Columns.DESCRIPTION))
        assertTrue("TABLE_SQL should contain is_deleted column", tableSql.contains(TransactionConstraints.Columns.IS_DELETED))
        assertTrue("TABLE_SQL should contain created_at column", tableSql.contains(TransactionConstraints.Columns.CREATED_AT))
        assertTrue("TABLE_SQL should contain updated_at column", tableSql.contains(TransactionConstraints.Columns.UPDATED_AT))
        assertTrue("TABLE_SQL should contain metadata column", tableSql.contains(TransactionConstraints.Columns.METADATA))
    }

    @Test
    fun `TABLE_SQL should enforce NOT NULL on required fields`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should enforce NOT NULL on user_id", 
            tableSql.contains("${TransactionConstraints.Columns.USER_ID} INTEGER NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on amount", 
            tableSql.contains("${TransactionConstraints.Columns.AMOUNT} NUMERIC NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on currency", 
            tableSql.contains("${TransactionConstraints.Columns.CURRENCY} TEXT NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on status", 
            tableSql.contains("${TransactionConstraints.Columns.STATUS} TEXT NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on payment_method", 
            tableSql.contains("${TransactionConstraints.Columns.PAYMENT_METHOD} TEXT NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on description", 
            tableSql.contains("${TransactionConstraints.Columns.DESCRIPTION} TEXT NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on is_deleted", 
            tableSql.contains("${TransactionConstraints.Columns.IS_DELETED} INTEGER NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on created_at", 
            tableSql.contains("${TransactionConstraints.Columns.CREATED_AT} INTEGER NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on updated_at", 
            tableSql.contains("${TransactionConstraints.Columns.UPDATED_AT} INTEGER NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on metadata", 
            tableSql.contains("${TransactionConstraints.Columns.METADATA} TEXT NOT NULL"))
    }

    @Test
    fun `TABLE_SQL should enforce amount constraints (greater than 0 and less than MAX_AMOUNT)`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should enforce amount > 0", 
            tableSql.contains("CHECK(${TransactionConstraints.Columns.AMOUNT} > 0"))
        assertTrue("TABLE_SQL should enforce amount <= MAX_AMOUNT", 
            tableSql.contains("CHECK(${TransactionConstraints.Columns.AMOUNT} <= ${TransactionConstraints.Constraints.MAX_AMOUNT})"))
    }

    @Test
    fun `TABLE_SQL should enforce currency length constraint`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should enforce currency length <= MAX_CURRENCY_LENGTH", 
            tableSql.contains("CHECK(length(${TransactionConstraints.Columns.CURRENCY}) <= ${TransactionConstraints.Constraints.MAX_CURRENCY_LENGTH})"))
    }

    @Test
    fun `TABLE_SQL should set default currency to IDR`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should set default currency to 'IDR'", 
            tableSql.contains("${TransactionConstraints.Columns.CURRENCY} TEXT NOT NULL DEFAULT 'IDR'"))
    }

    @Test
    fun `TABLE_SQL should enforce status allowed values`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should enforce status IN (PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED, CANCELLED)", 
            tableSql.contains("CHECK(${TransactionConstraints.Columns.STATUS} IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED'))"))
    }

    @Test
    fun `TABLE_SQL should enforce payment_method allowed values`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should enforce payment_method IN (CREDIT_CARD, BANK_TRANSFER, E_WALLET, VIRTUAL_ACCOUNT)", 
            tableSql.contains("CHECK(${TransactionConstraints.Columns.PAYMENT_METHOD} IN ('CREDIT_CARD', 'BANK_TRANSFER', 'E_WALLET', 'VIRTUAL_ACCOUNT'))"))
    }

    @Test
    fun `TABLE_SQL should enforce description length constraints (greater than 0 and less than MAX_DESCRIPTION_LENGTH)`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should enforce description length > 0", 
            tableSql.contains("CHECK(length(${TransactionConstraints.Columns.DESCRIPTION}) > 0"))
        assertTrue("TABLE_SQL should enforce description length <= MAX_DESCRIPTION_LENGTH", 
            tableSql.contains("CHECK(length(${TransactionConstraints.Columns.DESCRIPTION}) <= ${TransactionConstraints.Constraints.MAX_DESCRIPTION_LENGTH})"))
    }

    @Test
    fun `TABLE_SQL should enforce metadata length constraint`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should enforce metadata length <= MAX_METADATA_LENGTH", 
            tableSql.contains("CHECK(length(${TransactionConstraints.Columns.METADATA}) <= ${TransactionConstraints.Constraints.MAX_METADATA_LENGTH})"))
    }

    @Test
    fun `TABLE_SQL should enforce is_deleted values (0 or 1)`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should enforce is_deleted IN (0, 1)", 
            tableSql.contains("CHECK(${TransactionConstraints.Columns.IS_DELETED} IN (0, 1))"))
    }

    @Test
    fun `TABLE_SQL should set default is_deleted to 0`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should set default is_deleted to 0", 
            tableSql.contains("${TransactionConstraints.Columns.IS_DELETED} INTEGER NOT NULL DEFAULT 0"))
    }

    @Test
    fun `TABLE_SQL should set default metadata to empty string`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should set default metadata to empty string", 
            tableSql.contains("${TransactionConstraints.Columns.METADATA} TEXT NOT NULL DEFAULT ''"))
    }

    @Test
    fun `TABLE_SQL should set default timestamp for created_at`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should set default for created_at", 
            tableSql.contains("${TransactionConstraints.Columns.CREATED_AT} INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))"))
    }

    @Test
    fun `TABLE_SQL should set default timestamp for updated_at`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should set default for updated_at", 
            tableSql.contains("${TransactionConstraints.Columns.UPDATED_AT} INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))"))
    }

    @Test
    fun `TABLE_SQL should have foreign key to users table`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should contain FOREIGN KEY", tableSql.contains("FOREIGN KEY"))
        assertTrue("TABLE_SQL should reference user_id column", 
            tableSql.contains("FOREIGN KEY(${TransactionConstraints.Columns.USER_ID})"))
        assertTrue("TABLE_SQL should reference users table", 
            tableSql.contains("REFERENCES ${UserConstraints.TABLE_NAME}(${UserConstraints.Columns.ID})"))
    }

    @Test
    fun `TABLE_SQL should enforce RESTRICT delete on foreign key`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should contain ON DELETE RESTRICT", tableSql.contains("ON DELETE RESTRICT"))
    }

    @Test
    fun `TABLE_SQL should enforce CASCADE update on foreign key`() {
        val tableSql = TransactionConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should contain ON UPDATE CASCADE", tableSql.contains("ON UPDATE CASCADE"))
    }

    @Test
    fun `INDEX_USER_ID_SQL should create index on user_id column`() {
        val indexSql = TransactionConstraints.INDEX_USER_ID_SQL

        assertTrue("INDEX_USER_ID_SQL should contain CREATE INDEX", indexSql.contains("CREATE INDEX"))
        assertTrue("INDEX_USER_ID_SQL should contain index name", indexSql.contains(TransactionConstraints.Indexes.IDX_USER_ID))
        assertTrue("INDEX_USER_ID_SQL should contain transactions table name", indexSql.contains(TransactionConstraints.TABLE_NAME))
        assertTrue("INDEX_USER_ID_SQL should reference user_id column", indexSql.contains(TransactionConstraints.Columns.USER_ID))
    }

    @Test
    fun `INDEX_STATUS_SQL should create index on status column`() {
        val indexSql = TransactionConstraints.INDEX_STATUS_SQL

        assertTrue("INDEX_STATUS_SQL should contain CREATE INDEX", indexSql.contains("CREATE INDEX"))
        assertTrue("INDEX_STATUS_SQL should contain index name", indexSql.contains(TransactionConstraints.Indexes.IDX_STATUS))
        assertTrue("INDEX_STATUS_SQL should contain transactions table name", indexSql.contains(TransactionConstraints.TABLE_NAME))
        assertTrue("INDEX_STATUS_SQL should reference status column", indexSql.contains(TransactionConstraints.Columns.STATUS))
    }

    @Test
    fun `INDEX_USER_STATUS_SQL should create composite index on user_id and status`() {
        val indexSql = TransactionConstraints.INDEX_USER_STATUS_SQL

        assertTrue("INDEX_USER_STATUS_SQL should contain CREATE INDEX", indexSql.contains("CREATE INDEX"))
        assertTrue("INDEX_USER_STATUS_SQL should contain index name", indexSql.contains(TransactionConstraints.Indexes.IDX_USER_STATUS))
        assertTrue("INDEX_USER_STATUS_SQL should contain transactions table name", indexSql.contains(TransactionConstraints.TABLE_NAME))
        assertTrue("INDEX_USER_STATUS_SQL should reference user_id column", indexSql.contains(TransactionConstraints.Columns.USER_ID))
        assertTrue("INDEX_USER_STATUS_SQL should reference status column", indexSql.contains(TransactionConstraints.Columns.STATUS))
    }

    @Test
    fun `INDEX_STATUS_DELETED_SQL should create partial index on status and is_deleted`() {
        val indexSql = TransactionConstraints.INDEX_STATUS_DELETED_SQL

        assertTrue("INDEX_STATUS_DELETED_SQL should contain CREATE INDEX", indexSql.contains("CREATE INDEX"))
        assertTrue("INDEX_STATUS_DELETED_SQL should contain index name", indexSql.contains(TransactionConstraints.Indexes.IDX_STATUS_DELETED))
        assertTrue("INDEX_STATUS_DELETED_SQL should contain transactions table name", indexSql.contains(TransactionConstraints.TABLE_NAME))
        assertTrue("INDEX_STATUS_DELETED_SQL should reference status column", indexSql.contains(TransactionConstraints.Columns.STATUS))
        assertTrue("INDEX_STATUS_DELETED_SQL should reference is_deleted column", indexSql.contains(TransactionConstraints.Columns.IS_DELETED))
        assertTrue("INDEX_STATUS_DELETED_SQL should contain WHERE clause", indexSql.contains("WHERE"))
        assertTrue("INDEX_STATUS_DELETED_SQL should filter is_deleted = 0", 
            indexSql.contains("WHERE ${TransactionConstraints.Columns.IS_DELETED} = 0"))
    }

    @Test
    fun `INDEX_CREATED_AT_SQL should create index on created_at column`() {
        val indexSql = TransactionConstraints.INDEX_CREATED_AT_SQL

        assertTrue("INDEX_CREATED_AT_SQL should contain CREATE INDEX", indexSql.contains("CREATE INDEX"))
        assertTrue("INDEX_CREATED_AT_SQL should contain index name", indexSql.contains(TransactionConstraints.Indexes.IDX_CREATED_AT))
        assertTrue("INDEX_CREATED_AT_SQL should contain transactions table name", indexSql.contains(TransactionConstraints.TABLE_NAME))
        assertTrue("INDEX_CREATED_AT_SQL should reference created_at column", indexSql.contains(TransactionConstraints.Columns.CREATED_AT))
    }

    @Test
    fun `INDEX_UPDATED_AT_SQL should create index on updated_at column`() {
        val indexSql = TransactionConstraints.INDEX_UPDATED_AT_SQL

        assertTrue("INDEX_UPDATED_AT_SQL should contain CREATE INDEX", indexSql.contains("CREATE INDEX"))
        assertTrue("INDEX_UPDATED_AT_SQL should contain index name", indexSql.contains(TransactionConstraints.Indexes.IDX_UPDATED_AT))
        assertTrue("INDEX_UPDATED_AT_SQL should contain transactions table name", indexSql.contains(TransactionConstraints.TABLE_NAME))
        assertTrue("INDEX_UPDATED_AT_SQL should reference updated_at column", indexSql.contains(TransactionConstraints.Columns.UPDATED_AT))
    }

    @Test
    fun `constraint values should be reasonable`() {
        assertTrue("MAX_AMOUNT should be positive", TransactionConstraints.Constraints.MAX_AMOUNT > BigDecimal.ZERO)
        assertTrue("MAX_CURRENCY_LENGTH should be positive", TransactionConstraints.Constraints.MAX_CURRENCY_LENGTH > 0)
        assertTrue("MAX_DESCRIPTION_LENGTH should be positive", TransactionConstraints.Constraints.MAX_DESCRIPTION_LENGTH > 0)
        assertTrue("MAX_METADATA_LENGTH should be positive", TransactionConstraints.Constraints.MAX_METADATA_LENGTH > 0)

        assertTrue("MAX_CURRENCY_LENGTH should be at least 3", TransactionConstraints.Constraints.MAX_CURRENCY_LENGTH >= 3)
        assertTrue("MAX_DESCRIPTION_LENGTH should be at least 50", TransactionConstraints.Constraints.MAX_DESCRIPTION_LENGTH >= 50)
        assertTrue("MAX_METADATA_LENGTH should be at least 500", TransactionConstraints.Constraints.MAX_METADATA_LENGTH >= 500)
        assertTrue("MAX_AMOUNT should be at least 1000000", TransactionConstraints.Constraints.MAX_AMOUNT >= BigDecimal("1000000"))
    }
}
