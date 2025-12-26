package com.crispim.coverspin

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        screenStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        EngineActivity.setRotationEnabled(false)
                    }
                    Intent.ACTION_USER_PRESENT -> {
                        val shouldRotate = EngineActivity.loadUserPrefRotation(context!!)
                        if(!EngineActivity.setRotationEnabled(shouldRotate)) {
                            showToast(context, "Initializing...")
                            val intent = Intent(context, EngineActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
                }
            }
        }

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)
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
            val sharedPrefs = getSharedPreferences("CoverSpin", Context.MODE_PRIVATE)
            val shortcutsEnabled = sharedPrefs.getBoolean(Constants.PREF_KEY_VOLUME_SHORTCUTS, true)
            val displayManager = getSystemService(Context.DISPLAY_SERVICE) as android.hardware.display.DisplayManager
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
                    val newValue = !EngineActivity.loadUserPrefRotation(this)
                    if (!EngineActivity.setRotationEnabled(newValue)) {
                        showToast(this, "Starting...")
                        val intent = Intent(this, EngineActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    else {
                        EngineActivity.setNewUserPrefRotation(this, newValue)
                        showToast(this, if (newValue) "Rotation enabled" else "Rotation disabled")
                    }
                    return true
                } else {
                    hasVolumeDecreased = true
                    pendingVolumeDownRunnable = Runnable {
                        hasVolumeDecreased = false
                        pendingVolumeDownRunnable = null
                    }
                    val clickDelay = sharedPrefs.getInt(Constants.PREF_KEY_CLICK_DELAY, Constants.DEFAULT_CLICK_DELAY_MS).toLong()
                    handler.postDelayed(pendingVolumeDownRunnable!!, clickDelay)
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
