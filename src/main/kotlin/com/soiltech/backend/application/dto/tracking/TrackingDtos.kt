package com.soiltech.backend.application.dto.tracking

import com.soiltech.backend.domain.enum.AgentStatus
import com.soiltech.backend.domain.enum.DispatchStatus
import com.soiltech.backend.domain.enum.VehicleStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class TrackingVehicleDto(
    val id: UUID,
    val carPlateNumber: String,
    val make: String,
    val model: String,
    val year: Int,
    val vehicleType: String,
    val status: VehicleStatus,
    val fuelLevel: Double,
    val capacity: BigDecimal,
    val region: String,
    val driverName: String,
    val lat: Double?,
    val lng: Double?,
    val speed: Double?,
    val heading: Double?,
    val lastUpdated: LocalDateTime?
)

data class TrackingVehicleSummary(
    val total: Long,
    val onRoute: Long,
    val available: Long,
    val maintenance: Long,
    val offline: Long
)

data class TrackingVehiclesResponse(
    val data: List<TrackingVehicleDto>,
    val summary: TrackingVehicleSummary
)

data class UpdateVehiclePositionRequest(
    val lat: Double,
    val lng: Double,
    val speed: Double = 0.0,
    val heading: Double = 0.0
)

data class VehicleLocationHistoryDto(
    val lat: Double,
    val lng: Double,
    val speed: Double,
    val heading: Double,
    val timestamp: LocalDateTime
)

data class TrackingAgentDto(
    val id: UUID,
    val fullName: String,
    val agentCode: String,
    val lat: Double,
    val lng: Double,
    val lastSeen: LocalDateTime?,
    val status: AgentStatus
)

data class ActiveDispatchDto(
    val id: UUID,
    val vehicleId: UUID,
    val driverName: String,
    val plateNumber: String,
    val vehicleType: String,
    val pickupLocation: String?,
    val status: DispatchStatus,
    val scheduledDate: LocalDate
)
