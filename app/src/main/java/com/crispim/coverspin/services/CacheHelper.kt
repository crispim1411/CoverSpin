package com.crispim.coverspin.services

import android.content.SharedPreferences
import androidx.core.content.edit
import com.crispim.coverspin.Constants

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

    fun isDebugMessagesEnabled(): Boolean {
        return sharedPrefs.getBoolean(Constants.PREF_KEY_DEBUG_MESSAGES_ENABLED, true)
    }

    fun isKeepScreenOn(): Boolean {
        return sharedPrefs.getBoolean(Constants.PREF_KEY_KEEP_SCREEN_ON, false)
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

    fun setDebugMessagesEnabled(isEnabled: Boolean) {
        sharedPrefs.edit { putBoolean(Constants.PREF_KEY_DEBUG_MESSAGES_ENABLED, isEnabled) }
    }

    fun setKeepScreenOn(isEnabled: Boolean) {
        sharedPrefs.edit { putBoolean(Constants.PREF_KEY_KEEP_SCREEN_ON, isEnabled) }
    }
}