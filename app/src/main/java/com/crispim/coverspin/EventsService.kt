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
    private val clickDelay = 300L
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
                        EngineActivity.setRotationEnabled(context!!,false)
                    }
                    Intent.ACTION_USER_PRESENT -> {
                        val shouldRotate = EngineActivity.loadUserPrefRotation(context!!)
                        if (shouldRotate)
                            EngineActivity.setRotationEnabled(context,true)
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
            e.printStackTrace()
        }
        super.onDestroy()
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val shortcutsEnabled = getSharedPreferences("CoverSpin", Context.MODE_PRIVATE)
            .getBoolean("VOLUME_SHORTCUTS_ENABLED", true)
        if (!shortcutsEnabled)
            return super.onKeyEvent(event)

        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as android.hardware.display.DisplayManager
        val mainDisplay = displayManager.getDisplay(0)
        if (mainDisplay?.state == android.view.Display.STATE_ON)
            return super.onKeyEvent(event)

        val action = event.action

        if (event.scanCode == SCAN_CODE_VOLUME_DOWN && action == KeyEvent.ACTION_UP) {
            return true
        }

        if (event.scanCode == SCAN_CODE_VOLUME_DOWN && action == KeyEvent.ACTION_DOWN) {
            if (pendingVolumeDownRunnable != null) {
                handler.removeCallbacks(pendingVolumeDownRunnable!!)
                pendingVolumeDownRunnable = null
                val newValue = !EngineActivity.loadUserPrefRotation(this)
                if (!EngineActivity.setRotationEnabled(this,newValue))
                    showToast("Please initialize CoverSpin")
                else {
                    EngineActivity.setNewUserPrefRotation(this, newValue)
                    showToast(if (newValue) "Rotation enabled" else "Rotation disabled")
                }
                return true
            }
            else {
                pendingVolumeDownRunnable = Runnable {
                    adjustVolume()
                    pendingVolumeDownRunnable = null
                }
                handler.postDelayed(pendingVolumeDownRunnable!!, clickDelay)
                return true
            }
        }

        return super.onKeyEvent(event)
    }

    private fun adjustVolume() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI // Mostra a barra de volume na tela
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showToast(msg: String) {
        handler.post {
            try {
                val displayManager = getSystemService(Context.DISPLAY_SERVICE) as android.hardware.display.DisplayManager
                val targetDisplay = displayManager.getDisplay(1) ?: displayManager.getDisplay(0) ?: return@post

                val displayContext = createDisplayContext(targetDisplay)
                val wm = displayContext.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager

                val params = android.view.WindowManager.LayoutParams(
                    android.view.WindowManager.LayoutParams.WRAP_CONTENT,
                    android.view.WindowManager.LayoutParams.WRAP_CONTENT,
                    android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    android.graphics.PixelFormat.TRANSLUCENT
                )
                params.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
                params.y = 100

                val textView = android.widget.TextView(displayContext)
                textView.text = msg
                textView.setTextColor(android.graphics.Color.WHITE)
                textView.textSize = 14f
                textView.setPadding(40, 20, 40, 20)

                val background = android.graphics.drawable.GradientDrawable()
                background.setColor(0xCC000000.toInt())
                background.cornerRadius = 50f
                textView.background = background

                wm.addView(textView, params)

                handler.postDelayed({
                    try { wm.removeView(textView) } catch (e: Exception) {}
                }, 2000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
