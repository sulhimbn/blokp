package com.example.iurankomplek

import android.app.Application
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
        // Load SQLCipher native library for encrypted Room database. It only
        // exists in instrumented/device runs, so a failure here must not abort
        // startup (JVM unit tests and Robolectric load this class too).
        runCatching { System.loadLibrary("sqlcipher") }
    }
}
