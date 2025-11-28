package com.crispim.coverspin

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class KeepAliveService : AccessibilityService() {

    private var pendingVolumeRunnable: Runnable? = null
    private val clickDelay = 400L
    private val handler = Handler(Looper.getMainLooper())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (action == KeyEvent.ACTION_DOWN) {
                if (pendingVolumeRunnable != null) {
                    handler.removeCallbacks(pendingVolumeRunnable!!)
                    pendingVolumeRunnable = null
                    openRecentApps()
                    return true
                }
                else {
                    pendingVolumeRunnable = Runnable {
                        adjustVolume()
                        pendingVolumeRunnable = null
                    }
                    handler.postDelayed(pendingVolumeRunnable!!, clickDelay)
                    return true
                }
            }
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && action == KeyEvent.ACTION_DOWN) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            return true
        }

        return super.onKeyEvent(event)
    }

    private fun openRecentApps() {
        try {
            val samsungIntent = Intent()
            samsungIntent.component = android.content.ComponentName(
                "com.sec.android.app.launcher",
                "com.android.quickstep.RecentsActivity"
            )
            samsungIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            samsungIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

            val options = android.app.ActivityOptions.makeBasic()
            val displayManager = getSystemService(Context.DISPLAY_SERVICE) as android.hardware.display.DisplayManager
            val targetDisplay = displayManager.getDisplay(1) ?: displayManager.getDisplay(0)

            if (targetDisplay != null) {
                options.launchDisplayId = targetDisplay.displayId
                startActivity(samsungIntent, options.toBundle())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun adjustVolume() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI // Mostra a barra de volume na tela
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
