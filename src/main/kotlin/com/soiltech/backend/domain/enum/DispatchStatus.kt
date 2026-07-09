package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class DispatchStatus(@JsonValue val value: String) {
    PENDING("pending"),
    EN_ROUTE("enRoute"),
    PICKED_UP("pickedUp"),
    DELIVERED("delivered"),
    CANCELLED("cancelled");

    companion object {
        @JsonCreator
        fun fromValue(value: String): DispatchStatus =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown DispatchStatus: $value")
    }
}
