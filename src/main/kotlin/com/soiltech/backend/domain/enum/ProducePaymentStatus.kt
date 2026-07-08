package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ProducePaymentStatus(@JsonValue val value: String) {
    UNPAID("unpaid"),
    PARTIAL("partial"),
    PAID("paid");

    companion object {
        @JsonCreator
        fun fromValue(value: String): ProducePaymentStatus =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown ProducePaymentStatus: $value")
    }
}
