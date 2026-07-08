package com.soiltech.backend.application.dto.farmer

import com.soiltech.backend.domain.enum.FarmerStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class FarmerResponse(
    val id: UUID,
    val farmerCode: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val phone: String,
    val email: String?,
    val nationalId: String?,
    val agentId: UUID,
    val agentName: String,
    val lbcId: UUID,
    val lbcName: String,
    val region: String,
    val district: String,
    val farmsCount: Long,
    val totalFarmSize: Double,
    val cropTypes: List<String>,
    val walletBalance: BigDecimal,
    val totalEarnings: BigDecimal,
    val kycVerified: Boolean,
    val status: FarmerStatus,
    val rejectionReason: String?,
    val lat: Double?,
    val lng: Double?,
    val joinedDate: LocalDateTime,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class FarmerSummaryResponse(
    val total: Int,
    val approved: Int,
    val pending: Int,
    val rejected: Int
)

data class RegisterFarmerRequest(
    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    val lastName: String,

    @field:NotBlank(message = "Phone is required")
    @field:Pattern(regexp = "^\\+?[0-9]{7,15}\$", message = "Invalid phone number")
    val phone: String,

    @field:Email(message = "Invalid email address")
    val email: String? = null,

    val nationalId: String? = null,

    @field:NotNull(message = "Agent ID is required")
    val agentId: UUID,

    @field:NotNull(message = "LBC ID is required")
    val lbcId: UUID,

    @field:NotBlank(message = "Region is required")
    val region: String,

    @field:NotBlank(message = "District is required")
    val district: String,

    val cropTypes: List<String> = emptyList()
)

data class UpdateFarmerRequest(
    @field:Size(min = 1, message = "First name must not be blank")
    val firstName: String? = null,

    @field:Size(min = 1, message = "Last name must not be blank")
    val lastName: String? = null,

    @field:Pattern(regexp = "^\\+?[0-9]{7,15}\$", message = "Invalid phone number")
    val phone: String? = null,

    @field:Email(message = "Invalid email address")
    val email: String? = null,

    val nationalId: String? = null,

    val agentId: UUID? = null,

    val lbcId: UUID? = null,

    val region: String? = null,

    val district: String? = null,

    val cropTypes: List<String>? = null
)

data class RejectFarmerRequest(
    val reason: String? = null
)
