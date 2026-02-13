package com.example.iurankomplek.data.repository

import com.example.iurankomplek.utils.Result
import kotlinx.coroutines.flow.Flow

/**
 * Base repository interface defining common operations for all repositories
 */
interface BaseRepository<T> {
    
    suspend fun getAll(): Result<List<T>>
    
    suspend fun getById(id: String): Result<T>
    
    suspend fun create(item: T): Result<T>
    
    suspend fun update(item: T): Result<T>
    
    suspend fun delete(id: String): Result<Boolean>
    
    fun observeAll(): Flow<Result<List<T>>>
}
