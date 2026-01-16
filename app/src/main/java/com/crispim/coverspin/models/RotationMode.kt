package com.crispim.coverspin.models

enum class RotationMode(val value: Int, override val displayName: String) : DisplayName {
    AUTO(0, "Auto"),
    PORTRAIT(1, "Portrait"),
    LANDSCAPE(2, "Landscape"),
    SENSOR(3, "Sensor"),
    LOCKED(4, "Locked");

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: AUTO
    }
}

