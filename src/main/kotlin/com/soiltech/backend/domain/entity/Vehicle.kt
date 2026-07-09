package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.VehicleStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class Vehicle(
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
    val updatedAt: LocalDateTime,
    val createdBy: UUID?,
    val updatedBy: UUID?
)
