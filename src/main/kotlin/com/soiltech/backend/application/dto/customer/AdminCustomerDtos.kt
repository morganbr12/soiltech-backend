package com.soiltech.backend.application.dto.customer

import com.soiltech.backend.domain.enum.CustomerAccountType
import com.soiltech.backend.domain.enum.CustomerStatus
import com.soiltech.backend.domain.enum.CustomerTier
import com.soiltech.backend.domain.enum.KycDocumentType
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class AdminCustomerResponse(
    val id: UUID,
    val customerCode: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String?,
    val phone: String?,
    val region: String?,
    val district: String?,
    val address: String?,
    val status: CustomerStatus,
    val tier: CustomerTier,
    val totalOrders: Long,
    val totalSpent: BigDecimal,
    val walletBalance: BigDecimal,
    val rating: Double,
    val joinedDate: LocalDateTime,
    val lastOrderDate: LocalDateTime?,
    val verifiedDate: LocalDateTime?,
    val lat: Double?,
    val lng: Double?,
    val businessName: String?,
    val businessType: String?,
    val nationalId: String?,
    val isVerified: Boolean,
    val accountType: CustomerAccountType,
    val kycDocuments: List<KycDocumentDto> = emptyList(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AdminCustomerSummaryResponse(
    val total: Long,
    val active: Long,
    val verified: Long,
    val pending: Long,
    val suspended: Long,
    val rejected: Long
)

data class KycDocumentDto(
    val type: KycDocumentType,
    val url: String,
    val uploadedAt: LocalDateTime
)

data class CreateCustomerRequest(
    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    val lastName: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email address")
    val email: String,

    @field:NotBlank(message = "Phone is required")
    @field:Pattern(regexp = "^\\+?[0-9]{7,15}\$", message = "Invalid phone number")
    val phone: String,

    val region: String? = null,
    val district: String? = null,
    val address: String? = null,
    val businessName: String? = null,
    val businessType: String? = null,
    val nationalId: String? = null,

    @field:NotNull(message = "Account type is required")
    val accountType: CustomerAccountType = CustomerAccountType.INDIVIDUAL,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String
)

data class UpdateCustomerAdminRequest(
    @field:Size(min = 1) val firstName: String? = null,
    @field:Size(min = 1) val lastName: String? = null,
    @field:Email val email: String? = null,
    @field:Pattern(regexp = "^\\+?[0-9]{7,15}\$", message = "Invalid phone number") val phone: String? = null,
    val region: String? = null,
    val district: String? = null,
    val address: String? = null,
    val businessName: String? = null,
    val businessType: String? = null,
    val nationalId: String? = null,
    val accountType: CustomerAccountType? = null,
    val tier: CustomerTier? = null,
    val rating: Double? = null,
    val lat: Double? = null,
    val lng: Double? = null
)

data class RejectCustomerRequest(val reason: String? = null)
data class SuspendCustomerRequest(val reason: String? = null)
