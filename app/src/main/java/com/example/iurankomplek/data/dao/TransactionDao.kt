package com.example.iurankomplek.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.iurankomplek.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE is_deleted = 0")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id AND is_deleted = 0")
    suspend fun getTransactionById(id: String): Transaction?

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND is_deleted = 0")
    fun getTransactionsByUserId(userId: Long): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Update
    suspend fun update(transaction: Transaction)

    @Query("UPDATE transactions SET is_deleted = 1, updated_at = strftime('%s', 'now') WHERE id = :id")
    suspend fun softDeleteById(id: String)

    @Query("UPDATE transactions SET is_deleted = 0, updated_at = strftime('%s', 'now') WHERE id = :id")
    suspend fun restoreById(id: String)

    @Query("SELECT * FROM transactions WHERE is_deleted = 1 ORDER BY updated_at DESC")
    fun getDeletedTransactions(): Flow<List<Transaction>>

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM transactions WHERE status = :status AND is_deleted = 0")
    fun getTransactionsByStatus(status: com.example.iurankomplek.payment.PaymentStatus): Flow<List<Transaction>>
}
