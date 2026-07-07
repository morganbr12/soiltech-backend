package com.soiltech.backend.application.dto.customer

import com.soiltech.backend.domain.enum.CustomerAccountType
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime
import java.util.UUID

data class CustomerProfileDto(
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

data class UpdateCustomerProfileRequest(
    @field:NotBlank
    val fullName: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val profileImageUrl: String? = null,
    val location: String? = null
)
