package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ChatSenderType(@JsonValue val value: String) {
    CUSTOMER("customer"),
    AGENT("agent"),
    SYSTEM("system");

    companion object {
        @JsonCreator
        fun fromValue(value: String): ChatSenderType =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown ChatSenderType: $value")
    }
}
