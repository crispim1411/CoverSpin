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

enum class VolumeDirection {
    Up,
    Down
}

class RecentAppsService : AccessibilityService() {

    private var pendingVolumeUpRunnable: Runnable? = null
    private var pendingVolumeDownRunnable: Runnable? = null
    private val clickDelay = 400L
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
                        // Tela desligou/bloqueou: Pede para a Engine parar de forçar rotação
                        EngineActivity.setRotationEnabled(false)
                    }
                    Intent.ACTION_USER_PRESENT -> {
                        // Usuário desbloqueou: Volta a forçar a rotação
                        EngineActivity.setRotationEnabled(true)
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
        val action = event.action
        val keyCode = event.keyCode

        if (action == KeyEvent.ACTION_UP &&
            (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (action == KeyEvent.ACTION_DOWN) {
                if (pendingVolumeUpRunnable != null) {
                    handler.removeCallbacks(pendingVolumeUpRunnable!!)
                    pendingVolumeUpRunnable = null
                    //openRecentApps()
                    EngineActivity.setRotationEnabled(false)
                    return true
                }
                else {
                    pendingVolumeUpRunnable = Runnable {
                        adjustVolume(VolumeDirection.Up)
                        pendingVolumeUpRunnable = null
                    }
                    handler.postDelayed(pendingVolumeUpRunnable!!, clickDelay)
                    return true
                }
            }
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && action == KeyEvent.ACTION_DOWN) {
            if (pendingVolumeDownRunnable != null) {
                handler.removeCallbacks(pendingVolumeDownRunnable!!)
                pendingVolumeDownRunnable = null
                //performGlobalAction(GLOBAL_ACTION_HOME)
                EngineActivity.setRotationEnabled(true)
                return true
            }
            else {
                pendingVolumeDownRunnable = Runnable {
                    adjustVolume(VolumeDirection.Down)
                    pendingVolumeDownRunnable = null
                }
                handler.postDelayed(pendingVolumeDownRunnable!!, clickDelay)
                return true
            }
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
            samsungIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) // Tenta forçar reinício

            val options = android.app.ActivityOptions.makeBasic()
            val displayManager = getSystemService(Context.DISPLAY_SERVICE) as android.hardware.display.DisplayManager
            val targetDisplay = displayManager.getDisplay(1)

            if (targetDisplay != null) {
                options.launchDisplayId = targetDisplay.displayId
                startActivity(samsungIntent, options.toBundle())
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun adjustVolume(direction: VolumeDirection) {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val adjustment = if (direction == VolumeDirection.Up) {
                AudioManager.ADJUST_RAISE
            } else {
                AudioManager.ADJUST_LOWER
            }

            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                adjustment,
                AudioManager.FLAG_SHOW_UI // Mostra a barra de volume na tela
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
