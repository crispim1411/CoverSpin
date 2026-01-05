package com.crispim.coverspin.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.crispim.coverspin.activities.EngineActivity

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val engineIntent = Intent(context, EngineActivity::class.java)
            engineIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(engineIntent)
        }
    }
}