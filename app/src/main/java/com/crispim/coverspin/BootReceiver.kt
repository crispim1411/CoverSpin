package com.crispim.coverspin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val engineIntent = Intent(context, EngineActivity::class.java)
            engineIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(engineIntent)
        }
    }
}