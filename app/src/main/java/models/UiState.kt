package models

data class SettingsState(
    val hasOverlayPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val isEngineRunning: Boolean = false,
    val volumeShortcutsEnabled: Boolean = false,
    val isGestureButtonEnabled: Boolean = false,
    val keepScreenOn: Boolean = false,
    val logLevel: LogLevel = LogLevel.DEBUG,
    val isInnerScreen: Boolean = false
)