package com.example.premiumlauncher.lockscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class ScreenOnReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_SCREEN_ON) {
            // Start the lock screen activity
            val lockScreenIntent = Intent(context, LockScreenActivity::class.java)
            lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            ContextCompat.startForegroundService(context, lockScreenIntent)
        }
    }
}