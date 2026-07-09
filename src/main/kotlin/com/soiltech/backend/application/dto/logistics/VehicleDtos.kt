package com.soiltech.backend.application.dto.logistics

import com.soiltech.backend.domain.enum.VehicleStatus
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class VehicleDto(
    val id: UUID,
    val carPlateNumber: String,
    val vehicleType: String,
    val make: String,
    val model: String,
    val year: Int,
    val capacity: BigDecimal,
    val fuelLevel: Double,
    val region: String,
    val driverName: String,
    val status: VehicleStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CreateVehicleRequest(
    @field:NotBlank val carPlateNumber: String,
    @field:NotBlank val vehicleType: String,
    @field:NotBlank val make: String,
    @field:NotBlank val model: String,
    @field:NotNull @field:Min(1900) val year: Int,
    @field:NotNull val capacity: BigDecimal,
    @field:DecimalMin("0.0") @field:DecimalMax("100.0") val fuelLevel: Double = 100.0,
    @field:NotBlank val region: String,
    @field:NotBlank val driverName: String,
    val status: VehicleStatus = VehicleStatus.AVAILABLE
)

data class VehicleKpisDto(
    val totalVehicles: Long,
    val available: Long,
    val onRoute: Long,
    val maintenance: Long,
    val inactive: Long
)

data class UpdateVehicleRequest(
    val vehicleType: String? = null,
    val make: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val capacity: BigDecimal? = null,
    @field:DecimalMin("0.0") @field:DecimalMax("100.0") val fuelLevel: Double? = null,
    val region: String? = null,
    val driverName: String? = null,
    val status: VehicleStatus? = null
)
