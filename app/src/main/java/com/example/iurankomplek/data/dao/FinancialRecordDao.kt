package com.example.iurankomplek.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialRecordDao {
    @Query("SELECT * FROM financial_records WHERE is_deleted = 0 ORDER BY updated_at DESC")
    fun getAllFinancialRecords(): Flow<List<FinancialRecordEntity>>

    @Query("SELECT * FROM financial_records WHERE id = :recordId AND is_deleted = 0 LIMIT 1")
    suspend fun getFinancialRecordById(recordId: Long): FinancialRecordEntity?

    @Query("SELECT * FROM financial_records WHERE user_id = :userId AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getFinancialRecordsByUserId(userId: Long): Flow<List<FinancialRecordEntity>>

    @Query("SELECT * FROM financial_records WHERE user_id = :userId AND is_deleted = 0 ORDER BY updated_at DESC LIMIT 1")
    suspend fun getLatestFinancialRecordByUserId(userId: Long): FinancialRecordEntity?

    @Query("SELECT * FROM financial_records WHERE pemanfaatan_iuran LIKE '%' || :query || '%' AND is_deleted = 0 ORDER BY updated_at DESC")
    fun searchFinancialRecordsByPemanfaatan(query: String): Flow<List<FinancialRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: FinancialRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<FinancialRecordEntity>): List<Long>

    @Update
    suspend fun update(record: FinancialRecordEntity)

    @Query("UPDATE financial_records SET is_deleted = 1, updated_at = strftime('%s', 'now') WHERE id = :recordId")
    suspend fun softDeleteById(recordId: Long)

    @Query("UPDATE financial_records SET is_deleted = 1, updated_at = strftime('%s', 'now') WHERE user_id = :userId")
    suspend fun softDeleteByUserId(userId: Long)

    @Query("UPDATE financial_records SET is_deleted = 0, updated_at = strftime('%s', 'now') WHERE id = :recordId")
    suspend fun restoreById(recordId: Long)

    @Query("SELECT * FROM financial_records WHERE is_deleted = 1 ORDER BY updated_at DESC")
    fun getDeletedFinancialRecords(): Flow<List<FinancialRecordEntity>>

    @Delete
    suspend fun delete(record: FinancialRecordEntity)

    @Query("DELETE FROM financial_records WHERE id = :recordId")
    suspend fun deleteById(recordId: Long)

    @Query("DELETE FROM financial_records WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: Long)

    @Query("DELETE FROM financial_records")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM financial_records WHERE is_deleted = 0")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM financial_records WHERE user_id = :userId AND is_deleted = 0")
    suspend fun getCountByUserId(userId: Long): Int

    @Query("SELECT SUM(total_iuran_rekap) FROM financial_records WHERE user_id = :userId AND is_deleted = 0")
    suspend fun getTotalRekapByUserId(userId: Long): Long?

    @Query("SELECT * FROM financial_records WHERE updated_at >= :since AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getFinancialRecordsUpdatedSince(since: Long): Flow<List<FinancialRecordEntity>>

    @Query("SELECT * FROM financial_records WHERE user_id IN (:userIds) AND is_deleted = 0 ORDER BY user_id, updated_at DESC")
    suspend fun getFinancialRecordsByUserIds(userIds: List<Long>): List<FinancialRecordEntity>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAll(records: List<FinancialRecordEntity>)
}
