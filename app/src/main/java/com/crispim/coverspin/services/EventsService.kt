package com.crispim.coverspin.services

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.view.accessibility.AccessibilityEvent
import com.crispim.coverspin.Constants
import com.crispim.coverspin.activities.EngineActivity
import com.crispim.coverspin.activities.EngineActivity.Companion.loadUserPrefRotation
import com.crispim.coverspin.activities.EngineActivity.Companion.setNewUserPrefRotation
import com.crispim.coverspin.activities.EngineActivity.Companion.setRotationEnabled
import com.crispim.coverspin.models.LogLevel

@SuppressLint("AccessibilityPolicy")
class EventsService : AccessibilityService() {

    // Services
    private lateinit var toastHelper: ToastHelper
    private lateinit var cacheHelper: CacheHelper
    private lateinit var screenStateReceiver: BroadcastReceiver

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        cacheHelper = CacheHelper(getSharedPreferences(Constants.APP_NAME, MODE_PRIVATE))
        toastHelper = ToastHelper(this, cacheHelper)

        screenStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                try {
                    if (intent.action == Intent.ACTION_USER_PRESENT || intent.action == Intent.ACTION_SCREEN_ON) {
                        loadRotation()
                    }
                } catch (e: Exception) {
                    toastHelper.show("onReceive Error: ${e.message}", LogLevel.Error)
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenStateReceiver, filter)
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(screenStateReceiver)
        } catch (e: Exception) {
            toastHelper.show( "onDestroy Error: ${e.message}", LogLevel.Error)
        }
        super.onDestroy()
    }

    private fun loadRotation() {
        val shouldRotate = loadUserPrefRotation()
        if (!setRotationEnabled(shouldRotate)) {
            toastHelper.show("Initializing $shouldRotate", LogLevel.DEBUG)
            val startIntent = Intent(this, EngineActivity::class.java)
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.startActivity(startIntent)
            if (!shouldRotate)
                setNewUserPrefRotation(true)
        } else {
            toastHelper.show("Loaded $shouldRotate", LogLevel.DEBUG)
        }
    }
}