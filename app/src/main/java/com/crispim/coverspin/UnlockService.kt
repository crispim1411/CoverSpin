package com.crispim.coverspin

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent

class UnlockService : AccessibilityService() {
    private var unlockReceiver: BroadcastReceiver? = null
    private lateinit var toastHelper: ToastHelper


    override fun onServiceConnected() {
        super.onServiceConnected()
        toastHelper = ToastHelper(application)
        registerUnlockReceiver()

        if (!EngineActivity.isOverlayActive) {
            EngineActivity.initialize(application)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) { }

    override fun onInterrupt() { }

    private fun registerUnlockReceiver() {
        unlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                try {
                    if (intent.action == Intent.ACTION_USER_PRESENT || intent.action == Intent.ACTION_SCREEN_ON) {
                        if (!EngineActivity.isOverlayActive) {
                            EngineActivity.initialize(application)
                            toastHelper.show("initializing on unlock")
                        } else {
                            toastHelper.show("already running")
                        }
                    }
                } catch (e: Exception) {
                    toastHelper.show("unlock error: ${e.message}")
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(unlockReceiver, filter)
        toastHelper.show("events service started!")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (unlockReceiver != null) unregisterReceiver(unlockReceiver)
    }
}
