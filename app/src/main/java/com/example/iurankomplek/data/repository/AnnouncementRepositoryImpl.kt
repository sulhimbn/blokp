package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.network.ApiServiceV1
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

class AnnouncementRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiServiceV1
) : AnnouncementRepository, BaseRepository() {
    private val cache = ConcurrentHashMap<String, Announcement>()

    private val announcementFallbackManager = FallbackManager<List<Announcement>>(
        fallbackStrategy = CachedAnnouncementsFallback(cache),
        config = FallbackConfig(enableFallback = true, fallbackTimeoutMs = 5000L, logFallbackUsage = true)
    )

    override suspend fun getAnnouncements(forceRefresh: Boolean): OperationResult<List<Announcement>> {
        return announcementFallbackManager.executeWithFallback(
            primaryOperation = {
                if (!forceRefresh && cache.isNotEmpty()) {
                    OperationResult.Success(cache.values.toList())
                } else {
                    executeWithCircuitBreakerV2 { apiService.getAnnouncements() }
                }
            }
        )
    }

    override suspend fun getCachedAnnouncements(): OperationResult<List<Announcement>> {
        return try {
            OperationResult.Success(cache.values.toList())
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Unknown error")
        }
    }

    override suspend fun clearCache(): OperationResult<Unit> {
        return try {
            cache.clear()
            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Unknown error")
        }
    }

    private class CachedAnnouncementsFallback(private val cache: ConcurrentHashMap<String, Announcement>) : CachedDataFallback<List<Announcement>>() {
        override suspend fun getCachedData(): List<Announcement>? {
            return try {
                cache.values.toList().takeIf { it.isNotEmpty() }
            } catch (e: Exception) {
                null
            }
        }
    }
}
