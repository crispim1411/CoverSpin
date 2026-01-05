package models

enum class LogLevel(val value: Int, val displayName: String) {
    Error(0, "Error"),
    INFO(1, "Info"),
    DEBUG(2, "Debug");

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: INFO
    }
}