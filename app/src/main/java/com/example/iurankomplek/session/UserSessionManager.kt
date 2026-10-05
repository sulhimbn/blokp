package com.example.iurankomplek.session

import com.example.iurankomplek.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserSessionManager(
    private val store: SessionStore
) {
    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_USER_FIRST_NAME = "user_first_name"
        const val KEY_USER_LAST_NAME = "user_last_name"
        const val KEY_USER_AVATAR = "user_avatar"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

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
        saveUserToStore(user)
    }

    fun clearSession() {
        _currentUser.value = null
        _isLoggedIn.value = false
        store.clear()
    }

    val currentUserId: String?
        get() = _currentUser.value?.id

    private fun saveUserToStore(user: User) {
        store.putString(KEY_USER_ID, user.id)
        store.putString(KEY_USER_EMAIL, user.email)
        store.putString(KEY_USER_FIRST_NAME, user.firstName)
        store.putString(KEY_USER_LAST_NAME, user.lastName)
        store.putString(KEY_USER_AVATAR, user.avatar)
        store.putBoolean(KEY_IS_LOGGED_IN, true)
    }

    private fun restoreSession() {
        if (!store.getBoolean(KEY_IS_LOGGED_IN, false)) return

        val userId = store.getString(KEY_USER_ID)
        val email = store.getString(KEY_USER_EMAIL)
        val firstName = store.getString(KEY_USER_FIRST_NAME)
        val lastName = store.getString(KEY_USER_LAST_NAME)
        val avatar = store.getString(KEY_USER_AVATAR)

        if (userId != null && email != null && firstName != null && lastName != null) {
            _currentUser.value = User(
                id = userId,
                email = email,
                firstName = firstName,
                lastName = lastName,
                avatar = avatar
            )
            _isLoggedIn.value = true
        }
    }
}