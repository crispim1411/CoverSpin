package com.crispim.coverspin

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.display.DisplayManager
import android.provider.Settings
import android.view.Display
import android.view.KeyEvent
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.crispim.coverspin.activities.EngineActivity
import com.crispim.coverspin.services.CacheHelper
import com.crispim.coverspin.services.EventsService
import com.crispim.coverspin.services.ToastHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isEngineRunning: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val volumeShortcutsEnabled: Boolean = false,
    val isGestureButtonEnabled: Boolean = true,
    val debugMessagesEnabled: Boolean = false,
    val isInnerScreen: Boolean = true,
    val keepScreenOn: Boolean = false,
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var toastHelper: ToastHelper? = null
    private val cacheHelper: CacheHelper =
        CacheHelper(application.getSharedPreferences("CoverSpin", Context.MODE_PRIVATE))
    private val displayManager = application.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

    init {
        loadInitialState()
        if (cacheHelper.isDebugMessagesEnabled())
            toastHelper = ToastHelper(application)
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
                    volumeShortcutsEnabled = cacheHelper.isVolumeShortcutsEnabled(),
                    isGestureButtonEnabled = cacheHelper.isGestureButtonEnabled(),
                    debugMessagesEnabled = cacheHelper.isDebugMessagesEnabled(),
                    isInnerScreen = displayManager.getDisplay(0)?.state == Display.STATE_ON,
                    keepScreenOn = cacheHelper.isKeepScreenOn()
                )
            }
        }
    }

    fun onStartEngine() {
        viewModelScope.launch {
            if (_uiState.value.isEngineRunning) {
                toastHelper?.show("Already running")
            } else {
                val intent = Intent(application, EngineActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                application.startActivity(intent)
                toastHelper?.show("Starting...")
            }
        }
    }

    fun onVolumeShortcutsEnabledChange(isEnabled: Boolean) {
        viewModelScope.launch {
            cacheHelper.setVolumeShortcutsEnabled(isEnabled)
            _uiState.update { it.copy(volumeShortcutsEnabled = isEnabled) }
        }
    }

    fun onGestureButtonEnabledChange(context: Context, isEnabled: Boolean) {
        viewModelScope.launch {
            cacheHelper.setGestureButtonEnabled(isEnabled)
            _uiState.update { it.copy(isGestureButtonEnabled = isEnabled) }
            if (_uiState.value.isEngineRunning) {
                EngineActivity.setGestureButtonEnabled(context, isEnabled)
                EngineActivity.showGestureButtonHighlight(context)
            }
        }
    }

    fun onDebugMessagesEnabledChange(isEnabled: Boolean) {
        viewModelScope.launch {
            cacheHelper.setDebugMessagesEnabled(isEnabled)
            _uiState.update { it.copy(debugMessagesEnabled = isEnabled) }
        }
    }

    fun onKeepScreenOnChange(isEnabled: Boolean) {
        viewModelScope.launch {
            cacheHelper.setKeepScreenOn(isEnabled)
            _uiState.update { it.copy(keepScreenOn = isEnabled) }
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = android.text.TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        val componentName = android.content.ComponentName(context, service)
        val flatName = componentName.flattenToString()

        while (colonSplitter.hasNext()) {
            val component = colonSplitter.next()
            if (component.equals(flatName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
