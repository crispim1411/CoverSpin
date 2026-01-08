package com.crispim.coverspin.models

interface DisplayName {
    val displayName: String
}

enum class LogLevel(val value: Int, override val displayName: String) : DisplayName {
    Error(0, "Error"),
    Info(1, "Info"),
    Debug(2, "Debug");

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: Info
    }
}