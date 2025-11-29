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

class EngineActivity : Activity() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var overlayView: View? = null

        val isOverlayActive: Boolean
            get() = overlayView != null

        fun setRotationEnabled(enable: Boolean) {
            val view = overlayView ?: return
            try {
                val windowManager = view.context.getSystemService(WINDOW_SERVICE) as WindowManager
                val params = view.layoutParams as WindowManager.LayoutParams

                val newOrientation = if (enable) {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }

                // Só atualiza se mudou para economizar bateria
                if (params.screenOrientation != newOrientation) {
                    params.screenOrientation = newOrientation
                    windowManager.updateViewLayout(view, params)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun addRotationOverlay(context: Context) {
            val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager

            // Criação da View Invisível
            overlayView = View(context.applicationContext)

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

            // O SEGREDO: Aplicar a orientação NESTA janela flutuante
            params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

            try {
                windowManager.addView(overlayView, params)
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

        // ID 0 geralmente é a tela interna principal.
        if (display != null && display.displayId == 0) {
            finish()
            return
        }

        if (!isOverlayActive) {
            addRotationOverlay(this)
        }
        startRecentAppsService()
        finish()
    }

    private fun startRecentAppsService() {
        try {
            val serviceIntent = Intent(this, RecentAppsService::class.java)
            startForegroundService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
