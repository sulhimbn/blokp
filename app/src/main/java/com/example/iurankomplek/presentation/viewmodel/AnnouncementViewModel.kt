package com.example.iurankomplek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.iurankomplek.core.base.BaseViewModel
import com.example.iurankomplek.data.repository.AnnouncementRepository
import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AnnouncementViewModel(
    private val announcementRepository: AnnouncementRepository
) : BaseViewModel() {

    private val _announcementsState = createMutableStateFlow<List<Announcement>>(UiState.Loading)
    val announcementsState: StateFlow<UiState<List<Announcement>>> = _announcementsState

    fun loadAnnouncements(forceRefresh: Boolean = false) {
        executeWithLoadingStateForResult(_announcementsState, preventDuplicate = !forceRefresh) {
            announcementRepository.getAnnouncements(forceRefresh)
        }
    }

    fun refreshAnnouncements() {
        loadAnnouncements(forceRefresh = true)
    }

    class Factory(private val announcementRepository: AnnouncementRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AnnouncementViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AnnouncementViewModel(announcementRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}