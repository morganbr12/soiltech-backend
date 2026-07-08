package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class KycDocumentType(@JsonValue val value: String) {
    NATIONAL_ID("national_id"),
    PROOF_OF_ADDRESS("proof_of_address"),
    BUSINESS_CERT("business_cert");

    companion object {
        @JsonCreator
        fun fromValue(value: String): KycDocumentType =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown KycDocumentType: $value")
    }
}
