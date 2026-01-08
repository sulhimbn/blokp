package com.example.iurankomplek.data.cache

import android.app.Application
import com.example.iurankomplek.di.DependencyContainer

class CacheInitializer : Application() {
    
    override fun onCreate() {
        super.onCreate()
        CacheManager.initialize(applicationContext)
        DependencyContainer.initialize(applicationContext)
    }
}
