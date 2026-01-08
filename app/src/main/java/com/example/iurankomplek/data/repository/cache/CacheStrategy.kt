package com.example.iurankomplek.data.repository.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Strategy interface for repository caching.
 * Allows pluggable cache implementations (in-memory, database, no cache).
 *
 * @param T Type of cached data
 */
interface CacheStrategy<T> {
    suspend fun get(key: String?): T?
    suspend fun put(key: String?, value: T)
    suspend fun isValid(cachedValue: T?, forceRefresh: Boolean): Boolean
    suspend fun clear()
}

/**
 * In-memory cache strategy using ConcurrentHashMap.
 * Suitable for simple repositories with short-lived data.
 *
 * Thread-safe: Uses ConcurrentHashMap + Mutex for read/write safety
 * Performance: Fast memory access, no I/O overhead
 * Limitations: Data lost on app restart
 */
class InMemoryCacheStrategy<T> : CacheStrategy<T> {
    private val cache = ConcurrentHashMap<String, T>()
    private val mutex = Mutex()

    override suspend fun get(key: String?): T? {
        return if (key != null) cache[key] else null
    }

    override suspend fun put(key: String?, value: T) {
        if (key != null) {
            mutex.withLock {
                cache[key] = value
            }
        }
    }

    override suspend fun isValid(cachedValue: T?, forceRefresh: Boolean): Boolean {
        return !forceRefresh && cachedValue != null
    }

    override suspend fun clear() {
        mutex.withLock {
            cache.clear()
        }
    }
}

/**
 * No-cache strategy for API-only repositories.
 * Always fetches from network, never caches.
 *
 * Use case: Real-time data, one-time operations
 */
class NoCacheStrategy<T> : CacheStrategy<T> {
    override suspend fun get(key: String?): T? = null

    override suspend fun put(key: String?, value: T) {
    }

    override suspend fun isValid(cachedValue: T?, forceRefresh: Boolean): Boolean {
        return false
    }

    override suspend fun clear() {
    }
}
