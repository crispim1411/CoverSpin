package com.crispim.coverspin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.content.edit
import java.lang.ref.WeakReference

class EngineActivity : Activity() {

    companion object {
        private var overlayViewRef: WeakReference<View>? = null
        private val overlayView: View?
            get() = overlayViewRef?.get()


        private var rotationEnabled: Boolean = false;

        val isRotationEnabled: Boolean
            get() = isOverlayActive && rotationEnabled

        val isOverlayActive: Boolean
            get() = overlayView != null

        fun setNewUserPrefRotation(context: Context, enable: Boolean) {
            context.getSharedPreferences("CoverSpin", MODE_PRIVATE)
                .edit { putBoolean("IS_ROTATION_ENABLED", enable) }
            rotationEnabled = enable
        }

        fun loadUserPrefRotation(context: Context) : Boolean {
            rotationEnabled = context.getSharedPreferences("CoverSpin", MODE_PRIVATE)
                .getBoolean("IS_ROTATION_ENABLED", true)
            return rotationEnabled
        }

        fun setRotationEnabled(enable: Boolean) : Boolean {
            val view = overlayView ?: return false
            try {
                val windowManager = view.context.getSystemService(WINDOW_SERVICE) as WindowManager
                val params = view.layoutParams as WindowManager.LayoutParams

                val newOrientation = if (enable) {
                    rotationEnabled = true
                    ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

                } else {
                    rotationEnabled = false
                    ActivityInfo.SCREEN_ORIENTATION_LOCKED
                }

                if (params.screenOrientation != newOrientation) {
                    params.screenOrientation = newOrientation
                    windowManager.updateViewLayout(view, params)
                }
            } catch (e: Exception) {
                showToast(view.context, "setRotationEnabled Error: ${e.message}")
                return false;
            }
            return true;
        }

        private fun addRotationOverlay(context: Context) {
            try {
                val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager

                val newView = View(context.applicationContext)
                overlayViewRef = WeakReference(newView)

                val params = WindowManager.LayoutParams(
                    0, 0,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    PixelFormat.TRANSLUCENT
                )

                params.gravity = Gravity.TOP or Gravity.START
                params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                rotationEnabled = true

                windowManager.addView(newView, params)
            } catch (e: Exception) {
                showToast(context, "addRotationOverlay Error: ${e.message}")
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (intent?.getBooleanExtra("STOP_ACTION", false) == true) {
            return
        }

        if (!isOverlayActive) {
            addRotationOverlay(this)
        }

        moveTaskToBack(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val displayManager = getSystemService(DISPLAY_SERVICE) as android.hardware.display.DisplayManager
        val mainDisplay = displayManager.getDisplay(0)

        if (mainDisplay?.state == android.view.Display.STATE_ON) {
            finish()
            return
        }

        if (!isOverlayActive)
            addRotationOverlay(this)
        
        startRecentAppsService()
        finish()
    }

    private fun startRecentAppsService() {
        try {
            val serviceIntent = Intent(this, EventsService::class.java)
            startForegroundService(serviceIntent)
        } catch (e: Exception) {
            showToast(this, "startRecentAppsService Error: ${e.message}")
        }
    }
}
