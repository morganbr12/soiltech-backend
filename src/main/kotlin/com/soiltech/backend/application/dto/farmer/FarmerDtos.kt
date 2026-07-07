package com.soiltech.backend.application.dto.farmer

import com.soiltech.backend.domain.enum.FarmerStatus
import com.soiltech.backend.domain.enum.SyncStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.LocalDateTime
import java.util.UUID

data class FarmerDto(
    val id: UUID,
    val agentId: UUID,
    val fullName: String,
    val phone: String,
    val nationalId: String?,
    val location: String?,
    val status: FarmerStatus,
    val syncStatus: SyncStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CreateFarmerRequest(
    @field:NotBlank
    val fullName: String,
    @field:NotBlank @field:Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Invalid phone number")
    val phone: String,
    val nationalId: String? = null,
    val location: String? = null
)

data class UpdateFarmerRequest(
    val fullName: String? = null,
    val phone: String? = null,
    val nationalId: String? = null,
    val location: String? = null,
    val status: FarmerStatus? = null
)
