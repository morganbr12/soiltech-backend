package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ChatStatus(@JsonValue val value: String) {
    OPEN("open"),
    PENDING("pending"),
    RESOLVED("resolved"),
    ESCALATED("escalated");

    companion object {
        @JsonCreator
        fun fromValue(value: String): ChatStatus =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown ChatStatus: $value")
    }
}
