package com.crispim.coverspin.models

enum class LogLevel(val value: Int, val displayName: String) {
    Error(0, "Error"),
    Info(1, "Info"),
    Debug(2, "Debug");

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: Info
    }
}