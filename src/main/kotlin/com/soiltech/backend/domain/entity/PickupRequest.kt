package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.LogisticsStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class PickupRequest(
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
