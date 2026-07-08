package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class NotificationCategory(@JsonValue val value: String) {
    ORDER("order"),
    WALLET("wallet"),
    PROMO("promo"),
    SYSTEM("system");

    companion object {
        @JsonCreator
        fun fromValue(value: String): NotificationCategory =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown NotificationCategory: $value")
    }
}
