package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.CustomerAccountType
import com.soiltech.backend.domain.enum.CustomerStatus
import com.soiltech.backend.domain.enum.CustomerTier
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class CustomerProfile(
    val id: UUID,
    val userId: UUID,
    val customerCode: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val fullName: String,
    val email: String? = null,
    val phone: String?,
    val region: String? = null,
    val district: String? = null,
    val address: String?,
    val profileImageUrl: String?,
    val accountType: CustomerAccountType,
    val location: String?,
    val status: CustomerStatus = CustomerStatus.PENDING,
    val tier: CustomerTier = CustomerTier.BRONZE,
    val businessName: String? = null,
    val businessType: String? = null,
    val nationalId: String? = null,
    val isVerified: Boolean = false,
    val verifiedDate: LocalDateTime? = null,
    val rejectionReason: String? = null,
    val rating: Double = 0.0,
    val lat: Double? = null,
    val lng: Double? = null,
    val joinedDate: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CustomerMetrics(
    val totalOrders: Long = 0,
    val totalSpent: BigDecimal = BigDecimal.ZERO,
    val walletBalance: BigDecimal = BigDecimal.ZERO,
    val lastOrderDate: LocalDateTime? = null
)
