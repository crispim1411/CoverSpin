package com.crispim.coverspin.services

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.core.content.edit
import com.crispim.coverspin.Constants
import models.LogLevel

class CacheHelper(private val sharedPrefs: SharedPreferences) {

    // --- Readers ---

    fun isRotationEnabled(): Boolean {
        return sharedPrefs.getBoolean(Constants.PREF_KEY_ROTATION_ENABLED, true)
    }

    fun isVolumeShortcutsEnabled(): Boolean {
        return sharedPrefs.getBoolean(Constants.PREF_KEY_VOLUME_SHORTCUTS, false)
    }

    fun isGestureButtonEnabled(): Boolean {
        return sharedPrefs.getBoolean(Constants.PREF_KEY_GESTURE_BUTTON_ENABLED, true)
    }

    fun isKeepScreenOn(): Boolean {
        return sharedPrefs.getBoolean(Constants.PREF_KEY_KEEP_SCREEN_ON, false)
    }

    fun getLogLevel(): LogLevel {
        val levelInt = sharedPrefs.getInt(Constants.PREF_KEY_LOG_LEVEL, LogLevel.DEBUG.value)
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

    fun setVolumeShortcutsEnabled(isEnabled: Boolean) {
        sharedPrefs.edit { putBoolean(Constants.PREF_KEY_VOLUME_SHORTCUTS, isEnabled) }
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
}
