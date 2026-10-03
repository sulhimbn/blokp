package com.example.iurankomplek.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.iurankomplek.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionManager @Inject constructor(
    context: Context
) {
    private val context: Context = context.applicationContext
    private var prefsForTesting: SharedPreferences? = null

    /**
     * Production builds always get EncryptedSharedPreferences. The internal constructor lets
     * JVM tests supply plain preferences, because Robolectric has no AndroidKeyStore to back
     * the encrypted variant.
     */
    internal constructor(context: Context, prefsOverride: SharedPreferences) : this(context) {
        prefsForTesting = prefsOverride
    }

    companion object {
        private const val PREFS_FILE_NAME = "user_session_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_FIRST_NAME = "user_first_name"
        private const val KEY_USER_LAST_NAME = "user_last_name"
        private const val KEY_USER_AVATAR = "user_avatar"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    private val masterKey by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        prefsForTesting ?: EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    private val _isLoggedIn = MutableStateFlow(false)

    /**
     * Reads the persisted session once, on first use. Doing this in a constructor would put
     * keystore access and EncryptedSharedPreferences IO on whatever thread builds the
     * dependency graph, which is the main thread during activity startup.
     */
    private val restored: Boolean by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        restoreSession()
        true
    }

    val currentUser: StateFlow<User?>
        get() {
            restored
            return _currentUser
        }

    val isLoggedIn: StateFlow<Boolean>
        get() {
            restored
            return _isLoggedIn
        }

    fun setCurrentUser(user: User) {
        restored
        _currentUser.value = user
        _isLoggedIn.value = true
        saveUserToPrefs(user)
    }

    fun clearSession() {
        restored
        _currentUser.value = null
        _isLoggedIn.value = false
        clearUserFromPrefs()
    }

    val currentUserId: String?
        get() {
            restored
            return _currentUser.value?.id
        }

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