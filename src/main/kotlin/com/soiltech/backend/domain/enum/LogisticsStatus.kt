package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class LogisticsStatus(@JsonValue val value: String) {
    PENDING("pending"),
    IN_TRANSIT("inTransit"),
    DELIVERED("delivered"),
    CANCELLED("cancelled");

    companion object {
        @JsonCreator
        fun fromValue(value: String): LogisticsStatus =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown LogisticsStatus: $value")
    }
}
