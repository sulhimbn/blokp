package com.example.iurankomplek.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.iurankomplek.model.User

interface SessionStore {
    fun load(): User?
    fun save(user: User)
    fun clear()
}

class EncryptedSessionStore(context: Context) : SessionStore {

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

    override fun load(): User? {
        if (!encryptedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)) return null
        val userId = encryptedPrefs.getString(KEY_USER_ID, null) ?: return null
        val email = encryptedPrefs.getString(KEY_USER_EMAIL, null) ?: return null
        val firstName = encryptedPrefs.getString(KEY_USER_FIRST_NAME, null) ?: return null
        val lastName = encryptedPrefs.getString(KEY_USER_LAST_NAME, null) ?: return null
        return User(
            id = userId,
            email = email,
            firstName = firstName,
            lastName = lastName,
            avatar = encryptedPrefs.getString(KEY_USER_AVATAR, null)
        )
    }

    override fun save(user: User) {
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

    override fun clear() {
        encryptedPrefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_FILE_NAME = "user_session_prefs"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_USER_FIRST_NAME = "user_first_name"
        const val KEY_USER_LAST_NAME = "user_last_name"
        const val KEY_USER_AVATAR = "user_avatar"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
}

class InMemorySessionStore : SessionStore {
    @Volatile
    private var stored: User? = null

    override fun load(): User? = stored

    override fun save(user: User) {
        stored = user
    }

    override fun clear() {
        stored = null
    }
}