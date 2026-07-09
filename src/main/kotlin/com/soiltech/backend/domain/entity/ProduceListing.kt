package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.ProduceListingStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ProduceListing(
    val id: UUID,
    val produceRecordId: UUID,
    val farmerId: UUID,
    val farmId: UUID?,
    val agentId: UUID,
    val lbcId: UUID?,
    val cropType: String,
    val cropVariety: String?,
    val grade: String?,
    val totalQuantityKg: BigDecimal,
    val availableQuantityKg: BigDecimal,
    val pricePerKg: BigDecimal,
    val status: ProduceListingStatus,
    val region: String?,
    val district: String?,
    val agentName: String?,
    val farmerName: String?,
    val lbcName: String?,
    val photos: List<String>,
    val collectedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
