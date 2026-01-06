package com.crispim.coverspin

import android.app.Application
import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.crispim.coverspin.services.CacheHelper
import com.crispim.coverspin.activities.EngineActivity
import com.crispim.coverspin.services.ToastHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.crispim.coverspin.models.LogLevel
import com.crispim.coverspin.models.SettingsState

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    private var toastHelper: ToastHelper
    private val cacheHelper: CacheHelper =
        CacheHelper(application.getSharedPreferences(
            Constants.APP_NAME,
            Context.MODE_PRIVATE))
    private val displayManager =
        application.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

    init {
        toastHelper = ToastHelper(application, cacheHelper)
        loadInitialState()
    }

    fun onResume() {
        loadInitialState()
    }

    private fun loadInitialState() {
        try {
            _uiState.update {
                it.copy(
                    isEngineRunning = EngineActivity.isOverlayActive,
                    isInnerScreen = displayManager.getDisplay(0)?.state == Display.STATE_ON,
                    hasOverlayPermission = cacheHelper.hasOverlayPermission(getApplication()),
                    hasAccessibilityPermission = cacheHelper.hasAccessibilityPermission(
                        getApplication()
                    ),
                    isGestureButtonEnabled = cacheHelper.isGestureButtonEnabled(),
                    keepScreenOn = cacheHelper.isKeepScreenOn(),
                    logLevel = cacheHelper.getLogLevel()
                )
            }
        } catch (e: Exception) {
            toastHelper.show("loadInitialState error: ${e.message}", LogLevel.Error)
        }
    }

    fun onGestureButtonEnabledChange(context: Context, isEnabled: Boolean) {
        try {
            cacheHelper.setGestureButtonEnabled(isEnabled)
            _uiState.update { it.copy(isGestureButtonEnabled = isEnabled) }
            EngineActivity.setGestureButtonEnabled(context, isEnabled)
        } catch (e: Exception) {
            toastHelper.show("onGestureButtonEnabledChange error: ${e.message}", LogLevel.Error)
        }
    }

    fun onKeepScreenOnChange(isEnabled: Boolean) {
        try {
            cacheHelper.setKeepScreenOn(isEnabled)
            _uiState.update { it.copy(keepScreenOn = isEnabled) }
        } catch (e: Exception) {
            toastHelper.show("onKeepScreenOnChange error: ${e.message}", LogLevel.Error)
        }
    }

    fun onLogLevelChange(logLevel: LogLevel) {
        try {
            cacheHelper.setLogLevel(logLevel)
            _uiState.update { it.copy(logLevel = logLevel) }
        } catch (e: Exception) {
            toastHelper.show("onLogLevelChange error: ${e.message}", LogLevel.Error)
        }
    }

    fun onStartEngine() {
        try {
            if (_uiState.value.isEngineRunning) {
                toastHelper.show("Already running", LogLevel.Info)
            } else {
                EngineActivity.initialize(application)
                toastHelper.show("Starting...", LogLevel.Info)
            }
            _uiState.update { it.copy(isEngineRunning = EngineActivity.isOverlayActive) }
        } catch (e: Exception) {
            toastHelper.show("onStartEngine error: ${e.message}", LogLevel.Error)
        }
    }

    fun onStopEngine() {
        try {
            toastHelper.show("Stopping...", LogLevel.Debug)
            EngineActivity.removeOverlay()
            _uiState.update { it.copy(isEngineRunning = EngineActivity.isOverlayActive) }
        } catch (e: Exception) {
            toastHelper.show("onStopEngine error: ${e.message}", LogLevel.Error)
        }
    }
}
