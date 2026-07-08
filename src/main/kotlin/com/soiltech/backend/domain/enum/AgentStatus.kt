package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class AgentStatus(@JsonValue val value: String) {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    SUSPENDED("SUSPENDED");

    companion object {
        @JsonCreator
        fun fromValue(value: String): AgentStatus =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown agent status: $value. Accepted: ${entries.joinToString { it.value }}")
    }
}
