package com.crispim.coverspin.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.crispim.coverspin.Constants
import com.crispim.coverspin.activities.EngineActivity

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == Intent.ACTION_PACKAGE_REPLACED) {
            
            val cacheHelper = CacheHelper(
                context.getSharedPreferences(Constants.APP_NAME, Context.MODE_PRIVATE)
            )
            
            // Only auto-start if user has enabled this option
            if (cacheHelper.isAutoStartOnBoot()) {
                // Delay initialization slightly to ensure system is ready
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    try {
                        EngineActivity.initialize(context)
                    } catch (e: Exception) {
                        // Silently fail - user can manually start if needed
                    }
                }, 2000) // 2 second delay
            }
        }
    }
}