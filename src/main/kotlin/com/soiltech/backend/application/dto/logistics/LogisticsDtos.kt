package com.soiltech.backend.application.dto.logistics

import com.soiltech.backend.domain.enum.LogisticsStatus
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class PickupRequestDto(
    val id: UUID,
    val farmerId: UUID,
    val agentId: UUID,
    val produceRecordId: UUID?,
    val scheduledDate: LocalDate,
    val status: LogisticsStatus,
    val notes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CreatePickupRequestRequest(
    @field:NotNull
    val farmerId: UUID,
    val produceRecordId: UUID? = null,
    @field:NotNull @field:Future
    val scheduledDate: LocalDate,
    val notes: String? = null
)

data class UpdatePickupRequestRequest(
    val scheduledDate: LocalDate? = null,
    val status: LogisticsStatus? = null,
    val notes: String? = null
)
