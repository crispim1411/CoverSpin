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
import com.crispim.coverspin.models.*

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
                    logLevel = cacheHelper.getLogLevel(),
                    rotationMode = RotationMode.fromInt(cacheHelper.getRotationMode()),
                    animationType = cacheHelper.getAnimationType(),
                    animationDuration = cacheHelper.getAnimationDuration(),
                    gesturesEnabled = cacheHelper.isGesturesEnabled(),
                    hapticFeedbackEnabled = cacheHelper.isHapticFeedbackEnabled(),
                    autoStartOnBoot = cacheHelper.isAutoStartOnBoot(),
                    batteryOptimizationDisabled = cacheHelper.isBatteryOptimizationDisabled()
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

    fun setRotationMode(mode: RotationMode) {
        try {
            cacheHelper.setRotationMode(mode.value)
            _uiState.update { it.copy(rotationMode = mode) }
            // Apply rotation if engine is running
            if (EngineActivity.isOverlayActive) {
                EngineActivity.setRotationEnabled(mode != RotationMode.LOCKED)
            }
        } catch (e: Exception) {
            toastHelper.show("setRotationMode error: ${e.message}", LogLevel.Error)
        }
    }

    fun setAnimationType(type: AnimationType) {
        try {
            cacheHelper.setAnimationType(type)
            _uiState.update { it.copy(animationType = type) }
        } catch (e: Exception) {
            toastHelper.show("setAnimationType error: ${e.message}", LogLevel.Error)
        }
    }

    fun setAnimationDuration(duration: Int) {
        try {
            cacheHelper.setAnimationDuration(duration)
            _uiState.update { it.copy(animationDuration = duration) }
        } catch (e: Exception) {
            toastHelper.show("setAnimationDuration error: ${e.message}", LogLevel.Error)
        }
    }

    fun setGesturesEnabled(enabled: Boolean) {
        try {
            cacheHelper.setGesturesEnabled(enabled)
            _uiState.update { it.copy(gesturesEnabled = enabled) }
        } catch (e: Exception) {
            toastHelper.show("setGesturesEnabled error: ${e.message}", LogLevel.Error)
        }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        try {
            cacheHelper.setHapticFeedbackEnabled(enabled)
            _uiState.update { it.copy(hapticFeedbackEnabled = enabled) }
        } catch (e: Exception) {
            toastHelper.show("setHapticFeedbackEnabled error: ${e.message}", LogLevel.Error)
        }
    }

    fun setAutoStartOnBoot(enabled: Boolean) {
        try {
            cacheHelper.setAutoStartOnBoot(enabled)
            _uiState.update { it.copy(autoStartOnBoot = enabled) }
        } catch (e: Exception) {
            toastHelper.show("setAutoStartOnBoot error: ${e.message}", LogLevel.Error)
        }
    }

    fun setBatteryOptimizationDisabled(disabled: Boolean) {
        try {
            cacheHelper.setBatteryOptimizationDisabled(disabled)
            _uiState.update { it.copy(batteryOptimizationDisabled = disabled) }
        } catch (e: Exception) {
            toastHelper.show("setBatteryOptimizationDisabled error: ${e.message}", LogLevel.Error)
        }
    }
}
