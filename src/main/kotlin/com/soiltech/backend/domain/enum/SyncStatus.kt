package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class SyncStatus(@JsonValue val value: String) {
    SYNCED("synced"),
    PENDING("pending"),
    CONFLICT("conflict");

    companion object {
        @JsonCreator
        fun fromValue(value: String): SyncStatus =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown SyncStatus: $value")
    }
}
