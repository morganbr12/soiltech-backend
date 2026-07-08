package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ReviewTargetType(@JsonValue val value: String) {
    AGENT("agent"),
    DRIVER("driver"),
    WAREHOUSE("warehouse"),
    PRODUCE("produce"),
    LBC("lbc");

    companion object {
        @JsonCreator
        fun fromValue(value: String): ReviewTargetType =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown ReviewTargetType: $value")
    }
}
