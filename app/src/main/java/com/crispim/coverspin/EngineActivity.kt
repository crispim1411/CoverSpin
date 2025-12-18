package com.crispim.coverspin

import android.annotation.SuppressLint
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

        val isOverlayActive: Boolean
            get() = overlayView != null

        fun setNewUserPrefRotation(context: Context, enable: Boolean) {
            context.getSharedPreferences("CoverSpin", Context.MODE_PRIVATE)
                .edit { putBoolean("IS_ROTATION_ENABLED", enable) }
        }

        fun loadUserPrefRotation(context: Context) : Boolean {
            return context.getSharedPreferences("CoverSpin", Context.MODE_PRIVATE)
                .getBoolean("IS_ROTATION_ENABLED", true)
        }

        fun setRotationEnabled(context: Context, enable: Boolean) : Boolean {
            val view = overlayView ?: return false
            try {
                val windowManager = view.context.getSystemService(WINDOW_SERVICE) as WindowManager
                val params = view.layoutParams as WindowManager.LayoutParams

                val newOrientation = if (enable) {
                    ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_LOCKED
                }

                // Só atualiza se mudou para economizar bateria
                if (params.screenOrientation != newOrientation) {
                    params.screenOrientation = newOrientation
                    windowManager.updateViewLayout(view, params)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false;
            }
            return true;
        }

        private fun addRotationOverlay(context: Context) {
            val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager

            // Criação da View Invisível
            val newView = View(context.applicationContext)
            overlayViewRef = WeakReference(newView)

            val params = WindowManager.LayoutParams(
                0, 0,

                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,

                // FLAGS:
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,

                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.TOP or Gravity.START

            params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

            try {
                windowManager.addView(newView, params)
            } catch (e: Exception) {
                e.printStackTrace()
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

        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as android.hardware.display.DisplayManager
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
            e.printStackTrace()
        }
    }


}
