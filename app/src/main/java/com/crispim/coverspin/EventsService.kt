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

    private val SCAN_CODE_VOLUME_DOWN = 115
    private var pendingVolumeDownRunnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var screenStateReceiver: BroadcastReceiver

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()

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
            val shortcutsEnabled = sharedPrefs.getBoolean("VOLUME_SHORTCUTS_ENABLED", true)

            if (!shortcutsEnabled || event.scanCode != SCAN_CODE_VOLUME_DOWN) {
                return super.onKeyEvent(event)
            }

            val displayManager = getSystemService(Context.DISPLAY_SERVICE) as android.hardware.display.DisplayManager
            val mainDisplay = displayManager.getDisplay(0)
            if (mainDisplay?.state == android.view.Display.STATE_ON) {
                return super.onKeyEvent(event)
            }

            val action = event.action

            if (action == KeyEvent.ACTION_DOWN) {
                return true
            }

            if (action == KeyEvent.ACTION_UP) {
                val clickDelay = sharedPrefs.getInt("CLICK_DELAY_MS", 300).toLong()

                if (pendingVolumeDownRunnable != null) {
                    handler.removeCallbacks(pendingVolumeDownRunnable!!)
                    pendingVolumeDownRunnable = null

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
                    pendingVolumeDownRunnable = Runnable {
                        adjustVolume()
                        pendingVolumeDownRunnable = null
                    }
                    handler.postDelayed(pendingVolumeDownRunnable!!, clickDelay)
                    return true
                }
            }
        } catch (e: Exception) {
            showToast(this, "onKeyEvent Error: ${e.message}")
        }
        return super.onKeyEvent(event)
    }

    private fun adjustVolume() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI
            )
        } catch (e: Exception) {
            showToast(this, "adjustVolume Error: ${e.message}")
        }
    }
}
