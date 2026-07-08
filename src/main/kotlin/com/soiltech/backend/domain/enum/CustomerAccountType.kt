package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class CustomerAccountType(@JsonValue val value: String) {
    INDIVIDUAL("individual"),
    BUSINESS("business"),
    RETAIL_SHOP("retail_shop"),
    RESTAURANT("restaurant"),
    MARKET_TRADER("market_trader"),
    PROCESSOR("processor"),
    EXPORTER("exporter");

    companion object {
        @JsonCreator
        fun fromValue(value: String): CustomerAccountType =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown account type: $value")
    }
}
