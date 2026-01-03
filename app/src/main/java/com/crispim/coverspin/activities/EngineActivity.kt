package com.crispim.coverspin.activities

//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import com.crispim.coverspin.Constants
import com.crispim.coverspin.services.CacheHelper
import com.crispim.coverspin.services.EventsService
import java.lang.ref.WeakReference

class EngineActivity : Activity() {

    companion object {

        // variables
        private var overlayViewRef: WeakReference<View>? = null
        private var gestureOverlayViewRef: WeakReference<View>? = null
        private var keepScreenOn: Boolean = false

        // Timer for temporary gesture button
        private val hideButtonHandler = Handler(Looper.getMainLooper())
        private var hideButtonRunnable: Runnable? = null

        // Getters
        private val overlayView: View?
            get() = overlayViewRef?.get()
        private val gestureOverlayView: View?
            get() = gestureOverlayViewRef?.get()
        val isOverlayActive: Boolean
            get() = overlayView != null

        // Services
        private lateinit var cacheHelper: CacheHelper
        private lateinit var orientationEventListener: OrientationEventListener
        private lateinit var windowManagerSvc: WindowManager

        private fun showGestureButton(context: Context) {
            hideButtonRunnable?.let { hideButtonHandler.removeCallbacks(it) }
            addGestureOverlay(context, loadUserPrefRotation())
            hideButtonRunnable = Runnable { removeGestureOverlay() }
            hideButtonHandler.postDelayed(hideButtonRunnable!!, Constants.SHOWING_GESTURE_BUTTON_MS.toLong())
        }


        fun setNewUserPrefRotation(enabled: Boolean) {
            cacheHelper.setRotationEnabled(enabled)
        }

        fun loadUserPrefRotation(): Boolean {
            return cacheHelper.isRotationEnabled()
        }

        private fun invertRotation() {
            val newValue = !loadUserPrefRotation()
            if (!setRotationEnabled(newValue)) {
                val context = overlayView?.context ?: return;
                val intent = Intent(context, EngineActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                if (!newValue)
                    setNewUserPrefRotation(true)
            }
        }

        fun setRotationEnabled(enabled: Boolean): Boolean {
            try {
                val newOrientation = if (enabled) {
                    ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_LOCKED
                }

                overlayView?.let { view ->
                    val params = view.layoutParams as WindowManager.LayoutParams
                    if (params.screenOrientation != newOrientation) {
                        params.screenOrientation = newOrientation
                        windowManagerSvc.updateViewLayout(view, params)
                    }
                } ?: return false

                gestureOverlayView?.let { view ->
                    val params = view.layoutParams as WindowManager.LayoutParams
                    if (params.screenOrientation != newOrientation) {
                        params.screenOrientation = newOrientation
                        windowManagerSvc.updateViewLayout(view, params)
                    }
                }
                setNewUserPrefRotation(enabled)
                updateGestureButtonIcon(enabled)
                return true
            } catch (_: Exception) {
                return false
            }
        }

        fun setGestureButtonEnabled(context: Context, isEnabled: Boolean) {
            if (isEnabled) {
                showGestureButton(context)
                showGestureButtonHighlight(context)
            } else {
                removeGestureOverlay()
            }
        }

        private fun removeGestureOverlay() {
            gestureOverlayView?.let {
                windowManagerSvc.removeView(it)
            }
            gestureOverlayViewRef = null
        }

        private fun updateGestureButtonIcon(isEnabled: Boolean) {
            (gestureOverlayView as? ImageView)?.let { imageView ->
                val iconRes = if (isEnabled) R.drawable.ic_menu_rotate else R.drawable.ic_lock_lock
                imageView.setImageResource(iconRes)
                imageView.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            }
        }

        private fun addRotationOverlay(context: Context) {
            val newView = View(context.applicationContext)
            val params = WindowManager.LayoutParams(
                0, 0,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                        if (keepScreenOn) WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON else 0,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.TOP or Gravity.START
            params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            setNewUserPrefRotation(true)

            windowManagerSvc.addView(newView, params)
            overlayViewRef = WeakReference(newView)
            orientationEventListener.enable()
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun addGestureOverlay(context: Context, enabled: Boolean) {
            if (gestureOverlayView != null) return
            val sizeInDp = 32
            val sizeInPx = (sizeInDp * context.resources.displayMetrics.density).toInt()
            val padding = (sizeInDp / 4 * context.resources.displayMetrics.density).toInt()

            val gestureButton = ImageView(context.applicationContext).apply {
                layoutParams = FrameLayout.LayoutParams(sizeInPx, sizeInPx)
                setPadding(padding, padding, padding, padding)

                val shape = GradientDrawable()
                shape.shape = GradientDrawable.OVAL
                shape.setColor(Color.argb(100, 0, 0, 0)) // Translucent black
                shape.setStroke(1, Color.GRAY)
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
            params.x = (16 * context.resources.displayMetrics.density).toInt()
            params.y = 0

            windowManagerSvc.addView(gestureButton, params)
            gestureOverlayViewRef = WeakReference(gestureButton)
            updateGestureButtonIcon(loadUserPrefRotation())
        }

        private fun showGestureButtonHighlight(context: Context) {
            val highlightView = CutoutHighlightView(context.applicationContext)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            }

            windowManagerSvc.addView(highlightView, params)

            Handler(Looper.getMainLooper()).postDelayed({
                windowManagerSvc.removeView(highlightView)
            }, 2000)
        }

        fun destroy(context: Context) {
            removeGestureOverlay()
            overlayView?.let {
                windowManagerSvc.removeView(it)
            }
            overlayViewRef = null
            orientationEventListener.disable()
        }
    }

    private class CutoutHighlightView(context: Context) : View(context) {

        private val backgroundPaint = Paint().apply {
            color = Color.argb(180, 0, 0, 0)
        }
        private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }

        init {
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

            val buttonSizeDp = 32
            val density = context.resources.displayMetrics.density
            val buttonSizePx = (buttonSizeDp * density).toInt()
            val buttonRadius = buttonSizePx / 2f + (10 * density) // Larger cutout padding
            val marginPx = (16 * density).toInt()

            val cx = width - marginPx - (buttonSizePx / 2f)
            val cy = height / 2f

            canvas.drawCircle(cx, cy, buttonRadius, clearPaint)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        cacheHelper = CacheHelper(getSharedPreferences("CoverSpin", MODE_PRIVATE))
        val displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager
        windowManagerSvc = getSystemService(WINDOW_SERVICE) as WindowManager

        orientationEventListener = object : OrientationEventListener(this) {
            private var lastQuadrant = -1

            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                val currentQuadrant = when (orientation) {
                    in 45..134 -> 1  // Landscape
                    in 135..224 -> 2 // Reverse Portrait
                    in 225..314 -> 3  // Reverse Landscape
                    else -> 0        // Portrait
                }

                if (currentQuadrant != lastQuadrant) {
                    lastQuadrant = currentQuadrant
                    
                    val context = overlayView?.context ?: return
                    if (!cacheHelper.isGestureButtonEnabled()) return
                    showGestureButton(context)
                }
            }
        }

        val mainDisplay = displayManager.getDisplay(0)
        if (mainDisplay?.state == Display.STATE_ON) {
            finish()
            return
        }

        keepScreenOn = cacheHelper.isKeepScreenOn()

        if (!isOverlayActive) {
            addRotationOverlay(this)
        }
        
        startEventsService()
        finish()
    }

    private fun startEventsService() {
        val serviceIntent = Intent(this, EventsService::class.java)
        startForegroundService(serviceIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        hideButtonRunnable?.let { hideButtonHandler.removeCallbacks(it) }
    }
}
