package com.example.iurankomplek.utils

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Collections
import java.util.LinkedHashMap

/**
 * Thread-safe in-memory cache manager with TTL and LRU eviction support.
 * Provides caching functionality for repository data to reduce API calls
 * and improve app performance.
 * 
 * Features:
 * - TTL-based expiration
 * - LRU eviction when capacity is reached
 * - Configurable maximum cache size
 */
class CacheManager private constructor() {

    // Thread-safe LRU cache using synchronized LinkedHashMap with accessOrder=true
    // This provides O(1) get and O(1) LRU eviction
    private val cache: MutableMap<String, CacheEntry<*>> = Collections.synchronizedMap(
        object : LinkedHashMap<String, CacheEntry<*>>(16, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry<*>>?): Boolean {
                // This is called under synchronization, safe to check size
                return size > maxCacheSize
            }
        }
    )
    private val mutex = Mutex()

    companion object {
        private const val TAG = "CacheManager"
        
        // Default TTL: 5 minutes
        const val DEFAULT_TTL_MS = 5 * 60 * 1000L
        
        // Default maximum cache size: 100 entries
        const val DEFAULT_MAX_CACHE_SIZE = 100
        
        // Maximum cache size - can be configured via builder or setter
        var maxCacheSize: Int = DEFAULT_MAX_CACHE_SIZE
            private set
        
        // Singleton instance
        @Volatile
        private var instance: CacheManager? = null

        fun getInstance(): CacheManager {
            return instance ?: synchronized(this) {
                instance ?: CacheManager().also { instance = it }
            }
        }

        /**
         * Sets the maximum cache size. Should be called before any cache operations.
         * @param size Maximum number of entries to store in cache
         */
        fun setMaxCacheSize(size: Int) {
            if (size > 0) {
                maxCacheSize = size
                Log.d(TAG, "Max cache size set to: $size")
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
     * If cache is at capacity, LRU entry will be evicted.
     */
    fun <T> put(key: String, value: T, ttlMs: Long) {
        val expiryTime = System.currentTimeMillis() + ttlMs
        val entry = CacheEntry(value, expiryTime)
        
        synchronized(cache) {
            // LinkedHashMap.removeEldestEntry handles LRU eviction automatically
            // when we add a new entry that would exceed maxCacheSize
            cache[key] = entry
        }
        
        Log.d(TAG, "Cached entry for key: $key, TTL: ${ttlMs}ms, cache size: ${cache.size}")
    }

    /**
     * Retrieves a value from the cache if it exists and hasn't expired.
     * Returns null if the key doesn't exist or has expired.
     *
     * NOTE: UNCHECKED_CAST is required due to Kotlin type erasure.
     * CacheEntry<*> cannot be cast to CacheEntry<T> at runtime.
     * This is a fundamental Kotlin/Java generic limitation.
     * Safety: The cast is safe because we control the cache internally.
     */
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
                synchronized(cache) {
                    cache.remove(key)
                }
                return@withLock null
            }

            // get() automatically moves this entry to end due to accessOrder=true
            Log.d(TAG, "Cache hit for key: $key")
            entry.value
        }
    }

    /**
     * Retrieves a value from the cache without using mutex (for synchronous access).
     * Use this when you're already in a synchronized context.
     *
     * NOTE: UNCHECKED_CAST is required due to Kotlin type erasure.
     * CacheEntry<*> cannot be cast to CacheEntry<T> at runtime.
     * This is a fundamental Kotlin/Java generic limitation.
     * Safety: The cast is safe because we control the cache internally.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getSync(key: String): T? {
        val entry = cache[key] as CacheEntry<T>?
        if (entry == null) {
            return null
        }

        if (System.currentTimeMillis() > entry.expiryTime) {
            synchronized(cache) {
                cache.remove(key)
            }
            return null
        }

        // getSync also updates access order for LRU
        // Access via map.get() triggers LinkedHashMap's recordAccess
        cache[key] = entry

        return entry.value
    }

    /**
     * Removes a specific entry from the cache.
     */
    suspend fun remove(key: String) {
        mutex.withLock {
            synchronized(cache) {
                cache.remove(key)
            }
            Log.d(TAG, "Removed cache entry for key: $key")
        }
    }

    /**
     * Clears all entries from the cache.
     */
    suspend fun clear() {
        mutex.withLock {
            synchronized(cache) {
                cache.clear()
            }
            Log.d(TAG, "Cache cleared")
        }
    }

    /**
     * Synchronously clears all entries from the cache.
     */
    fun clearSync() {
        synchronized(cache) {
            cache.clear()
        }
        Log.d(TAG, "Cache cleared (sync)")
    }

    /**
     * Checks if a key exists in the cache and hasn't expired.
     */
    fun contains(key: String): Boolean {
        val entry = cache[key] ?: return false
        if (System.currentTimeMillis() > entry.expiryTime) {
            synchronized(cache) {
                cache.remove(key)
            }
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
            val expiredKeys = synchronized(cache) {
                cache.entries
                    .filter { currentTime > it.value.expiryTime }
                    .map { it.key }
                    .toList()
            }
            
            expiredKeys.forEach { key ->
                synchronized(cache) {
                    cache.remove(key)
                }
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
