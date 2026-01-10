package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.network.ApiServiceV1
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

class AnnouncementRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiServiceV1
) : AnnouncementRepository, BaseRepository {
    private val cache = ConcurrentHashMap<String, Announcement>()
    
    override suspend fun getAnnouncements(forceRefresh: Boolean): Result<List<Announcement>> {
        if (!forceRefresh && cache.isNotEmpty()) {
            return Result.success(cache.values.toList())
        }

        return executeWithCircuitBreakerV2 { apiService.getAnnouncements() }
    }
 
    override suspend fun getCachedAnnouncements(): Result<List<Announcement>> {
        return try {
            Result.success(cache.values.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
 
    override suspend fun clearCache(): Result<Unit> {
        return try {
            cache.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
