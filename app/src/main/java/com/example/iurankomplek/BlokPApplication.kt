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
        // Load SQLCipher native library for encrypted Room database. Guarded because
        // JVM unit tests have no native library on java.library.path.
        runCatching { System.loadLibrary("sqlcipher") }
            .onFailure { Log.w(TAG, "SQLCipher native library unavailable", it) }
    }

    private companion object {
        const val TAG = "BlokPApplication"
    }
}
