package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.LbcStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class Lbc(
    val id: UUID,
    val userId: UUID?,
    val name: String,
    val code: String,
    val region: String,
    val district: String,
    val manager: String,
    val phone: String,
    val email: String,
    val agents: Int,
    val farmers: Int,
    val produceTonnes: BigDecimal,
    val revenue: BigDecimal,
    val compliance: Int,
    val status: LbcStatus,
    val joinedDate: LocalDateTime,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val createdBy: UUID?,
    val updatedBy: UUID?
)
