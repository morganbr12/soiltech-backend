package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ReviewStatus(@JsonValue val value: String) {
    PENDING("pending"),
    APPROVED("approved"),
    FLAGGED("flagged"),
    REJECTED("rejected");

    companion object {
        @JsonCreator
        fun fromValue(value: String): ReviewStatus =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown ReviewStatus: $value")
    }
}
