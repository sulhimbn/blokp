package com.example.iurankomplek.data.dao

import androidx.room.*
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialRecordDao {
    @Query("SELECT * FROM financial_records ORDER BY updated_at DESC")
    fun getAllFinancialRecords(): Flow<List<FinancialRecordEntity>>

    @Query("SELECT * FROM financial_records WHERE id = :recordId LIMIT 1")
    suspend fun getFinancialRecordById(recordId: Long): FinancialRecordEntity?

    @Query("SELECT * FROM financial_records WHERE user_id = :userId ORDER BY updated_at DESC")
    fun getFinancialRecordsByUserId(userId: Long): Flow<List<FinancialRecordEntity>>

    @Query("SELECT * FROM financial_records WHERE user_id = :userId ORDER BY updated_at DESC LIMIT 1")
    suspend fun getLatestFinancialRecordByUserId(userId: Long): FinancialRecordEntity?

    @Query("SELECT * FROM financial_records WHERE pemanfaatan_iuran LIKE '%' || :query || '%' ORDER BY updated_at DESC")
    fun searchFinancialRecordsByPemanfaatan(query: String): Flow<List<FinancialRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: FinancialRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<FinancialRecordEntity>): List<Long>

    @Update
    suspend fun update(record: FinancialRecordEntity)

    @Delete
    suspend fun delete(record: FinancialRecordEntity)

    @Query("DELETE FROM financial_records WHERE id = :recordId")
    suspend fun deleteById(recordId: Long)

    @Query("DELETE FROM financial_records WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: Long)

    @Query("DELETE FROM financial_records")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM financial_records")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM financial_records WHERE user_id = :userId")
    suspend fun getCountByUserId(userId: Long): Int

    @Query("SELECT SUM(total_iuran_rekap) FROM financial_records WHERE user_id = :userId")
    suspend fun getTotalRekapByUserId(userId: Long): Long?

    @Query("SELECT * FROM financial_records WHERE updated_at >= :since ORDER BY updated_at DESC")
    fun getFinancialRecordsUpdatedSince(since: Long): Flow<List<FinancialRecordEntity>>
}
