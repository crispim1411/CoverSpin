package com.crispim.coverspin.models

enum class AnimationType(val value: Int, override val displayName: String, val durationMs: Int) : DisplayName {
    MINIMAL(0, "Minimal", 0),
    FADE(1, "Fade", 300),
    SLIDE(2, "Slide", 300),
    ROTATE(3, "Rotate", 400);

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: FADE
    }
}

