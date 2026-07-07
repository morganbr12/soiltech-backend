package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class FarmerStatus(@JsonValue val value: String) {
    ACTIVE("active"),
    INACTIVE("inactive"),
    SUSPENDED("suspended");

    companion object {
        @JsonCreator
        fun fromValue(value: String): FarmerStatus =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown FarmerStatus: $value")
    }
}
