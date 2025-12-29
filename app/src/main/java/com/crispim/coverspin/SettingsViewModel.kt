package com.crispim.coverspin

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.text.TextUtils
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isEngineRunning: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val isRotationEnabled: Boolean = true,
    val volumeShortcutsEnabled: Boolean = false,
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val sharedPrefs: SharedPreferences =
        application.getSharedPreferences("CoverSpin", Context.MODE_PRIVATE)

    init {
        loadInitialState()
    }

    fun onResume() {
        loadInitialState()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isEngineRunning = EngineActivity.isOverlayActive,
                    hasOverlayPermission = Settings.canDrawOverlays(getApplication()),
                    hasAccessibilityPermission = isAccessibilityServiceEnabled(getApplication(), EventsService::class.java),
                    isRotationEnabled = EngineActivity.isRotationEnabled,
                    volumeShortcutsEnabled = sharedPrefs.getBoolean(Constants.PREF_KEY_VOLUME_SHORTCUTS, false),
                )
            }
        }
    }

    fun onRotationEnabledChange(isEnabled: Boolean) {
        viewModelScope.launch {
            sharedPrefs.edit { putBoolean(Constants.PREF_KEY_ROTATION_ENABLED, isEnabled) }
            _uiState.update { it.copy(isRotationEnabled = isEnabled) }
            if (_uiState.value.isEngineRunning) {
                EngineActivity.setRotationEnabled(isEnabled)
            }
        }
    }

    fun onVolumeShortcutsEnabledChange(isEnabled: Boolean) {
        viewModelScope.launch {
            sharedPrefs.edit { putBoolean(Constants.PREF_KEY_VOLUME_SHORTCUTS, isEnabled) }
            _uiState.update { it.copy(volumeShortcutsEnabled = isEnabled) }
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)

            val componentName = android.content.ComponentName(context, service)
            val flatName = componentName.flattenToString()

            while (colonSplitter.hasNext()) {
                val component = colonSplitter.next()
                if (component.equals(flatName, ignoreCase = true)) {
                    return true
                }
            }
        } catch (e: Exception) {
            showToast(context, "isAccessibilityServiceEnabled Error: ${e.message}")
        }
        return false
    }
}
