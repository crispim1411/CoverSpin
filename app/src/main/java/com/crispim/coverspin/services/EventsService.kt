package com.crispim.coverspin.services

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
                if (intent.action == Intent.ACTION_USER_PRESENT || intent.action == Intent.ACTION_SCREEN_ON) {
                    if (!EngineActivity.isOverlayActive)
                        EngineActivity.initialize(context!!)
                    loadRotation()
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
        try {
            val shouldRotate = loadUserPrefRotation()
            if (!setRotationEnabled(shouldRotate)) {
                toastHelper.show("Error set rotation", LogLevel.Debug)
                setNewUserPrefRotation(true)
            } else {
                toastHelper.show("Loaded $shouldRotate", LogLevel.Debug)
            }
        } catch (e: Exception) {
            toastHelper.show("loadRotation Error: ${e.message}", LogLevel.Error)
        }
    }
}