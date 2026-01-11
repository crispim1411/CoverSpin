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
import java.lang.ref.WeakReference

class EngineActivity : Activity() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var overlayViewRef: WeakReference<View>? = null

        val isOverlayActive: Boolean
            get() = overlayViewRef?.get() != null

        fun initialize(context: Context) {
            val startIntent = Intent(context, EngineActivity::class.java)
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            context.startActivity(startIntent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isOverlayActive)
            addRotationOverlay()
        finish()
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
    }

    private fun addRotationOverlay() {
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val overlayView = View(applicationContext)

        val params = WindowManager.LayoutParams(
            0, 0,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        windowManager.addView(overlayView, params)
        overlayViewRef = WeakReference(overlayView)
    }
}
