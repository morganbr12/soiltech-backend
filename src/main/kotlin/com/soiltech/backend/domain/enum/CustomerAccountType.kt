package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class CustomerAccountType(@JsonValue val value: String) {
    INDIVIDUAL("individual"),
    BUSINESS("business"),
    RETAIL_SHOP("retail_shop");

    companion object {
        @JsonCreator
        fun fromValue(value: String): CustomerAccountType =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown account type: $value")
    }
}
