package com.soiltech.backend.application.dto.farm

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class FarmDto(
    val id: UUID,
    val farmerId: UUID,
    val name: String,
    val sizeHectares: Double?,
    val cropType: String?,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val estimatedYieldKg: Double?,
    val lastHarvestDate: LocalDate?,
    val photos: List<String> = emptyList(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AdminFarmListDto(
    val farmId: UUID,
    val farmName: String,
    val farmerName: String,
    val region: String,
    val district: String,
    val cropType: String?,
    val sizeHectares: Double?,
    val estimatedYieldKg: Double?,
    val lastHarvestDate: LocalDate?,
    val registeredDate: LocalDateTime
)

data class CreateFarmRequest(
    @field:NotBlank
    val name: String,
    @field:Positive
    val sizeHectares: Double? = null,
    val cropType: String? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val estimatedYieldKg: Double? = null,
    val lastHarvestDate: LocalDate? = null
)

data class UpdateFarmRequest(
    val name: String? = null,
    val sizeHectares: Double? = null,
    val cropType: String? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val estimatedYieldKg: Double? = null,
    val lastHarvestDate: LocalDate? = null
)
