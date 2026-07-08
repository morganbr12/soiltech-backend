package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class WalletStatus(@JsonValue val value: String) {
    ACTIVE("active"),
    FROZEN("frozen"),
    CLOSED("closed");

    companion object {
        @JsonCreator
        fun fromValue(value: String): WalletStatus =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown WalletStatus: $value")
    }
}
