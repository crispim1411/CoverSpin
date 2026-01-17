package com.crispim.coverspin

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class ToastHelper(private val context: Context) {
    private val toastHandler = Handler(Looper.getMainLooper())

    fun show(msg: String) {
        toastHandler.post {
            try {
                val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
                val targetDisplay = displayManager.getDisplay(1)?.takeIf { it.state != Display.STATE_OFF }
                    ?: displayManager.getDisplay(0)
                    ?: return@post

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

                val displayHeight = displayContext.resources.displayMetrics.heightPixels
                params.y = displayHeight/4

                val textView = TextView(displayContext).apply {
                    text = msg
                    setTextColor(Color.WHITE)
                    textSize = 14f
                    setPadding(40, 20, 40, 20)
                    background = GradientDrawable().apply {
                        setColor(0xCC000000.toInt())
                        cornerRadius = 50f
                    }
                }

                wm.addView(textView, params)

                toastHandler.postDelayed({
                    try { wm.removeView(textView) } catch (_: Exception) {}
                }, 5000)
            } catch (e: Exception) {
                Log.e("showToast", "Failed to show toast: ${e.message}")
            }
        }
    }
}