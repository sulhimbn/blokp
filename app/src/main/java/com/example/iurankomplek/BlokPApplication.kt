package com.example.iurankomplek

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class with Hilt dependency injection support.
 * This is the entry point for the app's dependency injection graph.
 */
@HiltAndroidApp
class BlokPApplication : Application()