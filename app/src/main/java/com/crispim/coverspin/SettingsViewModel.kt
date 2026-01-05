package com.crispim.coverspin

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.crispim.coverspin.services.CacheHelper
import com.crispim.coverspin.activities.EngineActivity
import com.crispim.coverspin.services.ToastHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import models.LogLevel
import models.SettingsState

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    private var toastHelper: ToastHelper
    private val cacheHelper: CacheHelper =
        CacheHelper(application.getSharedPreferences(Constants.APP_NAME, Context.MODE_PRIVATE))

    init {
        toastHelper = ToastHelper(application, cacheHelper)
        loadInitialState()
    }

    fun onResume() {
        loadInitialState()
    }

    private fun loadInitialState() {
        _uiState.update {
            it.copy(
                hasOverlayPermission = cacheHelper.hasOverlayPermission(getApplication()),
                hasAccessibilityPermission = cacheHelper.hasAccessibilityPermission(getApplication()),
                volumeShortcutsEnabled = cacheHelper.isVolumeShortcutsEnabled(),
                isGestureButtonEnabled = cacheHelper.isGestureButtonEnabled(),
                keepScreenOn = cacheHelper.isKeepScreenOn(),
                logLevel = cacheHelper.getLogLevel()
            )
        }
    }

    fun onVolumeShortcutsEnabledChange(isEnabled: Boolean) {
        cacheHelper.setVolumeShortcutsEnabled(isEnabled)
        _uiState.update { it.copy(volumeShortcutsEnabled = isEnabled) }
    }

    fun onGestureButtonEnabledChange(context: Context, isEnabled: Boolean) {
        cacheHelper.setGestureButtonEnabled(isEnabled)
        _uiState.update { it.copy(isGestureButtonEnabled = isEnabled) }
        EngineActivity.setGestureButtonEnabled(context, isEnabled)
    }

    fun onKeepScreenOnChange(isEnabled: Boolean) {
        cacheHelper.setKeepScreenOn(isEnabled)
        _uiState.update { it.copy(keepScreenOn = isEnabled) }
    }

    fun onLogLevelChange(logLevel: LogLevel) {
        cacheHelper.setLogLevel(logLevel)
        _uiState.update { it.copy(logLevel = logLevel) }
    }

    fun onStartEngine() {
        if (_uiState.value.isEngineRunning) {
            toastHelper.show("Already running", LogLevel.INFO)
        } else {
            val intent = Intent(application, EngineActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            application.startActivity(intent)
            toastHelper.show("Starting...", LogLevel.INFO)
        }
    }
}
