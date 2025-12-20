package com.crispim.coverspin

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager

private val toastHandler = Handler(Looper.getMainLooper())

fun showToast(context: Context, msg: String) {
    toastHandler.post {
        try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as android.hardware.display.DisplayManager
            val targetDisplay = displayManager.getDisplay(1) ?: displayManager.getDisplay(0) ?: return@post

            val displayContext = context.createDisplayContext(targetDisplay)
            val wm = displayContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
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

            toastHandler.postDelayed({
                try { wm.removeView(textView) } catch (e: Exception) {}
            }, 2000)
        } catch (e: Exception) {
            // Se o próprio showToast falhar, loga no Logcat para evitar loop.
            Log.e("showToast", "Failed to show toast: ${e.message}")
        }
    }
}