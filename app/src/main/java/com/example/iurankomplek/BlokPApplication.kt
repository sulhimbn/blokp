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
        try {
            System.loadLibrary("sqlcipher")
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "SQLCipher native library unavailable; encrypted storage disabled", e)
        }
    }

    private companion object {
        const val TAG = "BlokPApplication"
    }
}
