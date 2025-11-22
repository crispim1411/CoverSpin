package com.example.navigationsample

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class EngineActivity : Activity() {

    private var overlayView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ID 0 geralmente é a tela interna principal.
        if (display != null && display.displayId == 0) {
            finish()
            return
        }

        addRotationOverlay()
        moveTaskToBack(true)
    }

    private fun addRotationOverlay() {
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Criação da View Invisível
        overlayView = View(this)

        val params = WindowManager.LayoutParams(
            0, 0,

            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,

            // FLAGS:
            // NOT_FOCUSABLE: O toque passa para o app de baixo.
            // NOT_TOUCH_MODAL: Garante que cliques fora não sejam bloqueados.
            // WATCH_OUTSIDE_TOUCH: Monitora eventos.
            // SHOW_WHEN_LOCKED: Aparece na capa.
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,

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

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null) {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
