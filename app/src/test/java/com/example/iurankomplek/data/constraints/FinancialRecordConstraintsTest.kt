package com.example.iurankomplek.data.constraints

import org.junit.Assert.*
import org.junit.Test

class FinancialRecordConstraintsTest {

    @Test
    fun `TABLE_NAME should be 'financial_records'`() {
        assertEquals("financial_records", FinancialRecordConstraints.TABLE_NAME)
    }

    @Test
    fun `Columns should contain all required fields`() {
        val expectedColumns = listOf(
            FinancialRecordConstraints.Columns.ID,
            FinancialRecordConstraints.Columns.USER_ID,
            FinancialRecordConstraints.Columns.IURAN_PERWARGA,
            FinancialRecordConstraints.Columns.JUMLAH_IURAN_BULANAN,
            FinancialRecordConstraints.Columns.TOTAL_IURAN_INDIVIDU,
            FinancialRecordConstraints.Columns.PENGELUARAN_IURAN_WARGA,
            FinancialRecordConstraints.Columns.TOTAL_IURAN_REKAP,
            FinancialRecordConstraints.Columns.PEMANFAATAN_IURAN,
            FinancialRecordConstraints.Columns.IS_DELETED,
            FinancialRecordConstraints.Columns.CREATED_AT,
            FinancialRecordConstraints.Columns.UPDATED_AT
        )

        assertEquals(11, expectedColumns.size)
        assertEquals("id", FinancialRecordConstraints.Columns.ID)
        assertEquals("user_id", FinancialRecordConstraints.Columns.USER_ID)
        assertEquals("iuran_perwarga", FinancialRecordConstraints.Columns.IURAN_PERWARGA)
        assertEquals("jumlah_iuran_bulanan", FinancialRecordConstraints.Columns.JUMLAH_IURAN_BULANAN)
        assertEquals("total_iuran_individu", FinancialRecordConstraints.Columns.TOTAL_IURAN_INDIVIDU)
        assertEquals("pengeluaran_iuran_warga", FinancialRecordConstraints.Columns.PENGELUARAN_IURAN_WARGA)
        assertEquals("total_iuran_rekap", FinancialRecordConstraints.Columns.TOTAL_IURAN_REKAP)
        assertEquals("pemanfaatan_iuran", FinancialRecordConstraints.Columns.PEMANFAATAN_IURAN)
        assertEquals("is_deleted", FinancialRecordConstraints.Columns.IS_DELETED)
        assertEquals("created_at", FinancialRecordConstraints.Columns.CREATED_AT)
        assertEquals("updated_at", FinancialRecordConstraints.Columns.UPDATED_AT)
    }

    @Test
    fun `Constraints MAX_PEMANFAATAN_LENGTH should be 500`() {
        assertEquals(500, FinancialRecordConstraints.Constraints.MAX_PEMANFAATAN_LENGTH)
    }

    @Test
    fun `Constraints MAX_NUMERIC_VALUE should be positive`() {
        assertTrue(FinancialRecordConstraints.Constraints.MAX_NUMERIC_VALUE > 0)
    }

    @Test
    fun `Constraints MAX_NUMERIC_VALUE should be consistent with ValidationRules`() {
        assertEquals(ValidationRules.Numeric.MAX_VALUE, FinancialRecordConstraints.Constraints.MAX_NUMERIC_VALUE)
    }

    @Test
    fun `Indexes should contain all required indexes`() {
        val expectedIndexes = listOf(
            FinancialRecordConstraints.Indexes.IDX_USER_ID,
            FinancialRecordConstraints.Indexes.IDX_UPDATED_AT,
            FinancialRecordConstraints.Indexes.IDX_USER_REKAP,
            FinancialRecordConstraints.Indexes.IDX_ACTIVE_FINANCIAL_USER_UPDATED,
            FinancialRecordConstraints.Indexes.IDX_ACTIVE_FINANCIAL,
            FinancialRecordConstraints.Indexes.IDX_ACTIVE_FINANCIAL_UPDATED
        )

        assertEquals(6, expectedIndexes.size)
        assertEquals("idx_financial_user_id", FinancialRecordConstraints.Indexes.IDX_USER_ID)
        assertEquals("idx_financial_updated_at", FinancialRecordConstraints.Indexes.IDX_UPDATED_AT)
        assertEquals("idx_financial_user_rekap", FinancialRecordConstraints.Indexes.IDX_USER_REKAP)
        assertEquals("idx_financial_active_user_updated", FinancialRecordConstraints.Indexes.IDX_ACTIVE_FINANCIAL_USER_UPDATED)
        assertEquals("idx_financial_active", FinancialRecordConstraints.Indexes.IDX_ACTIVE_FINANCIAL)
        assertEquals("idx_financial_active_updated", FinancialRecordConstraints.Indexes.IDX_ACTIVE_FINANCIAL_UPDATED)
    }

    @Test
    fun `TABLE_SQL should create financial_records table with all columns`() {
        val tableSql = FinancialRecordConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should contain CREATE TABLE", tableSql.contains("CREATE TABLE"))
        assertTrue("TABLE_SQL should contain financial_records table name", tableSql.contains(FinancialRecordConstraints.TABLE_NAME))
        assertTrue("TABLE_SQL should contain id column", tableSql.contains(FinancialRecordConstraints.Columns.ID))
        assertTrue("TABLE_SQL should contain user_id column", tableSql.contains(FinancialRecordConstraints.Columns.USER_ID))
        assertTrue("TABLE_SQL should contain iuran_perwarga column", tableSql.contains(FinancialRecordConstraints.Columns.IURAN_PERWARGA))
        assertTrue("TABLE_SQL should contain jumlah_iuran_bulanan column", tableSql.contains(FinancialRecordConstraints.Columns.JUMLAH_IURAN_BULANAN))
        assertTrue("TABLE_SQL should contain total_iuran_individu column", tableSql.contains(FinancialRecordConstraints.Columns.TOTAL_IURAN_INDIVIDU))
        assertTrue("TABLE_SQL should contain pengeluaran_iuran_warga column", tableSql.contains(FinancialRecordConstraints.Columns.PENGELUARAN_IURAN_WARGA))
        assertTrue("TABLE_SQL should contain total_iuran_rekap column", tableSql.contains(FinancialRecordConstraints.Columns.TOTAL_IURAN_REKAP))
        assertTrue("TABLE_SQL should contain pemanfaatan_iuran column", tableSql.contains(FinancialRecordConstraints.Columns.PEMANFAATAN_IURAN))
        assertTrue("TABLE_SQL should contain is_deleted column", tableSql.contains(FinancialRecordConstraints.Columns.IS_DELETED))
        assertTrue("TABLE_SQL should contain created_at column", tableSql.contains(FinancialRecordConstraints.Columns.CREATED_AT))
        assertTrue("TABLE_SQL should contain updated_at column", tableSql.contains(FinancialRecordConstraints.Columns.UPDATED_AT))
    }

    @Test
    fun `TABLE_SQL should enforce NOT NULL on required fields`() {
        val tableSql = FinancialRecordConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should enforce NOT NULL on user_id", 
            tableSql.contains("${FinancialRecordConstraints.Columns.USER_ID} INTEGER NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on iuran_perwarga", 
            tableSql.contains("${FinancialRecordConstraints.Columns.IURAN_PERWARGA} INTEGER NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on jumlah_iuran_bulanan", 
            tableSql.contains("${FinancialRecordConstraints.Columns.JUMLAH_IURAN_BULANAN} INTEGER NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on total_iuran_individu", 
            tableSql.contains("${FinancialRecordConstraints.Columns.TOTAL_IURAN_INDIVIDU} INTEGER NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on pengeluaran_iuran_warga", 
            tableSql.contains("${FinancialRecordConstraints.Columns.PENGELUARAN_IURAN_WARGA} INTEGER NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on total_iuran_rekap", 
            tableSql.contains("${FinancialRecordConstraints.Columns.TOTAL_IURAN_REKAP} INTEGER NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on pemanfaatan_iuran", 
            tableSql.contains("${FinancialRecordConstraints.Columns.PEMANFAATAN_IURAN} TEXT NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on is_deleted", 
            tableSql.contains("${FinancialRecordConstraints.Columns.IS_DELETED} INTEGER NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on created_at", 
            tableSql.contains("${FinancialRecordConstraints.Columns.CREATED_AT} INTEGER NOT NULL"))
        assertTrue("TABLE_SQL should enforce NOT NULL on updated_at", 
            tableSql.contains("${FinancialRecordConstraints.Columns.UPDATED_AT} INTEGER NOT NULL"))
    }

    @Test
    fun `TABLE_SQL should enforce non-negative constraints on numeric fields`() {
        val tableSql = FinancialRecordConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should enforce iuran_perwarga >= 0", 
            tableSql.contains("CHECK(${FinancialRecordConstraints.Columns.IURAN_PERWARGA} >= 0)"))
        assertTrue("TABLE_SQL should enforce jumlah_iuran_bulanan >= 0", 
            tableSql.contains("CHECK(${FinancialRecordConstraints.Columns.JUMLAH_IURAN_BULANAN} >= 0)"))
        assertTrue("TABLE_SQL should enforce total_iuran_individu >= 0", 
            tableSql.contains("CHECK(${FinancialRecordConstraints.Columns.TOTAL_IURAN_INDIVIDU} >= 0)"))
        assertTrue("TABLE_SQL should enforce pengeluaran_iuran_warga >= 0", 
            tableSql.contains("CHECK(${FinancialRecordConstraints.Columns.PENGELUARAN_IURAN_WARGA} >= 0)"))
        assertTrue("TABLE_SQL should enforce total_iuran_rekap >= 0", 
            tableSql.contains("CHECK(${FinancialRecordConstraints.Columns.TOTAL_IURAN_REKAP} >= 0)"))
    }

    @Test
    fun `TABLE_SQL should enforce pemanfaatan_iuran length constraint`() {
        val tableSql = FinancialRecordConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should enforce pemanfaatan_iuran length > 0", 
            tableSql.contains("CHECK(length(${FinancialRecordConstraints.Columns.PEMANFAATAN_IURAN}) > 0)"))
    }

    @Test
    fun `TABLE_SQL should enforce is_deleted values (0 or 1)`() {
        val tableSql = FinancialRecordConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should enforce is_deleted IN (0, 1)", 
            tableSql.contains("CHECK(${FinancialRecordConstraints.Columns.IS_DELETED} IN (0, 1))"))
    }

    @Test
    fun `TABLE_SQL should set default values for numeric fields`() {
        val tableSql = FinancialRecordConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should set default iuran_perwarga to 0", 
            tableSql.contains("${FinancialRecordConstraints.Columns.IURAN_PERWARGA} INTEGER NOT NULL DEFAULT 0"))
        assertTrue("TABLE_SQL should set default jumlah_iuran_bulanan to 0", 
            tableSql.contains("${FinancialRecordConstraints.Columns.JUMLAH_IURAN_BULANAN} INTEGER NOT NULL DEFAULT 0"))
        assertTrue("TABLE_SQL should set default total_iuran_individu to 0", 
            tableSql.contains("${FinancialRecordConstraints.Columns.TOTAL_IURAN_INDIVIDU} INTEGER NOT NULL DEFAULT 0"))
        assertTrue("TABLE_SQL should set default pengeluaran_iuran_warga to 0", 
            tableSql.contains("${FinancialRecordConstraints.Columns.PENGELUARAN_IURAN_WARGA} INTEGER NOT NULL DEFAULT 0"))
        assertTrue("TABLE_SQL should set default total_iuran_rekap to 0", 
            tableSql.contains("${FinancialRecordConstraints.Columns.TOTAL_IURAN_REKAP} INTEGER NOT NULL DEFAULT 0"))
    }

    @Test
    fun `TABLE_SQL should set default is_deleted to 0`() {
        val tableSql = FinancialRecordConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should set default is_deleted to 0", 
            tableSql.contains("${FinancialRecordConstraints.Columns.IS_DELETED} INTEGER NOT NULL DEFAULT 0"))
    }

    @Test
    fun `TABLE_SQL should set default timestamp for created_at`() {
        val tableSql = FinancialRecordConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should set default for created_at", 
            tableSql.contains("${FinancialRecordConstraints.Columns.CREATED_AT} INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))"))
    }

    @Test
    fun `TABLE_SQL should set default timestamp for updated_at`() {
        val tableSql = FinancialRecordConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should set default for updated_at", 
            tableSql.contains("${FinancialRecordConstraints.Columns.UPDATED_AT} INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))"))
    }

    @Test
    fun `TABLE_SQL should have foreign key to users table`() {
        val tableSql = FinancialRecordConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should contain FOREIGN KEY", tableSql.contains("FOREIGN KEY"))
        assertTrue("TABLE_SQL should reference user_id column", 
            tableSql.contains("FOREIGN KEY(${FinancialRecordConstraints.Columns.USER_ID})"))
        assertTrue("TABLE_SQL should reference users table", 
            tableSql.contains("REFERENCES ${UserConstraints.TABLE_NAME}(${UserConstraints.Columns.ID})"))
    }

    @Test
    fun `TABLE_SQL should enforce CASCADE delete on foreign key`() {
        val tableSql = FinancialRecordConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should contain ON DELETE CASCADE", tableSql.contains("ON DELETE CASCADE"))
    }

    @Test
    fun `TABLE_SQL should enforce CASCADE update on foreign key`() {
        val tableSql = FinancialRecordConstraints.TABLE_SQL

        assertTrue("TABLE_SQL should contain ON UPDATE CASCADE", tableSql.contains("ON UPDATE CASCADE"))
    }

    @Test
    fun `INDEX_USER_ID_SQL should create index on user_id column`() {
        val indexSql = FinancialRecordConstraints.INDEX_USER_ID_SQL

        assertTrue("INDEX_USER_ID_SQL should contain CREATE INDEX", indexSql.contains("CREATE INDEX"))
        assertTrue("INDEX_USER_ID_SQL should contain index name", indexSql.contains(FinancialRecordConstraints.Indexes.IDX_USER_ID))
        assertTrue("INDEX_USER_ID_SQL should contain financial_records table name", indexSql.contains(FinancialRecordConstraints.TABLE_NAME))
        assertTrue("INDEX_USER_ID_SQL should reference user_id column", indexSql.contains(FinancialRecordConstraints.Columns.USER_ID))
    }

    @Test
    fun `INDEX_UPDATED_AT_SQL should create index on updated_at column with DESC`() {
        val indexSql = FinancialRecordConstraints.INDEX_UPDATED_AT_SQL

        assertTrue("INDEX_UPDATED_AT_SQL should contain CREATE INDEX", indexSql.contains("CREATE INDEX"))
        assertTrue("INDEX_UPDATED_AT_SQL should contain index name", indexSql.contains(FinancialRecordConstraints.Indexes.IDX_UPDATED_AT))
        assertTrue("INDEX_UPDATED_AT_SQL should contain financial_records table name", indexSql.contains(FinancialRecordConstraints.TABLE_NAME))
        assertTrue("INDEX_UPDATED_AT_SQL should reference updated_at column", indexSql.contains(FinancialRecordConstraints.Columns.UPDATED_AT))
        assertTrue("INDEX_UPDATED_AT_SQL should contain DESC", indexSql.contains("DESC"))
    }

    @Test
    fun `INDEX_USER_REKAP_SQL should create composite index on user_id and total_iuran_rekap`() {
        val indexSql = FinancialRecordConstraints.INDEX_USER_REKAP_SQL

        assertTrue("INDEX_USER_REKAP_SQL should contain CREATE INDEX", indexSql.contains("CREATE INDEX"))
        assertTrue("INDEX_USER_REKAP_SQL should contain index name", indexSql.contains(FinancialRecordConstraints.Indexes.IDX_USER_REKAP))
        assertTrue("INDEX_USER_REKAP_SQL should contain financial_records table name", indexSql.contains(FinancialRecordConstraints.TABLE_NAME))
        assertTrue("INDEX_USER_REKAP_SQL should reference user_id column", indexSql.contains(FinancialRecordConstraints.Columns.USER_ID))
        assertTrue("INDEX_USER_REKAP_SQL should reference total_iuran_rekap column", indexSql.contains(FinancialRecordConstraints.Columns.TOTAL_IURAN_REKAP))
    }

    @Test
    fun `constraint values should be reasonable`() {
        assertTrue("MAX_PEMANFAATAN_LENGTH should be positive", FinancialRecordConstraints.Constraints.MAX_PEMANFAATAN_LENGTH > 0)
        assertTrue("MAX_NUMERIC_VALUE should be positive", FinancialRecordConstraints.Constraints.MAX_NUMERIC_VALUE > 0)

        assertTrue("MAX_PEMANFAATAN_LENGTH should be at least 100", FinancialRecordConstraints.Constraints.MAX_PEMANFAATAN_LENGTH >= 100)
        assertTrue("MAX_NUMERIC_VALUE should be at least 1000000", FinancialRecordConstraints.Constraints.MAX_NUMERIC_VALUE >= 1000000)
    }
}
