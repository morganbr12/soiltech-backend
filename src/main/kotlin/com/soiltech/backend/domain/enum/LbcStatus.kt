package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class LbcStatus(@JsonValue val value: String) {
    ACTIVE("ACTIVE"),
    PENDING("PENDING"),
    SUSPENDED("SUSPENDED"),
    INACTIVE("INACTIVE");

    companion object {
        @JsonCreator
        fun fromValue(value: String): LbcStatus =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown LBC status: $value. Accepted: ${entries.joinToString { it.value }}")
    }
}
