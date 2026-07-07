package com.soiltech.backend.application.dto.payment

import com.soiltech.backend.domain.enum.PaymentStatus
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class PaymentRecordDto(
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

data class CreatePaymentRecordRequest(
    @field:NotNull
    val farmerId: UUID,
    val produceRecordId: UUID? = null,
    @field:NotNull @field:DecimalMin("0.01")
    val amount: BigDecimal,
    val currency: String = "KES",
    val reference: String? = null,
    val paidAt: LocalDateTime? = null
)
