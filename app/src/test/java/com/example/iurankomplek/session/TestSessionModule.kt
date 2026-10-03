package com.example.iurankomplek.session

import android.content.Context
import android.content.SharedPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AndroidKeyStore is not available under Robolectric, so the JVM suite swaps the
 * encrypted prefs for plain ones. Production keeps [EncryptedSessionPreferencesProvider].
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [SessionModule::class])
abstract class TestSessionModule {
    @Binds
    @Singleton
    abstract fun bindSessionPreferencesProvider(
        impl: InMemorySessionPreferencesProvider
    ): SessionPreferencesProvider
}

class InMemorySessionPreferencesProvider @Inject constructor(
    private val context: Context
) : SessionPreferencesProvider {
    override fun create(): SharedPreferences =
        context.getSharedPreferences("user_session_prefs_test", Context.MODE_PRIVATE)
}
