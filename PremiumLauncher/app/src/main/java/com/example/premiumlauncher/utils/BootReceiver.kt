package com.example.premiumlauncher.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.startForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start the efficient package change service
            val serviceIntent = Intent(context, PackageChangeService::class.java)
            // If targeting Android Oreo and above, use startForegroundService
            context.startForegroundService(serviceIntent)
        }
    }
}