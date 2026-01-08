package com.crispim.coverspin.services

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.GestureDetector.OnGestureListener
import com.crispim.coverspin.Constants
import com.crispim.coverspin.activities.EngineActivity
import com.crispim.coverspin.models.GestureAction
import com.crispim.coverspin.models.GestureType
import com.crispim.coverspin.models.RotationMode

class CoverSpinGestureDetector(
    context: Context,
    private val onGestureDetected: (GestureAction) -> Unit
) : GestureDetector.SimpleOnGestureListener() {

    private val cacheHelper = CacheHelper(
        context.getSharedPreferences(Constants.APP_NAME, Context.MODE_PRIVATE)
    )
    private var velocityTracker: VelocityTracker? = null
    private var lastTapTime = 0L
    private val doubleTapTimeout = 300L

    override fun onDown(e: MotionEvent): Boolean {
        velocityTracker = VelocityTracker.obtain()
        velocityTracker?.addMovement(e)
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (e1 == null) return false
        
        val deltaX = e2.x - e1.x
        val deltaY = e2.y - e1.y
        val minVelocity = 500f
        val minDistance = 100f

        // Check if gestures are enabled
        if (!cacheHelper.isGesturesEnabled()) return false

        // Determine swipe direction
        when {
            Math.abs(deltaY) > Math.abs(deltaX) && Math.abs(velocityY) > minVelocity && Math.abs(deltaY) > minDistance -> {
                // Vertical swipe
                if (deltaY > 0) {
                    // Swipe down
                    handleGesture(GestureType.SWIPE_DOWN)
                } else {
                    // Swipe up
                    handleGesture(GestureType.SWIPE_UP)
                }
                return true
            }
            Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(velocityX) > minVelocity && Math.abs(deltaX) > minDistance -> {
                // Horizontal swipe
                if (deltaX > 0) {
                    // Swipe right
                    handleGesture(GestureType.SWIPE_RIGHT)
                } else {
                    // Swipe left
                    handleGesture(GestureType.SWIPE_LEFT)
                }
                return true
            }
        }

        return false
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTapTime < doubleTapTimeout) {
            // Double tap detected
            handleGesture(GestureType.DOUBLE_TAP)
            lastTapTime = 0L
            return true
        }
        lastTapTime = currentTime
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        handleGesture(GestureType.LONG_PRESS)
    }

    private fun handleGesture(gestureType: GestureType) {
        val action = cacheHelper.getGestureAction(gestureType)
        if (action != GestureAction.NONE) {
            onGestureDetected(action)
        }
    }

    fun cleanup() {
        velocityTracker?.recycle()
        velocityTracker = null
    }
}

