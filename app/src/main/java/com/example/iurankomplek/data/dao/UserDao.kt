package com.example.iurankomplek.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.UserWithFinancialRecords
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE is_deleted = 0 ORDER BY last_name ASC, first_name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId AND is_deleted = 0 LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email AND is_deleted = 0 LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId AND is_deleted = 0")
    suspend fun getUserWithFinancialRecords(userId: Long): UserWithFinancialRecords?

    @Transaction
    @Query("SELECT * FROM users WHERE is_deleted = 0")
    fun getAllUsersWithFinancialRecords(): Flow<List<UserWithFinancialRecords>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>): List<Long>

    @Update
    suspend fun update(user: UserEntity)

    @Query("UPDATE users SET is_deleted = 1, updated_at = strftime('%s', 'now') WHERE id = :userId")
    suspend fun softDeleteById(userId: Long)

    @Query("UPDATE users SET is_deleted = 0, updated_at = strftime('%s', 'now') WHERE id = :userId")
    suspend fun restoreById(userId: Long)

    @Query("SELECT * FROM users WHERE is_deleted = 1 ORDER BY updated_at DESC")
    fun getDeletedUsers(): Flow<List<UserEntity>>

    @Delete
    suspend fun delete(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteById(userId: Long)

    @Query("DELETE FROM users")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM users WHERE is_deleted = 0")
    suspend fun getCount(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email AND is_deleted = 0)")
    suspend fun emailExists(email: String): Boolean

    @Query("SELECT * FROM users WHERE email IN (:emails) AND is_deleted = 0")
    suspend fun getUsersByEmails(emails: List<String>): List<UserEntity>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAll(users: List<UserEntity>)
}
