package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.CustomerAccountType
import java.time.LocalDateTime
import java.util.UUID

data class CustomerProfile(
    val id: UUID,
    val userId: UUID,
    val fullName: String,
    val phone: String?,
    val address: String?,
    val profileImageUrl: String?,
    val accountType: CustomerAccountType,
    val location: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
