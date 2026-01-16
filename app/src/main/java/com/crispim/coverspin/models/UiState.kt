package com.crispim.coverspin.models

data class SettingsState(
    val isEngineRunning: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val isGestureButtonEnabled: Boolean = true,
    val keepScreenOn: Boolean = false,
    val logLevel: LogLevel = LogLevel.Debug,
    val isInnerScreen: Boolean = false,
    val rotationMode: RotationMode = RotationMode.AUTO,
    val animationType: AnimationType = AnimationType.FADE,
    val animationDuration: Int = 300,
    val gesturesEnabled: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val autoStartOnBoot: Boolean = true,
    val batteryOptimizationDisabled: Boolean = false
)