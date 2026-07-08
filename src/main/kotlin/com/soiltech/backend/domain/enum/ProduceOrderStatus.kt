package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ProduceOrderStatus(@JsonValue val value: String) {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    PROCESSING("processing"),
    DELIVERED("delivered"),
    CANCELLED("cancelled");

    companion object {
        @JsonCreator
        fun fromValue(value: String): ProduceOrderStatus =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown ProduceOrderStatus: $value")
    }
}
