package com.example.iurankomplek.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.iurankomplek.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PREFS_FILE_NAME = "user_session_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_FIRST_NAME = "user_first_name"
        private const val KEY_USER_LAST_NAME = "user_last_name"
        private const val KEY_USER_AVATAR = "user_avatar"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
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
        saveUserToPrefs(user)
    }

    fun clearSession() {
        _currentUser.value = null
        _isLoggedIn.value = false
        clearUserFromPrefs()
    }

    val currentUserId: String?
        get() = _currentUser.value?.id

    private fun saveUserToPrefs(user: User) {
        encryptedPrefs.edit().apply {
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_FIRST_NAME, user.firstName)
            putString(KEY_USER_LAST_NAME, user.lastName)
            putString(KEY_USER_AVATAR, user.avatar)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    private fun clearUserFromPrefs() {
        encryptedPrefs.edit().clear().apply()
    }

    private fun restoreSession() {
        val isLoggedIn = encryptedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (isLoggedIn) {
            val userId = encryptedPrefs.getString(KEY_USER_ID, null)
            val email = encryptedPrefs.getString(KEY_USER_EMAIL, null)
            val firstName = encryptedPrefs.getString(KEY_USER_FIRST_NAME, null)
            val lastName = encryptedPrefs.getString(KEY_USER_LAST_NAME, null)
            val avatar = encryptedPrefs.getString(KEY_USER_AVATAR, null)

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
}