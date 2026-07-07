package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class OrderStatus(@JsonValue val value: String) {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    PROCESSING("processing"),
    SHIPPED("shipped"),
    DELIVERED("delivered"),
    CANCELLED("cancelled");

    companion object {
        @JsonCreator
        fun fromValue(value: String): OrderStatus =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown OrderStatus: $value")
    }
}
