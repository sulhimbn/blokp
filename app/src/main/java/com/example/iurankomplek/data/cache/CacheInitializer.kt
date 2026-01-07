package com.example.iurankomplek.data.cache

import android.app.Application

class CacheInitializer : Application() {
    
    override fun onCreate() {
        super.onCreate()
        CacheManager.initialize(applicationContext)
    }
}
