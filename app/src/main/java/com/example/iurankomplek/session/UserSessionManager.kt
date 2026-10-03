package com.example.iurankomplek.session

import com.example.iurankomplek.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionManager @Inject constructor(
    private val store: SessionStore
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        store.load()?.let { restored ->
            _currentUser.value = restored
            _isLoggedIn.value = true
        }
    }

    fun setCurrentUser(user: User) {
        _currentUser.value = user
        _isLoggedIn.value = true
        store.save(user)
    }

    fun clearSession() {
        _currentUser.value = null
        _isLoggedIn.value = false
        store.clear()
    }

    val currentUserId: String?
        get() = _currentUser.value?.id
}