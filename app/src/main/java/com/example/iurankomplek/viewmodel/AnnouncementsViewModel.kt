package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnnouncementsViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _announcementsState = MutableStateFlow<UiState<List<Announcement>>>(UiState.Loading)
    val announcementsState: StateFlow<UiState<List<Announcement>>> = _announcementsState

    fun loadAnnouncements() {
        if (_announcementsState.value is UiState.Loading) return

        viewModelScope.launch {
            _announcementsState.value = UiState.Loading
            try {
                val response = apiService.getAnnouncements()
                if (response.isSuccessful) {
                    val announcements = response.body()
                    if (announcements != null) {
                        _announcementsState.value = UiState.Success(announcements)
                    } else {
                        _announcementsState.value = UiState.Error("No announcements available")
                    }
                } else {
                    _announcementsState.value = UiState.Error("Failed to load announcements: ${response.code()}")
                }
            } catch (e: Exception) {
                _announcementsState.value = UiState.Error("Network error: ${e.message}")
            }
        }
    }

    class Factory(
        private val apiService: ApiService
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AnnouncementsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AnnouncementsViewModel(apiService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
