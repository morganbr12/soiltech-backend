package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.KycDocumentType
import java.time.LocalDateTime
import java.util.UUID

data class KycDocument(
    val id: UUID,
    val customerId: UUID,
    val type: KycDocumentType,
    val url: String,
    val uploadedAt: LocalDateTime
)
