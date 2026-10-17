package com.example.premiumlauncher.utils

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Efficient package change service that uses throttling to reduce battery overhead
 */
class PackageChangeService : Service() {

    private val efficientService = EfficientPackageChangeService()

    override fun onCreate() {
        super.onCreate()
        efficientService.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return efficientService.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? = efficientService.onBind(intent)
}