package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class CollectionStatus(@JsonValue val value: String) {
    PENDING("pending"),
    COLLECTED("collected"),
    REJECTED("rejected");

    companion object {
        @JsonCreator
        fun fromValue(value: String): CollectionStatus =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown CollectionStatus: $value")
    }
}
