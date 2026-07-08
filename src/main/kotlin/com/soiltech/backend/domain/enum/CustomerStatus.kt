package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class CustomerStatus(@JsonValue val value: String) {
    PENDING("pending"),
    VERIFIED("verified"),
    ACTIVE("active"),
    SUSPENDED("suspended"),
    REJECTED("rejected");

    companion object {
        @JsonCreator
        fun fromValue(value: String): CustomerStatus =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown CustomerStatus: $value")
    }
}
