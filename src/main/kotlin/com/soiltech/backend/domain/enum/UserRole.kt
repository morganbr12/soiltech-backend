package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class UserRole(@JsonValue val value: String) {
    AGENT("agent"),
    CUSTOMER("customer"),
    ADMIN("admin"),
    LBC("lbc");

    companion object {
        @JsonCreator
        fun fromValue(value: String): UserRole =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown role: $value")
    }
}
