package com.example.iurankomplek.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

/** Supplies the [SharedPreferences] backing [UserSessionManager]. */
interface SessionPreferencesProvider {
    fun create(): SharedPreferences
}

class EncryptedSessionPreferencesProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : SessionPreferencesProvider {
    override fun create(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        const val PREFS_FILE_NAME = "user_session_prefs"
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {
    @Binds
    @Singleton
    abstract fun bindSessionPreferencesProvider(
        impl: EncryptedSessionPreferencesProvider
    ): SessionPreferencesProvider
}
