package com.soiltech.backend.application.usecase.logistics

import com.soiltech.backend.application.dto.logistics.CreateVehicleRequest
import com.soiltech.backend.application.dto.logistics.UpdateVehicleRequest
import com.soiltech.backend.application.dto.logistics.VehicleDto
import com.soiltech.backend.application.dto.logistics.VehicleKpisDto
import com.soiltech.backend.domain.entity.Vehicle
import com.soiltech.backend.domain.enum.VehicleStatus
import com.soiltech.backend.domain.repository.VehicleRepository
import com.soiltech.backend.interfaces.exception.ConflictException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

fun Vehicle.toDto() = VehicleDto(
    id = id,
    carPlateNumber = carPlateNumber,
    vehicleType = vehicleType,
    make = make,
    model = model,
    year = year,
    capacity = capacity,
    fuelLevel = fuelLevel,
    region = region,
    driverName = driverName,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)

@Service
class CreateVehicleUseCase(private val vehicleRepository: VehicleRepository) {
    fun execute(request: CreateVehicleRequest): VehicleDto {
        if (vehicleRepository.findByCarPlateNumber(request.carPlateNumber) != null) {
            throw ConflictException("Vehicle with plate number '${request.carPlateNumber}' already exists")
        }
        val vehicle = Vehicle(
            id = UUID.randomUUID(),
            carPlateNumber = request.carPlateNumber.uppercase().trim(),
            vehicleType = request.vehicleType,
            make = request.make,
            model = request.model,
            year = request.year,
            capacity = request.capacity,
            fuelLevel = request.fuelLevel,
            region = request.region,
            driverName = request.driverName,
            status = request.status,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            createdBy = null,
            updatedBy = null
        )
        return vehicleRepository.save(vehicle).toDto()
    }
}

@Service
class ListVehiclesUseCase(private val vehicleRepository: VehicleRepository) {
    fun execute(
        status: VehicleStatus?,
        region: String?,
        vehicleType: String?,
        search: String?,
        page: Int,
        perPage: Int
    ): Pair<List<VehicleDto>, PaginationMeta> {
        val pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending())
        val result = vehicleRepository.findAll(status, region, vehicleType, search, pageable)
        return result.content.map { it.toDto() } to PaginationMeta.from(result, page, perPage)
    }
}

@Service
class GetVehicleUseCase(private val vehicleRepository: VehicleRepository) {
    fun execute(id: UUID): VehicleDto =
        vehicleRepository.findById(id)?.toDto()
            ?: throw NotFoundException("Vehicle not found")
}

@Service
class UpdateVehicleUseCase(private val vehicleRepository: VehicleRepository) {
    fun execute(id: UUID, request: UpdateVehicleRequest): VehicleDto {
        val existing = vehicleRepository.findById(id)
            ?: throw NotFoundException("Vehicle not found")
        val updated = existing.copy(
            vehicleType = request.vehicleType ?: existing.vehicleType,
            make = request.make ?: existing.make,
            model = request.model ?: existing.model,
            year = request.year ?: existing.year,
            capacity = request.capacity ?: existing.capacity,
            fuelLevel = request.fuelLevel ?: existing.fuelLevel,
            region = request.region ?: existing.region,
            driverName = request.driverName ?: existing.driverName,
            status = request.status ?: existing.status,
            updatedAt = LocalDateTime.now()
        )
        return vehicleRepository.save(updated).toDto()
    }
}

@Service
class DeleteVehicleUseCase(private val vehicleRepository: VehicleRepository) {
    fun execute(id: UUID) = vehicleRepository.delete(id)
}

@Service
class GetVehicleKpisUseCase(private val vehicleRepository: VehicleRepository) {
    fun execute(): VehicleKpisDto = VehicleKpisDto(
        totalVehicles = vehicleRepository.countAll(),
        available = vehicleRepository.countByStatus(VehicleStatus.AVAILABLE),
        onRoute = vehicleRepository.countByStatus(VehicleStatus.ON_ROUTE),
        maintenance = vehicleRepository.countByStatus(VehicleStatus.MAINTENANCE),
        inactive = vehicleRepository.countByStatus(VehicleStatus.INACTIVE)
    )
}
