package com.example.iurankomplek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.data.repository.AnnouncementRepository
import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnnouncementViewModel(
    private val announcementRepository: AnnouncementRepository
) : ViewModel() {

    private val _announcementsState = MutableStateFlow<UiState<List<Announcement>>>(UiState.Loading)
    val announcementsState: StateFlow<UiState<List<Announcement>>> = _announcementsState

    fun loadAnnouncements(forceRefresh: Boolean = false) {
        if (_announcementsState.value is UiState.Loading) return

        viewModelScope.launch {
            _announcementsState.value = UiState.Loading
            announcementRepository.getAnnouncements(forceRefresh)
                .onSuccess { announcements ->
                    _announcementsState.value = UiState.Success(announcements)
                }
                .onFailure { exception ->
                    _announcementsState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                }
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