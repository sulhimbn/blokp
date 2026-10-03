package com.example.iurankomplek

import android.app.Application
import android.os.Build
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
        // Robolectric runs on the JVM, where the Android SQLCipher .so cannot be loaded.
        // Everywhere else a missing library is a real failure and must stay loud.
        if (Build.FINGERPRINT != "robolectric") {
            System.loadLibrary("sqlcipher")
        }
    }
}
