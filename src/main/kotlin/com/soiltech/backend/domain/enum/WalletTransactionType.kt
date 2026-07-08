package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class WalletTransactionType(@JsonValue val value: String) {
    DEPOSIT("deposit"),
    WITHDRAWAL("withdrawal"),
    PAYMENT("payment"),
    REFUND("refund");

    companion object {
        @JsonCreator
        fun fromValue(value: String): WalletTransactionType =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown WalletTransactionType: $value")
    }
}
