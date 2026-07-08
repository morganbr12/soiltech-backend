package com.soiltech.backend.application.dto.lbc

import com.soiltech.backend.domain.enum.LbcStatus
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

// ── Request DTOs ──────────────────────────────────────────────────────────────

data class CreateLbcRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Code is required")
    @field:Size(max = 50, message = "Code must not exceed 50 characters")
    val code: String,

    @field:NotBlank(message = "Region is required")
    val region: String,

    @field:NotBlank(message = "District is required")
    val district: String,

    @field:NotBlank(message = "Manager name is required")
    val manager: String,

    @field:NotBlank(message = "Phone is required")
    @field:Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number")
    val phone: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email address")
    val email: String
)

data class UpdateLbcRequest(
    @field:Size(min = 1, message = "Name must not be blank")
    val name: String? = null,

    @field:Size(min = 1, max = 50, message = "Code must not be blank or exceed 50 characters")
    val code: String? = null,

    val region: String? = null,
    val district: String? = null,
    val manager: String? = null,

    @field:Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number")
    val phone: String? = null,

    @field:Email(message = "Invalid email address")
    val email: String? = null,

    @field:Min(value = 0, message = "Agents count must be 0 or greater")
    val agents: Int? = null,

    @field:Min(value = 0, message = "Farmers count must be 0 or greater")
    val farmers: Int? = null,

    @field:DecimalMin(value = "0.0", message = "Produce tonnes must be 0 or greater")
    val produceTonnes: BigDecimal? = null,

    @field:DecimalMin(value = "0.0", message = "Revenue must be 0 or greater")
    val revenue: BigDecimal? = null,

    @field:Min(value = 0, message = "Compliance must be between 0 and 100")
    @field:Max(value = 100, message = "Compliance must be between 0 and 100")
    val compliance: Int? = null,

    val status: LbcStatus? = null,
    val joinedDate: LocalDateTime? = null
)

data class BulkSuspendRequest(
    @field:NotEmpty(message = "At least one ID is required")
    val ids: List<UUID>
)

// ── Response DTOs ─────────────────────────────────────────────────────────────

data class LbcResponse(
    val id: String,
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
    val updatedAt: LocalDateTime
)

data class LbcSummaryResponse(
    val total: Int,
    val active: Int,
    val pending: Int,
    val suspended: Int,
    val inactive: Int
)

data class BulkSuspendResponse(
    val succeeded: List<String>,
    val skipped: List<String>,
    val failures: List<BulkSuspendFailure>
)

data class BulkSuspendFailure(
    val id: String,
    val reason: String
)
