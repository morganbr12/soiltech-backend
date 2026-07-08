package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class FarmerStatus(@JsonValue val value: String) {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");

    companion object {
        @JsonCreator
        fun fromValue(value: String): FarmerStatus =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown FarmerStatus: $value. Accepted: ${entries.map { it.value }}")
    }
}
