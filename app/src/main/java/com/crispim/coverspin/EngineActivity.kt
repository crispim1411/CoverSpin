package com.crispim.coverspin

//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
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
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import java.lang.ref.WeakReference
import androidx.core.content.edit

class EngineActivity : Activity() {
    companion object {
        private var overlayViewRef: WeakReference<View>? = null
        private var gestureOverlayViewRef: WeakReference<View>? = null
        private val hideButtonHandler = Handler(Looper.getMainLooper())
        private var hideButtonRunnable: Runnable? = null
        private var showButtonRunnable: Runnable? = null
        private var lockRotationRunnable: Runnable? = null
        private lateinit var orientationEventListener: OrientationEventListener
        private var rotationEnabled: Boolean = true
        val isOverlayActive: Boolean
            get() = overlayViewRef?.get() != null
        var isRotationWorking: Boolean = false
        private var rotationMode: String = "AUTO"
        private var buttonPosition: String = "CENTER_RIGHT"

        var trackLogsEnabled: Boolean = false
            private set

        fun initialize(context: Context) {
            try {
                if (trackLogsEnabled) {
                    ToastHelper(context).show("Initializing")
                }

                val prefs = context.getSharedPreferences("settings", MODE_PRIVATE)
                rotationMode = prefs.getString("rotation_mode", "AUTO") ?: "AUTO"
                buttonPosition = prefs.getString("button_position", "CENTER_RIGHT") ?: "CENTER_RIGHT"
                trackLogsEnabled = prefs.getBoolean("track_logs", false)

                val startIntent = Intent(context, EngineActivity::class.java)
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                context.startActivity(startIntent)
            } catch (e: Exception) {
                ToastHelper(context).show("Failed to initialize: ${e.message}")
            }
        }

        fun updateMode(context: Context, mode: String) {
            rotationMode = mode
            val prefs = context.getSharedPreferences("settings", MODE_PRIVATE)
            prefs.edit { putString("rotation_mode", mode) }

            if (overlayViewRef == null)
                initialize(context)
        }

        fun updatePosition(context: Context, position: String) {
            buttonPosition = position
            val prefs = context.getSharedPreferences("settings", MODE_PRIVATE)
            prefs.edit { putString("button_position", position) }
        }

        fun routineSetRotation(context: Context, enabled: Boolean, keepListener:Boolean=false) {
            rotationEnabled = enabled

            if (overlayViewRef == null)
                initialize(context)

            overlayViewRef?.get()?.let { view ->
                try {
                    val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
                    val params = view.layoutParams as WindowManager.LayoutParams

                    params.screenOrientation = if (enabled) {
                        ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_LOCKED
                    }

                    windowManager.updateViewLayout(view, params)

                    if (enabled || keepListener) orientationEventListener.enable()
                    else orientationEventListener.disable()

                } catch (e: Exception) {
                    ToastHelper(context).show("Failed to update rotation mode: ${e.message}")
                }
            }
        }

        fun updateTrackLogs(context: Context, enabled: Boolean) {
            trackLogsEnabled = enabled
            val prefs = context.getSharedPreferences("settings", MODE_PRIVATE)
            prefs.edit { putBoolean("track_logs", enabled) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (display != null && display.displayId == 0) {
            finish()
            return
        }

        orientationEventListener = createOrientationListener(applicationContext)
        if (!isOverlayActive) {
            if (trackLogsEnabled)
                ToastHelper(this).show("Adding rotation overlay")
            addRotationOverlay()
        }

        finish()
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        hideButtonRunnable?.let { hideButtonHandler.removeCallbacks(it) }
        showButtonRunnable?.let { hideButtonHandler.removeCallbacks(it) }
        lockRotationRunnable?.let { hideButtonHandler.removeCallbacks(it) }
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

        if (rotationMode == "AUTO") {
            params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            rotationEnabled = true
        } else {
            params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
            rotationEnabled = false
        }

        try {
            windowManager.addView(overlayView, params)
            overlayViewRef = WeakReference(overlayView)
            orientationEventListener.enable()
        } catch (e: Exception) {
            ToastHelper(this).show("Failed to add rotation overlay: ${e.message}")
        }
    }

    // region gesture button
    @SuppressLint("ClickableViewAccessibility")
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
            shape.setColor(Color.argb(100, 0, 0, 0))
            val strokeWidth = (1 * density).toInt()
            shape.setStroke(strokeWidth, Color.DKGRAY)
            background = shape

            setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    if (rotationMode == "AUTO") {
                        rotationEnabled = !rotationEnabled
                        setRotation(getRotationAuto())
                    } else {
                        setRotation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR)
                        lockRotationRunnable?.let { hideButtonHandler.removeCallbacks(it) }
                        lockRotationRunnable = Runnable {
                            setRotation(ActivityInfo.SCREEN_ORIENTATION_LOCKED)
                        }
                        hideButtonHandler.postDelayed({ removeGestureOverlay() }, 300)
                        hideButtonHandler.postDelayed(lockRotationRunnable!!, 1000)
                    }
                    vibrate()
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
        val margin = (16 * density).toInt()

        when (buttonPosition) {
            "CENTER_LEFT" -> {
                params.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                params.x = margin
                params.y = 0
            }
            "BOTTOM_RIGHT" -> {
                params.gravity = Gravity.BOTTOM or Gravity.END
                params.x = margin
                params.y = margin
            }
            "BOTTOM_LEFT" -> {
                params.gravity = Gravity.BOTTOM or Gravity.START
                params.x = margin
                params.y = margin
            }
            else -> {
                params.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                params.x = margin
                params.y = 0
            }
        }

        try {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.addView(gestureButton, params)
            gestureOverlayViewRef = WeakReference(gestureButton)
            updateGestureButtonIcon(enabled)
        } catch (e: Exception) {
            ToastHelper(this).show("Failed to add gesture button: ${e.message}")
        }
    }

    private fun vibrate() {
        getSystemService(Vibrator::class.java)?.let {
            if (it.hasVibrator()) {
                it.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }

    private fun showGestureButton(context: Context) {
        showButtonRunnable?.let { hideButtonHandler.removeCallbacks(it) }
        hideButtonRunnable?.let { hideButtonHandler.removeCallbacks(it) }

        showButtonRunnable = Runnable {
            if (trackLogsEnabled)
                ToastHelper(this@EngineActivity).show("Showing gesture button")

            addGestureOverlay(context, rotationEnabled)
            hideButtonRunnable = Runnable { removeGestureOverlay() }
            hideButtonHandler.postDelayed(hideButtonRunnable!!, 3000)
        }
        
        hideButtonHandler.postDelayed(showButtonRunnable!!, 500)
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
            val iconRes = if (isEnabled || rotationMode == "MANUAL")
                R.drawable.ic_popup_sync else R.drawable.ic_lock_idle_lock
            setImageResource(iconRes)
            setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun getRotationAuto() : Int {
        return if (rotationEnabled)
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        else
            ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }

    private fun setRotation(newOrientation: Int) {
        if (overlayViewRef == null)
            initialize(applicationContext)
        else {
            try {
                if (trackLogsEnabled)
                    ToastHelper(this).show("Setting rotation to $newOrientation")
                val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

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
                updateGestureButtonIcon(rotationEnabled)
            } catch (e: Exception) {
                ToastHelper(this).show("Failed to set rotation: ${e.message}")
            }
        }
    }

    private fun createOrientationListener(context: Context): OrientationEventListener {
        return object : OrientationEventListener(context) {
            private val THRESHOLD = 20
            private val GESTURE_THRESHOLD = 25
            private var lastProcessedTime = 0L
            private var lastOrientation = -1
            private var lastQuadrant: Int = -1
            private var lastQuadrantUpdateTime = 0L

            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastProcessedTime < 100 || (display != null && display?.displayId == 0))
                    return
                lastProcessedTime = currentTime

                var isClockwise = false
                if (lastOrientation != -1) {
                    val delta = orientation - lastOrientation
                    val normalizedDelta = when {
                        delta > 180 -> delta - 360
                        delta < -180 -> delta + 360
                        else -> delta
                    }
                    if (normalizedDelta != 0) isClockwise = normalizedDelta > 0
                }
                lastOrientation = orientation

                val currentQuadrant = when {
                    (orientation >= 360 - THRESHOLD || orientation <= THRESHOLD) -> 0
                    (orientation >= 90 - THRESHOLD && orientation <= 90 + THRESHOLD) -> 1
                    (orientation >= 180 - THRESHOLD && orientation <= 180 + THRESHOLD) -> 2
                    (orientation >= 270 - THRESHOLD && orientation <= 270 + THRESHOLD) -> 3
                    else -> -1
                }

                if (currentQuadrant != -1 && lastQuadrant != -1 && currentQuadrant != lastQuadrant && isOverlayActive) {
                    val targetAngle = currentQuadrant * 90
                    val isCorrectStep = if (isClockwise) (lastQuadrant + 1) % 4 == currentQuadrant
                    else (lastQuadrant + 3) % 4 == currentQuadrant

                    val isInGestureRange = when (currentQuadrant) {
                        0 -> orientation >= 360 - GESTURE_THRESHOLD || orientation <= GESTURE_THRESHOLD
                        else -> if (isClockwise) orientation in (targetAngle - GESTURE_THRESHOLD)..targetAngle
                        else orientation in targetAngle..(targetAngle + GESTURE_THRESHOLD)
                    }

                    if (isCorrectStep && isInGestureRange) {
                        showGestureButton(context)
                    }
                }

                if (currentQuadrant != -1 && currentQuadrant != lastQuadrant && (currentTime - lastQuadrantUpdateTime > 3000)) {
                    lastQuadrant = currentQuadrant
                    lastQuadrantUpdateTime = currentTime
                }
                
                isRotationWorking = true
            }
        }
    }
    // endregion
}
