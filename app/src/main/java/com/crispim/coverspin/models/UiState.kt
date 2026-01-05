package com.crispim.coverspin.models

data class SettingsState(
    val isEngineRunning: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val isGestureButtonEnabled: Boolean = true,
    val keepScreenOn: Boolean = false,
    val logLevel: LogLevel = LogLevel.DEBUG,
    val isInnerScreen: Boolean = false
)