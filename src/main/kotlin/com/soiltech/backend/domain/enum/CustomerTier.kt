package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class CustomerTier(@JsonValue val value: String) {
    BRONZE("bronze"),
    SILVER("silver"),
    GOLD("gold"),
    PLATINUM("platinum");

    companion object {
        @JsonCreator
        fun fromValue(value: String): CustomerTier =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown CustomerTier: $value")
    }
}
