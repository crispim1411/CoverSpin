package com.crispim.coverspin

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class EventsService : AccessibilityService() {

    private var pendingVolumeDownRunnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var screenStateReceiver: BroadcastReceiver
    private lateinit var audioManager: AudioManager
    private var hasVolumeDecreased: Boolean = false

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var displayManager: DisplayManager

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        sharedPrefs = getSharedPreferences("CoverSpin", MODE_PRIVATE)
        displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager

        screenStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val serviceContext = this@EventsService
                if (intent.action == Intent.ACTION_USER_PRESENT || intent.action == Intent.ACTION_SCREEN_ON) {
                    val shouldRotate = EngineActivity.loadUserPrefRotation()
                    if(!EngineActivity.setRotationEnabled(shouldRotate)) {
                        showToast(serviceContext, "Initializing...")
                        val startIntent = Intent(serviceContext, EngineActivity::class.java)
                        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(startIntent)
                        if (!shouldRotate)
                            EngineActivity.setNewUserPrefRotation(true)
                    }
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
            showToast(this, "onDestroy Error: ${e.message}")
        }
        super.onDestroy()
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        try {
            val shortcutsEnabled = sharedPrefs.getBoolean(Constants.PREF_KEY_VOLUME_SHORTCUTS, true)
            val mainDisplay = displayManager.getDisplay(0)

            if (!shortcutsEnabled
                || event.scanCode != Constants.SCAN_CODE_VOLUME_DOWN
                || event.action == KeyEvent.ACTION_DOWN
                || mainDisplay?.state == android.view.Display.STATE_ON) {
                return super.onKeyEvent(event)
            }

            if (event.action == KeyEvent.ACTION_UP) {
                if (pendingVolumeDownRunnable != null) {
                    handler.removeCallbacks(pendingVolumeDownRunnable!!)
                    pendingVolumeDownRunnable = null
                    restoreVolume()
                    val newValue = !EngineActivity.loadUserPrefRotation()
                    if (!EngineActivity.setRotationEnabled(newValue)) {
                        val intent = Intent(this, EngineActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    showToast(this, if (newValue) "Rotation enabled" else "Rotation disabled")
                    EngineActivity.setNewUserPrefRotation(newValue)
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
            showToast(this, "onKeyEvent Error: ${e.message}")
        }

        return super.onKeyEvent(event)
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
                showToast(this, "Failed to restore volume: ${e.message}")
            } finally {
                hasVolumeDecreased = false
            }
        }
    }
}
