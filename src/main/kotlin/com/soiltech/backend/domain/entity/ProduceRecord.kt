package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.CollectionStatus
import com.soiltech.backend.domain.enum.SyncStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ProduceRecord(
    val id: UUID,
    val farmerId: UUID,
    val farmId: UUID?,
    val agentId: UUID,
    val cropType: String,
    val quantityKg: BigDecimal,
    val pricePerKg: BigDecimal,
    val totalAmount: BigDecimal,
    val status: CollectionStatus,
    val collectedAt: LocalDateTime?,
    val notes: String?,
    val syncStatus: SyncStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
