package com.soiltech.backend.application.dto.agent

import com.soiltech.backend.domain.enum.AgentStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class AgentProfileDto(
    val id: UUID,
    val userId: UUID,
    val fullName: String,
    val firstName: String,
    val lastName: String,
    val agentCode: String,
    val region: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AgentResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val agentCode: String,
    val lbcId: UUID,
    val lbcName: String,
    val region: String,
    val district: String,
    val farmersCount: Long,
    val farmsCount: Long,
    val produceCollected: BigDecimal,
    val status: AgentStatus,
    val lat: Double?,
    val lng: Double?,
    val lastSeen: LocalDateTime?,
    val joinedDate: LocalDateTime,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AgentSummaryResponse(
    val total: Int,
    val active: Int,
    val inactive: Int,
    val suspended: Int
)

data class RegisterAgentRequest(
    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    val lastName: String,

    @field:NotBlank(message = "Phone is required")
    @field:Pattern(regexp = "^\\+?[0-9]{7,15}\$", message = "Invalid phone number")
    val phone: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email address")
    val email: String,

    @field:NotNull(message = "LBC ID is required")
    val lbcId: UUID,

    @field:NotBlank(message = "Region is required")
    val region: String,

    @field:NotBlank(message = "District is required")
    val district: String
)

data class UpdateAgentRequest(
    @field:Size(min = 1, message = "First name must not be blank")
    val firstName: String? = null,

    @field:Size(min = 1, message = "Last name must not be blank")
    val lastName: String? = null,

    @field:Pattern(regexp = "^\\+?[0-9]{7,15}\$", message = "Invalid phone number")
    val phone: String? = null,

    @field:Email(message = "Invalid email address")
    val email: String? = null,

    val lbcId: UUID? = null,

    val region: String? = null,

    val district: String? = null,

    val status: AgentStatus? = null
)
