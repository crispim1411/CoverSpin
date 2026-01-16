package com.crispim.coverspin.models

enum class GestureType(val value: Int, val displayName: String) {
    SWIPE_UP(0, "Swipe Up"),
    SWIPE_DOWN(1, "Swipe Down"),
    SWIPE_LEFT(2, "Swipe Left"),
    SWIPE_RIGHT(3, "Swipe Right"),
    DOUBLE_TAP(4, "Double Tap"),
    LONG_PRESS(5, "Long Press");

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: SWIPE_UP
    }
}

