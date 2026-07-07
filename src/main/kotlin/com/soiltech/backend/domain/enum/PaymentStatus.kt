package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class PaymentStatus(@JsonValue val value: String) {
    PENDING("pending"),
    COMPLETED("completed"),
    FAILED("failed"),
    REFUNDED("refunded");

    companion object {
        @JsonCreator
        fun fromValue(value: String): PaymentStatus =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown PaymentStatus: $value")
    }
}
