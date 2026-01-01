package com.crispim.coverspin.services

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class ToastHelper(private val context: Context) {
    private val toastHandler = Handler(Looper.getMainLooper())

    fun show(msg: String) {
        toastHandler.post {
            try {
                val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
                val targetDisplay = displayManager.getDisplay(1) ?: displayManager.getDisplay(0) ?: return@post

                val displayContext = context.createDisplayContext(targetDisplay)
                val wm = displayContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT
                )
                params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                params.y = 100

                val textView = TextView(displayContext)
                textView.text = msg
                textView.setTextColor(Color.WHITE)
                textView.textSize = 14f
                textView.setPadding(40, 20, 40, 20)

                val background = GradientDrawable()
                background.setColor(0xCC000000.toInt())
                background.cornerRadius = 50f
                textView.background = background

                wm.addView(textView, params)

                toastHandler.postDelayed({
                    try { wm.removeView(textView) } catch (e: Exception) {}
                }, 2000)
            } catch (e: Exception) {
                Log.e("showToast", "Failed to show toast: ${e.message}")
            }
        }
    }
}