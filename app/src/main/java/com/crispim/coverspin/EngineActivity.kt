package com.crispim.coverspin

import android.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import java.lang.ref.WeakReference

class EngineActivity : Activity() {
    companion object {
        private var overlayViewRef: WeakReference<View>? = null
        private var gestureOverlayViewRef: WeakReference<View>? = null

        // Timer for temporary gesture button
        private val hideButtonHandler = Handler(Looper.getMainLooper())
        private var hideButtonRunnable: Runnable? = null
        private lateinit var orientationEventListener: OrientationEventListener

        private var rotationEnabled: Boolean = false
        val isOverlayActive: Boolean
            get() = overlayViewRef?.get() != null
        var isRotationWorking: Boolean = false

        fun initialize(context: Context) {
            val startIntent = Intent(context, EngineActivity::class.java)
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            context.startActivity(startIntent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (display != null && display.displayId == 0) {
            finish()
            return
        }

        orientationEventListener = createOrientationListener(applicationContext)
        if (!isOverlayActive)
            addRotationOverlay()

        finish()
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        hideButtonRunnable?.let { hideButtonHandler.removeCallbacks(it) }
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
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        rotationEnabled = true

        windowManager.addView(overlayView, params)
        overlayViewRef = WeakReference(overlayView)
        orientationEventListener.enable()
    }

    // region gesture button
    private fun addGestureOverlay(context: Context, enabled: Boolean) {
        if (gestureOverlayViewRef != null) return
        val sizeInDp = 32
        val density = context.resources.displayMetrics.density
        val sizeInPx = (sizeInDp * density).toInt()
        val padding = (sizeInDp / 4 * density).toInt()

        val gestureButton = ImageView(context.applicationContext).apply {
            layoutParams = FrameLayout.LayoutParams(sizeInPx, sizeInPx)
            setPadding(padding, padding, padding, padding)

            val shape = GradientDrawable()
            shape.shape = GradientDrawable.OVAL
            shape.setColor(Color.argb(100, 0, 0, 0)) // Translucent black
            val strokeWidth = (1 * density).toInt()
            shape.setStroke(strokeWidth, Color.DKGRAY)
            background = shape

            setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    invertRotation()
                    view.performClick()
                }
                true
            }
        }

        val params = WindowManager.LayoutParams(
            sizeInPx,
            sizeInPx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.screenOrientation =
            if (enabled) {
                ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LOCKED
            }
        params.gravity = Gravity.END or Gravity.CENTER_VERTICAL
        params.x = (16 * density).toInt()
        params.y = 0

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(gestureButton, params)
        gestureOverlayViewRef = WeakReference(gestureButton)
        updateGestureButtonIcon(enabled)
    }

    private fun showGestureButton(context: Context) {
        hideButtonRunnable?.let { hideButtonHandler.removeCallbacks(it) }
        addGestureOverlay(
            context,
            rotationEnabled
        )
        hideButtonRunnable = Runnable { removeGestureOverlay() }
        hideButtonHandler.postDelayed(Companion.hideButtonRunnable!!, 2000)
    }

    private fun removeGestureOverlay() {
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        gestureOverlayViewRef?.let {
            windowManager.removeView(it.get())
        }
        gestureOverlayViewRef = null
    }

    private fun updateGestureButtonIcon(isEnabled: Boolean) {
        (gestureOverlayViewRef?.get() as? ImageView)?.apply {
            val iconRes = if (isEnabled) R.drawable.ic_popup_sync else R.drawable.ic_lock_idle_lock
            setImageResource(iconRes)
            setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun invertRotation() {
        if (overlayViewRef == null)
            initialize(applicationContext)
        else {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            rotationEnabled = !rotationEnabled
            val newOrientation = if (rotationEnabled) {
                ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LOCKED
            }
            overlayViewRef?.get()?.let { view ->
                val params = view.layoutParams as WindowManager.LayoutParams
                if (params.screenOrientation != newOrientation) {
                    params.screenOrientation = newOrientation
                    windowManager.updateViewLayout(view, params)
                }
            }
            gestureOverlayViewRef?.get()?.let { view ->
                val params = view.layoutParams as WindowManager.LayoutParams
                if (params.screenOrientation != newOrientation) {
                    params.screenOrientation = newOrientation
                    windowManager.updateViewLayout(view, params)
                }
            }
        }
    }

    private fun createOrientationListener(context: Context): OrientationEventListener {
        return object : OrientationEventListener(context) {
            private var lastQuadrant = -1

            override fun onOrientationChanged(orientation: Int) {
                if (display != null && display?.displayId == 0 || orientation == ORIENTATION_UNKNOWN)
                    return

                val currentQuadrant = when (orientation) {
                    in 45..134 -> 1  // Landscape
                    in 135..224 -> 2 // Reverse Portrait
                    in 225..314 -> 3 // Reverse Landscape
                    else -> 0        // Portrait
                }

                if (currentQuadrant != lastQuadrant) {
                    lastQuadrant = currentQuadrant
                    val view = overlayViewRef?.get() ?: return
                    showGestureButton(view.context)
                }
                isRotationWorking = true
            }
        }
    }
    // endregion
}
