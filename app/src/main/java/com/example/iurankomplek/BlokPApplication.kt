package com.example.iurankomplek

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class with Hilt dependency injection support.
 * This is the entry point for the app's dependency injection graph.
 *
 * Also loads SQLCipher native library for encrypted database support.
 */
@HiltAndroidApp
class BlokPApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        isSqlCipherAvailable = runCatching { System.loadLibrary("sqlcipher") }
            .onFailure { Log.e(TAG, "SQLCipher native library unavailable; encrypted storage is disabled", it) }
            .isSuccess
    }

    companion object {
        private const val TAG = "BlokPApplication"

        @Volatile
        @JvmStatic
        var isSqlCipherAvailable: Boolean = false
            private set
    }
}
