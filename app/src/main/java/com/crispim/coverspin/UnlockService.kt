package com.crispim.coverspin

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.accessibility.AccessibilityEvent

@SuppressLint("AccessibilityPolicy")
class UnlockService : AccessibilityService() {
    private var unlockReceiver: BroadcastReceiver? = null
    private lateinit var toastHelper: ToastHelper
    private var isFirstAlert: Boolean = true
    private var tries: Int = 0

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
        tries = 0
        unlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                try {
                    val displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager
                    if (displayManager.getDisplay(1)?.state != Display.STATE_ON)
                        return

                    val action = intent.action
                    if (action == Intent.ACTION_SCREEN_ON || action == Intent.ACTION_USER_PRESENT) {
                        if (!EngineActivity.isOverlayActive) {
                            EngineActivity.initialize(application)
                            tries += 1

                            if (tries >= 2) {
                                if (isFirstAlert) {
                                    toastHelper.show("Check if your app was added to GoodLock")
                                    isFirstAlert = false
                                }
                                tries = 0
                            }
                        } else {
                            tries = 0
                        }
                    }
                } catch (e: Exception) {
                    if (EngineActivity.trackLogsEnabled) {
                        toastHelper.show("unlock error: ${e.message}")
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(unlockReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (unlockReceiver != null) unregisterReceiver(unlockReceiver)
    }
}
