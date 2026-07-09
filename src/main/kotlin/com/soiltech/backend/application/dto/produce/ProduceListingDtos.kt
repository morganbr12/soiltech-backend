package com.soiltech.backend.application.dto.produce

import com.soiltech.backend.domain.enum.ProduceListingStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ProduceListingDto(
    val id: UUID,
    val produceRecordId: UUID,
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
    val collectedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
