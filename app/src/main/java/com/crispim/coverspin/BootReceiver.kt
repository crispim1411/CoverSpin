package com.crispim.coverspin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DISPLAY_SERVICE
import android.content.Intent
import android.hardware.display.DisplayManager
import android.view.Display

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val displayManager = context.getSystemService(DISPLAY_SERVICE) as DisplayManager
            displayManager.getDisplay(1)
                ?.takeIf { it.state == Display.STATE_ON || it.state == Display.STATE_DOZE }
                ?.let { EngineActivity.initialize(context) }
        }
    }
}