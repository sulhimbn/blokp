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

    override suspend fun getAnnouncements(forceRefresh: Boolean): OperationResult<List<Announcement>> {
        if (!forceRefresh && cache.isNotEmpty()) {
            return OperationResult.Success(cache.values.toList())
        }

        return executeWithCircuitBreakerV2 { apiService.getAnnouncements() }
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
}
