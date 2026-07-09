package com.soiltech.backend.application.dto.produce

import com.soiltech.backend.domain.enum.CollectionStatus
import com.soiltech.backend.domain.enum.ProduceListingStatus
import com.soiltech.backend.domain.enum.SyncStatus
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ProduceRecordDto(
    val id: UUID,
    val farmerId: UUID,
    val farmId: UUID?,
    val agentId: UUID,
    val cropType: String,
    val cropVariety: String?,
    val grade: String?,
    val quantityKg: BigDecimal,
    val pricePerKg: BigDecimal,
    val totalAmount: BigDecimal,
    val status: CollectionStatus,
    val listingStatus: ProduceListingStatus?,
    val collectedAt: LocalDateTime?,
    val notes: String?,
    val photos: List<String>,
    val syncStatus: SyncStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CreateProduceRecordRequest(
    @field:NotNull
    val farmerId: UUID,
    val farmId: UUID? = null,
    @field:NotBlank
    val cropType: String,
    val cropVariety: String? = null,
    val grade: String? = null,
    @field:NotNull @field:DecimalMin("0.001")
    val quantityKg: BigDecimal,
    @field:NotNull @field:DecimalMin("0.01")
    val pricePerKg: BigDecimal,
    val notes: String? = null,
    val collectedAt: LocalDateTime? = null
)

data class UpdateProduceRecordRequest(
    val status: CollectionStatus? = null,
    val quantityKg: BigDecimal? = null,
    val pricePerKg: BigDecimal? = null,
    val notes: String? = null,
    val collectedAt: LocalDateTime? = null,
    val syncStatus: SyncStatus? = null
)
