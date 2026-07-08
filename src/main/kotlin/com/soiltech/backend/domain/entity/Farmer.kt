package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.FarmerStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class Farmer(
    val id: UUID,
    val farmerCode: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String?,
    val nationalId: String?,
    val agentId: UUID,
    val agentName: String,
    val lbcId: UUID,
    val lbcName: String,
    val region: String,
    val district: String,
    val status: FarmerStatus,
    val kycVerified: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val cropTypes: List<String>,
    val rejectionReason: String?,
    val joinedDate: LocalDateTime,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val createdBy: UUID?,
    val updatedBy: UUID?
)

data class FarmerMetrics(
    val farmsCount: Long = 0,
    val totalFarmSize: Double = 0.0,
    val cropTypes: List<String> = emptyList(),
    val walletBalance: BigDecimal = BigDecimal.ZERO,
    val totalEarnings: BigDecimal = BigDecimal.ZERO
)
