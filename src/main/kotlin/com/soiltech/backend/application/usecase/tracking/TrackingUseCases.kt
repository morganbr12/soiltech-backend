package com.soiltech.backend.application.usecase.tracking

import com.soiltech.backend.application.dto.tracking.ActiveDispatchDto
import com.soiltech.backend.application.dto.tracking.TrackingAgentDto
import com.soiltech.backend.application.dto.tracking.TrackingVehicleDto
import com.soiltech.backend.application.dto.tracking.TrackingVehicleSummary
import com.soiltech.backend.application.dto.tracking.TrackingVehiclesResponse
import com.soiltech.backend.application.dto.tracking.UpdateVehiclePositionRequest
import com.soiltech.backend.application.dto.tracking.VehicleLocationHistoryDto
import com.soiltech.backend.domain.entity.VehicleLocation
import com.soiltech.backend.domain.entity.VehicleLocationHistory
import com.soiltech.backend.domain.enum.VehicleStatus
import com.soiltech.backend.domain.repository.AgentRepository
import com.soiltech.backend.domain.repository.DriverDispatchRepository
import com.soiltech.backend.domain.repository.VehicleLocationHistoryRepository
import com.soiltech.backend.domain.repository.VehicleLocationRepository
import com.soiltech.backend.domain.repository.VehicleRepository
import com.soiltech.backend.infrastructure.service.TrackingService
import com.soiltech.backend.interfaces.exception.NotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class GetTrackingVehiclesUseCase(
    private val vehicleRepository: VehicleRepository,
    private val vehicleLocationRepository: VehicleLocationRepository
) {
    fun execute(): TrackingVehiclesResponse {
        val vehicles = vehicleRepository.findAllVehicles()
        val locationsByVehicleId = vehicleLocationRepository.findAll().associateBy { it.vehicleId }

        val dtos = vehicles.map { v ->
            val loc = locationsByVehicleId[v.id]
            TrackingVehicleDto(
                id = v.id,
                carPlateNumber = v.carPlateNumber,
                make = v.make,
                model = v.model,
                year = v.year,
                vehicleType = v.vehicleType,
                status = v.status,
                fuelLevel = v.fuelLevel,
                capacity = v.capacity,
                region = v.region,
                driverName = v.driverName,
                lat = loc?.lat,
                lng = loc?.lng,
                speed = loc?.speed,
                heading = loc?.heading,
                lastUpdated = loc?.lastUpdated
            )
        }

        val summary = TrackingVehicleSummary(
            total = vehicles.size.toLong(),
            onRoute = vehicles.count { it.status == VehicleStatus.ON_ROUTE }.toLong(),
            available = vehicles.count { it.status == VehicleStatus.AVAILABLE }.toLong(),
            maintenance = vehicles.count { it.status == VehicleStatus.MAINTENANCE }.toLong(),
            offline = vehicles.count { it.status == VehicleStatus.INACTIVE }.toLong()
        )

        return TrackingVehiclesResponse(data = dtos, summary = summary)
    }
}

@Service
class UpdateVehiclePositionUseCase(
    private val vehicleRepository: VehicleRepository,
    private val vehicleLocationRepository: VehicleLocationRepository,
    private val vehicleLocationHistoryRepository: VehicleLocationHistoryRepository,
    private val trackingService: TrackingService
) {
    fun execute(vehicleId: UUID, request: UpdateVehiclePositionRequest): TrackingVehicleDto {
        val vehicle = vehicleRepository.findById(vehicleId)
            ?: throw NotFoundException("Vehicle not found")

        val now = LocalDateTime.now()
        val existingId = vehicleLocationRepository.findByVehicleId(vehicleId)?.id ?: UUID.randomUUID()

        val location = vehicleLocationRepository.upsert(
            VehicleLocation(
                id = existingId,
                vehicleId = vehicleId,
                lat = request.lat,
                lng = request.lng,
                speed = request.speed,
                heading = request.heading,
                lastUpdated = now
            )
        )

        vehicleLocationHistoryRepository.save(
            VehicleLocationHistory(
                id = UUID.randomUUID(),
                vehicleId = vehicleId,
                lat = request.lat,
                lng = request.lng,
                speed = request.speed,
                heading = request.heading,
                recordedAt = now
            )
        )

        trackingService.broadcast(vehicleId, request.lat, request.lng, request.speed, request.heading, now)

        return TrackingVehicleDto(
            id = vehicle.id,
            carPlateNumber = vehicle.carPlateNumber,
            make = vehicle.make,
            model = vehicle.model,
            year = vehicle.year,
            vehicleType = vehicle.vehicleType,
            status = vehicle.status,
            fuelLevel = vehicle.fuelLevel,
            capacity = vehicle.capacity,
            region = vehicle.region,
            driverName = vehicle.driverName,
            lat = location.lat,
            lng = location.lng,
            speed = location.speed,
            heading = location.heading,
            lastUpdated = location.lastUpdated
        )
    }
}

@Service
class GetVehicleLocationHistoryUseCase(
    private val vehicleRepository: VehicleRepository,
    private val vehicleLocationHistoryRepository: VehicleLocationHistoryRepository
) {
    fun execute(vehicleId: UUID, from: LocalDateTime, to: LocalDateTime): List<VehicleLocationHistoryDto> {
        vehicleRepository.findById(vehicleId)
            ?: throw NotFoundException("Vehicle not found")
        return vehicleLocationHistoryRepository.findByVehicleIdBetween(vehicleId, from, to)
            .map {
                VehicleLocationHistoryDto(
                    lat = it.lat,
                    lng = it.lng,
                    speed = it.speed,
                    heading = it.heading,
                    timestamp = it.recordedAt
                )
            }
    }
}

@Service
class GetTrackingAgentsUseCase(
    private val agentRepository: AgentRepository
) {
    fun execute(): List<TrackingAgentDto> =
        agentRepository.findAllWithLocation().map { agent ->
            TrackingAgentDto(
                id = agent.id,
                fullName = "${agent.firstName} ${agent.lastName}",
                agentCode = agent.agentCode,
                lat = agent.latitude!!,
                lng = agent.longitude!!,
                lastSeen = agent.lastSeen,
                status = agent.status
            )
        }
}

@Service
class GetActiveDispatchesUseCase(
    private val dispatchRepository: DriverDispatchRepository
) {
    fun execute(): List<ActiveDispatchDto> =
        dispatchRepository.findActive().map { d ->
            ActiveDispatchDto(
                id = d.id,
                vehicleId = d.vehicleId,
                driverName = d.driverName,
                plateNumber = d.plateNumber,
                vehicleType = d.vehicleType,
                pickupLocation = d.pickupLocation,
                status = d.status,
                scheduledDate = d.scheduledDate
            )
        }
}
