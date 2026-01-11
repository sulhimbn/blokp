package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.AnnouncementRepository
import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.utils.OperationResult

class LoadAnnouncementsUseCase(
    private val announcementRepository: AnnouncementRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): OperationResult<List<Announcement>> {
        return try {
            announcementRepository.getAnnouncements(forceRefresh)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to load announcements")
        }
    }
}
