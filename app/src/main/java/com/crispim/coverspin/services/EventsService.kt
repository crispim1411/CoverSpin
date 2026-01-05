package com.crispim.coverspin.services

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.crispim.coverspin.Constants
import com.crispim.coverspin.activities.EngineActivity
import com.crispim.coverspin.activities.EngineActivity.Companion.loadUserPrefRotation
import com.crispim.coverspin.activities.EngineActivity.Companion.setNewUserPrefRotation
import com.crispim.coverspin.activities.EngineActivity.Companion.setRotationEnabled
import com.crispim.coverspin.models.LogLevel

@SuppressLint("AccessibilityPolicy")
class EventsService : AccessibilityService() {

    private var pendingVolumeDownRunnable: Runnable? = null
    private var hasVolumeDecreased: Boolean = false

    // Services
    private lateinit var toastHelper: ToastHelper
    private lateinit var cacheHelper: CacheHelper
    private lateinit var displayManager: DisplayManager
    private lateinit var screenStateReceiver: BroadcastReceiver
    private lateinit var audioManager: AudioManager
    private val handler = Handler(Looper.getMainLooper())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        cacheHelper = CacheHelper(getSharedPreferences(Constants.APP_NAME, MODE_PRIVATE))
        displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager
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

    override fun onKeyEvent(event: KeyEvent): Boolean {
        try {
            val shortcutsEnabled = cacheHelper.isVolumeShortcutsEnabled()
            val mainDisplay = displayManager.getDisplay(0)

            if (!shortcutsEnabled
                || event.scanCode != Constants.SCAN_CODE_VOLUME_DOWN
                || event.action == KeyEvent.ACTION_DOWN
                || mainDisplay?.state == Display.STATE_ON) {
                return super.onKeyEvent(event)
            }

            if (event.action == KeyEvent.ACTION_UP) {
                if (pendingVolumeDownRunnable != null) {
                    handler.removeCallbacks(pendingVolumeDownRunnable!!)
                    pendingVolumeDownRunnable = null
                    restoreVolume()
                    invertRotation()
                    return true
                } else {
                    hasVolumeDecreased = true
                    pendingVolumeDownRunnable = Runnable {
                        hasVolumeDecreased = false
                        pendingVolumeDownRunnable = null
                    }
                    handler.postDelayed(pendingVolumeDownRunnable!!, Constants.DEFAULT_CLICK_DELAY_MS.toLong())
                }
            }
        } catch (e: Exception) {
            toastHelper.show("onKeyEvent Error: ${e.message}", LogLevel.Error)
        }

        return super.onKeyEvent(event)
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

    private fun invertRotation() {
        val newValue = !loadUserPrefRotation()
        if (!setRotationEnabled(newValue)) {
            val intent = Intent(this, EngineActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.startActivity(intent)
            if (!newValue)
                setNewUserPrefRotation(true)
        }
        toastHelper.show(
            if (newValue)
                "Rotation enabled"
            else
                "Rotation disabled",
            LogLevel.DEBUG)

    }

    private fun restoreVolume() {
        if (hasVolumeDecreased) {
            try {
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    currentVolume+2,
                    0
                )
            } catch (e: Exception) {
                toastHelper.show("Failed to restore volume: ${e.message}", LogLevel.Error)
            } finally {
                hasVolumeDecreased = false
            }
        }
    }
}