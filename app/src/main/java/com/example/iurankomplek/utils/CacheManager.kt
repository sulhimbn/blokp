package com.example.iurankomplek.utils

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Thread-safe in-memory cache manager with TTL support and LRU eviction.
 * Provides caching functionality for repository data to reduce API calls
 * and improve app performance.
 *
 * Features:
 * - TTL (Time-To-Live) support for entries
 * - LRU (Least Recently Used) eviction when max size is reached
 * - Thread-safe operations using Mutex
 * - Cache statistics tracking
 */
class CacheManager private constructor(private val maxSize: Int = DEFAULT_MAX_SIZE) {

    // Using LinkedHashMap for LRU support - maintains access order
    private val cache = object : LinkedHashMap<String, CacheEntry<*>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry<*>>?): Boolean {
            return size > maxSize
        }
    }
    private val mutex = Mutex()

    companion object {
        private const val TAG = "CacheManager"

        // Default TTL: 5 minutes
        const val DEFAULT_TTL_MS = 5 * 60 * 1000L

        // Default max cache size: 100 entries
        const val DEFAULT_MAX_SIZE = 100

        // Singleton instance with default max size
        @Volatile
        private var instance: CacheManager? = null

        fun getInstance(): CacheManager {
            return instance ?: synchronized(this) {
                instance ?: CacheManager(DEFAULT_MAX_SIZE).also { instance = it }
            }
        }

        /**
         * Get singleton instance with custom max size.
         * Note: Only effective on first call, subsequent calls use the same instance.
         */
        fun getInstance(maxSize: Int): CacheManager {
            return instance ?: synchronized(this) {
                instance ?: CacheManager(maxSize).also { instance = it }
            }
        }
    }

    // Cache statistics
    @Volatile private var hitCount = 0
    @Volatile private var missCount = 0
    @Volatile private var evictionCount = 0

    /**
     * Stores a value in the cache with a default TTL.
     */
    fun <T> put(key: String, value: T) {
        put(key, value, DEFAULT_TTL_MS)
    }

    /**
     * Stores a value in the cache with a specified TTL.
     * If cache is full, evicts least recently used entry before adding new one.
     */
    fun <T> put(key: String, value: T, ttlMs: Long) {
        val expiryTime = System.currentTimeMillis() + ttlMs
        val entry = CacheEntry(value, expiryTime)

        // Use tryLock to avoid blocking if mutex is held (e.g., from evictExpired)
        if (mutex.tryLock()) {
            try {
                // Check if we need to evict before adding
                if (cache.size >= maxSize && !cache.containsKey(key)) {
                    evictLRU()
                }
                cache[key] = entry
                Log.d(TAG, "Cached entry for key: $key, TTL: ${ttlMs}ms, size: ${cache.size}/$maxSize")
            } finally {
                mutex.unlock()
            }
        } else {
            // If can't acquire lock, just put (avoid deadlocks)
            cache[key] = entry
            Log.d(TAG, "Cached entry for key: $key (lock not acquired), TTL: ${ttlMs}ms")
        }
    }

    /**
     * Evicts the least recently used entry from the cache.
     */
    private fun evictLRU() {
        val eldestKey = cache.keys.firstOrNull()
        if (eldestKey != null) {
            cache.remove(eldestKey)
            evictionCount++
            Log.d(TAG, "Evicted LRU entry for key: $eldestKey")
        }
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
                missCount++
                Log.d(TAG, "Cache miss for key: $key")
                return@withLock null
            }

            if (System.currentTimeMillis() > entry.expiryTime) {
                Log.d(TAG, "Cache expired for key: $key")
                cache.remove(key)
                missCount++
                return@withLock null
            }

            // Moving the accessed entry to the end (most recently used)
            // This is handled automatically by LinkedHashMap with accessOrder=true
            hitCount++
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
            missCount++
            return null
        }

        if (System.currentTimeMillis() > entry.expiryTime) {
            cache.remove(key)
            missCount++
            return null
        }

        hitCount++
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
     * Clears all entries from the cache and resets statistics.
     */
    suspend fun clear() {
        mutex.withLock {
            val size = cache.size
            cache.clear()
            hitCount = 0
            missCount = 0
            evictionCount = 0
            Log.d(TAG, "Cache cleared (removed $size entries)")
        }
    }

    /**
     * Synchronously clears all entries from the cache and resets statistics.
     */
    fun clearSync() {
        val size = cache.size
        cache.clear()
        hitCount = 0
        missCount = 0
        evictionCount = 0
        Log.d(TAG, "Cache cleared (sync, removed $size entries)")
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
     * Gets the maximum cache size.
     */
    fun getMaxSize(): Int = maxSize

    /**
     * Gets cache hit count.
     */
    fun getHitCount(): Int = hitCount

    /**
     * Gets cache miss count.
     */
    fun getMissCount(): Int = missCount

    /**
     * Gets the number of entries evicted due to LRU policy.
     */
    fun getEvictionCount(): Int = evictionCount

    /**
     * Gets cache hit rate as a percentage (0-100).
     */
    fun getHitRate(): Float {
        val total = hitCount + missCount
        return if (total > 0) (hitCount.toFloat() / total * 100) else 0f
    }

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
     * Forces immediate LRU eviction if cache is at capacity.
     * Call this before adding new entries if you're not using put().
     */
    suspend fun evictIfNeeded() {
        mutex.withLock {
            while (cache.size >= maxSize) {
                evictLRU()
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
