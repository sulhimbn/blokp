package com.example.iurankomplek.utils

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe in-memory cache manager with TTL support.
 * Provides caching functionality for repository data to reduce API calls
 * and improve app performance.
 */
class CacheManager private constructor() {

    private val cache = ConcurrentHashMap<String, CacheEntry<*>>()
    private val mutex = Mutex()

    companion object {
        private const val TAG = "CacheManager"
        
        // Default TTL: 5 minutes
        const val DEFAULT_TTL_MS = 5 * 60 * 1000L
        
        // Singleton instance
        @Volatile
        private var instance: CacheManager? = null

        fun getInstance(): CacheManager {
            return instance ?: synchronized(this) {
                instance ?: CacheManager().also { instance = it }
            }
        }
    }

    /**
     * Stores a value in the cache with a default TTL.
     */
    fun <T> put(key: String, value: T) {
        put(key, value, DEFAULT_TTL_MS)
    }

    /**
     * Stores a value in the cache with a specified TTL.
     */
    fun <T> put(key: String, value: T, ttlMs: Long) {
        val expiryTime = System.currentTimeMillis() + ttlMs
        val entry = CacheEntry(value, expiryTime)
        cache[key] = entry
        Log.d(TAG, "Cached entry for key: $key, TTL: ${ttlMs}ms")
    }

    /**
     * Retrieves a value from the cache if it exists and hasn't expired.
     * Returns null if the key doesn't exist or has expired.
     */
    // Legitimate: Type erasure in generic cache. CacheEntry<T> cannot be reified at runtime.
    @Suppress("UNCHECKED_CAST")
    suspend fun <T> get(key: String): T? {
        return mutex.withLock {
            val entry = cache[key] as CacheEntry<T>?
            if (entry == null) {
                Log.d(TAG, "Cache miss for key: $key")
                return@withLock null
            }

            if (System.currentTimeMillis() > entry.expiryTime) {
                Log.d(TAG, "Cache expired for key: $key")
                cache.remove(key)
                return@withLock null
            }

            Log.d(TAG, "Cache hit for key: $key")
            entry.value
        }
    }

    /**
     * Retrieves a value from the cache without using mutex (for synchronous access).
     * Use this when you're already in a synchronized context.
     */
    // Legitimate: Type erasure in generic cache. CacheEntry<T> cannot be reified at runtime.
    @Suppress("UNCHECKED_CAST")
    fun <T> getSync(key: String): T? {
        val entry = cache[key] as CacheEntry<T>?
        if (entry == null) {
            return null
        }

        if (System.currentTimeMillis() > entry.expiryTime) {
            cache.remove(key)
            return null
        }

        return entry.value
    }

    /**
     * Removes a specific entry from the cache.
     */
    suspend fun remove(key: String) {
        mutex.withLock {
            cache.remove(key)
            Log.d(TAG, "Removed cache entry for key: $key")
        }
    }

    /**
     * Clears all entries from the cache.
     */
    suspend fun clear() {
        mutex.withLock {
            cache.clear()
            Log.d(TAG, "Cache cleared")
        }
    }

    /**
     * Synchronously clears all entries from the cache.
     */
    fun clearSync() {
        cache.clear()
        Log.d(TAG, "Cache cleared (sync)")
    }

    /**
     * Checks if a key exists in the cache and hasn't expired.
     */
    fun contains(key: String): Boolean {
        val entry = cache[key] ?: return false
        if (System.currentTimeMillis() > entry.expiryTime) {
            cache.remove(key)
            return false
        }
        return true
    }

    /**
     * Gets the number of entries in the cache.
     */
    fun size(): Int = cache.size

    /**
     * Removes all expired entries from the cache.
     */
    suspend fun evictExpired() {
        mutex.withLock {
            val currentTime = System.currentTimeMillis()
            val expiredKeys = cache.entries
                .filter { currentTime > it.value.expiryTime }
                .map { it.key }
            
            expiredKeys.forEach { key ->
                cache.remove(key)
            }
            
            if (expiredKeys.isNotEmpty()) {
                Log.d(TAG, "Evicted ${expiredKeys.size} expired cache entries")
            }
        }
    }

    /**
     * Internal data class representing a cache entry with expiry time.
     */
    private data class CacheEntry<T>(
        val value: T,
        val expiryTime: Long
    )
}
