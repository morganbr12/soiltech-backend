package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class PaymentRecord(
    val id: UUID,
    val farmerId: UUID,
    val agentId: UUID,
    val produceRecordId: UUID?,
    val amount: BigDecimal,
    val currency: String,
    val status: PaymentStatus,
    val reference: String?,
    val paidAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
