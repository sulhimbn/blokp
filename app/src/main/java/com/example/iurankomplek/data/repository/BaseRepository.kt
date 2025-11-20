package com.example.iurankomplek.data.repository

interface BaseRepository<T> {
    suspend fun getAll(): Result<List<T>>
    suspend fun getById(id: String): Result<T>
    suspend fun create(item: T): Result<T>
    suspend fun update(item: T): Result<T>
    suspend fun delete(id: String): Result<Unit>
}