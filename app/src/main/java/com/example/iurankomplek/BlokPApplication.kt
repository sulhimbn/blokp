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
        loadSqlCipher()
    }

    private fun loadSqlCipher() {
        try {
            System.loadLibrary("sqlcipher")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "SQLCipher native library unavailable; encrypted storage is disabled", e)
        }
    }

    companion object {
        private const val TAG = "BlokPApplication"
    }
}
