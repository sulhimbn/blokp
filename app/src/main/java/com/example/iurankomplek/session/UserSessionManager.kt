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
        restoreSession()
    }

    fun setCurrentUser(user: User) {
        _currentUser.value = user
        _isLoggedIn.value = true
        store.saveUser(user)
    }

    fun clearSession() {
        _currentUser.value = null
        _isLoggedIn.value = false
        store.clear()
    }

    val currentUserId: String?
        get() = _currentUser.value?.id

    private fun restoreSession() {
        val user = store.loadUser() ?: return
        _currentUser.value = user
        _isLoggedIn.value = true
    }
}
