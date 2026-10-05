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
        // Robolectric runs on the JVM where the .so is absent; fail later at DB open, not at startup.
        runCatching { System.loadLibrary("sqlcipher") }
            .onFailure { Log.w("BlokPApplication", "SQLCipher native library unavailable", it) }
    }
}
