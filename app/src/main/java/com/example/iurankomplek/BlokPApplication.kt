package com.example.iurankomplek

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class with Hilt dependency injection support.
 * This is the entry point for the app's dependency injection graph.
 *
 * Loads the SQLCipher native library for encrypted database support.
 */
@HiltAndroidApp
class BlokPApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        loadSqlCipher()
    }

    private fun loadSqlCipher() {
        if (sqlCipherLoaded) return
        try {
            System.loadLibrary(SQLCIPHER_LIBRARY)
            sqlCipherLoaded = true
        } catch (e: UnsatisfiedLinkError) {
            Log.e(
                TAG,
                "SQLCipher native library unavailable; encrypted storage is disabled for this process",
                e
            )
        }
    }

    companion object {
        private const val TAG = "BlokPApplication"
        private const val SQLCIPHER_LIBRARY = "sqlcipher"

        @Volatile
        private var sqlCipherLoaded = false

        fun isSqlCipherLoaded(): Boolean = sqlCipherLoaded
    }
}
