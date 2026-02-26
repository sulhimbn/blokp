package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnnouncementViewModel @Inject constructor() : ViewModel() {

    private val _announcementsState = MutableStateFlow<UiState<List<Announcement>>>(UiState.Loading)
    val announcementsState: StateFlow<UiState<List<Announcement>>> = _announcementsState

    init {
        loadAnnouncements()
    }

    fun loadAnnouncements() {
        viewModelScope.launch {
            _announcementsState.value = UiState.Loading
            try {
                val response = ApiConfig.getApiService().getAnnouncements()
                if (response.isSuccessful) {
                    val announcements = response.body()
                    if (announcements != null) {
                        _announcementsState.value = UiState.Success(announcements)
                    } else {
                        _announcementsState.value = UiState.Error("No announcements available")
                    }
                } else {
                    _announcementsState.value = UiState.Error("Failed to load announcements")
                }
            } catch (e: Exception) {
                _announcementsState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}
