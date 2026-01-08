package com.crispim.coverspin.services

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.core.content.edit
import com.crispim.coverspin.Constants
import com.crispim.coverspin.models.AnimationType
import com.crispim.coverspin.models.GestureAction
import com.crispim.coverspin.models.GestureType
import com.crispim.coverspin.models.LogLevel
import com.crispim.coverspin.models.RotationMode

class CacheHelper(private val sharedPrefs: SharedPreferences) {

    // --- Readers ---

    fun isRotationEnabled(): Boolean {
        return sharedPrefs.getBoolean(Constants.PREF_KEY_ROTATION_ENABLED, true)
    }

    fun isGestureButtonEnabled(): Boolean {
        return sharedPrefs.getBoolean(Constants.PREF_KEY_GESTURE_BUTTON_ENABLED, true)
    }

    fun isKeepScreenOn(): Boolean {
        return sharedPrefs.getBoolean(Constants.PREF_KEY_KEEP_SCREEN_ON, false)
    }

    fun getLogLevel(): LogLevel {
        val levelInt = sharedPrefs.getInt(Constants.PREF_KEY_LOG_LEVEL, LogLevel.Debug.value)
        return LogLevel.fromInt(levelInt)
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun hasAccessibilityPermission(context: Context): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = android.text.TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        val componentName = android.content.ComponentName(context, EventsService::class.java)
        val flatName = componentName.flattenToString()

        while (colonSplitter.hasNext()) {
            val component = colonSplitter.next()
            if (component.equals(flatName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    // --- Writers ---

    fun setRotationEnabled(isEnabled: Boolean) {
        sharedPrefs.edit { putBoolean(Constants.PREF_KEY_ROTATION_ENABLED, isEnabled) }
    }

    fun setGestureButtonEnabled(isEnabled: Boolean) {
        sharedPrefs.edit { putBoolean(Constants.PREF_KEY_GESTURE_BUTTON_ENABLED, isEnabled) }
    }

    fun setKeepScreenOn(isEnabled: Boolean) {
        sharedPrefs.edit { putBoolean(Constants.PREF_KEY_KEEP_SCREEN_ON, isEnabled) }
    }

    fun setLogLevel(logLevel: LogLevel) {
        sharedPrefs.edit { putInt(Constants.PREF_KEY_LOG_LEVEL, logLevel.value) }
    }

    // --- New Feature Methods ---

    fun getRotationMode(): Int {
        return sharedPrefs.getInt(Constants.PREF_KEY_ROTATION_MODE, RotationMode.AUTO.value)
    }

    fun setRotationMode(mode: Int) {
        sharedPrefs.edit { putInt(Constants.PREF_KEY_ROTATION_MODE, mode) }
    }

    fun getAnimationType(): AnimationType {
        val typeInt = sharedPrefs.getInt(Constants.PREF_KEY_ANIMATION_TYPE, AnimationType.FADE.value)
        return AnimationType.fromInt(typeInt)
    }

    fun setAnimationType(type: AnimationType) {
        sharedPrefs.edit { putInt(Constants.PREF_KEY_ANIMATION_TYPE, type.value) }
    }

    fun getAnimationDuration(): Int {
        return sharedPrefs.getInt(
            Constants.PREF_KEY_ANIMATION_DURATION,
            Constants.DEFAULT_ANIMATION_DURATION_MS
        )
    }

    fun setAnimationDuration(durationMs: Int) {
        sharedPrefs.edit { putInt(Constants.PREF_KEY_ANIMATION_DURATION, durationMs) }
    }

    fun isGesturesEnabled(): Boolean {
        return sharedPrefs.getBoolean(Constants.PREF_KEY_GESTURES_ENABLED, false)
    }

    fun setGesturesEnabled(enabled: Boolean) {
        sharedPrefs.edit { putBoolean(Constants.PREF_KEY_GESTURES_ENABLED, enabled) }
    }

    fun getGestureAction(gestureType: GestureType): GestureAction {
        val actionInt = sharedPrefs.getInt(
            "GESTURE_ACTION_${gestureType.name}",
            getDefaultGestureAction(gestureType).value
        )
        return GestureAction.fromInt(actionInt)
    }

    fun setGestureAction(gestureType: GestureType, action: GestureAction) {
        sharedPrefs.edit { putInt("GESTURE_ACTION_${gestureType.name}", action.value) }
    }

    private fun getDefaultGestureAction(gestureType: GestureType): GestureAction {
        return when (gestureType) {
            GestureType.SWIPE_UP -> GestureAction.PORTRAIT
            GestureType.SWIPE_DOWN -> GestureAction.LANDSCAPE
            GestureType.SWIPE_LEFT, GestureType.SWIPE_RIGHT -> GestureAction.TOGGLE
            GestureType.DOUBLE_TAP -> GestureAction.QUICK_TOGGLE
            GestureType.LONG_PRESS -> GestureAction.OPEN_SETTINGS
        }
    }

    fun isHapticFeedbackEnabled(): Boolean {
        return sharedPrefs.getBoolean(Constants.PREF_KEY_HAPTIC_FEEDBACK, true)
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        sharedPrefs.edit { putBoolean(Constants.PREF_KEY_HAPTIC_FEEDBACK, enabled) }
    }

    fun isAutoStartOnBoot(): Boolean {
        return sharedPrefs.getBoolean(Constants.PREF_KEY_AUTO_START_ON_BOOT, true)
    }

    fun setAutoStartOnBoot(enabled: Boolean) {
        sharedPrefs.edit { putBoolean(Constants.PREF_KEY_AUTO_START_ON_BOOT, enabled) }
    }

    fun isBatteryOptimizationDisabled(): Boolean {
        return sharedPrefs.getBoolean(Constants.PREF_KEY_BATTERY_OPTIMIZATION_DISABLED, false)
    }

    fun setBatteryOptimizationDisabled(disabled: Boolean) {
        sharedPrefs.edit { putBoolean(Constants.PREF_KEY_BATTERY_OPTIMIZATION_DISABLED, disabled) }
    }
}
