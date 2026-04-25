package com.oilwatcher.monitor

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import android.content.Context
import org.osmdroid.config.Configuration

/**
 * Oil Watcher Application class.
 * @HiltAndroidApp triggers Hilt's code generation for dependency injection.
 */
@HiltAndroidApp
class OilWatcherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize osmdroid configuration globally
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName
    }
}
