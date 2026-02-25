package com.example.iurankomplek

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import net.sqlcipher.database.SQLiteDatabase

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
        // Load SQLCipher native library for encrypted Room database
        SQLiteDatabase.loadLibs(this)
    }
}

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class with Hilt dependency injection support.
 * This is the entry point for the app's dependency injection graph.
 */
@HiltAndroidApp
class BlokPApplication : Application()