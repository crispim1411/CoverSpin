package com.crispim.coverspin.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.crispim.coverspin.activities.EngineActivity

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            EngineActivity.initialize(context)
        }
    }
}