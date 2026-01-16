package com.crispim.coverspin.models

enum class GestureAction(val value: Int, val displayName: String) {
    PORTRAIT(0, "Force Portrait"),
    LANDSCAPE(1, "Force Landscape"),
    TOGGLE(2, "Toggle Rotation"),
    QUICK_TOGGLE(3, "Quick Toggle"),
    OPEN_SETTINGS(4, "Open Settings"),
    NONE(5, "None");

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: NONE
    }
}

