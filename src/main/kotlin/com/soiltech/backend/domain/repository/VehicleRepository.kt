package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.Vehicle
import com.soiltech.backend.domain.enum.VehicleStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface VehicleRepository {
    fun save(vehicle: Vehicle): Vehicle
    fun findById(id: UUID): Vehicle?
    fun findByCarPlateNumber(plateNumber: String): Vehicle?
    fun findAll(status: VehicleStatus?, region: String?, vehicleType: String?, search: String?, pageable: Pageable): Page<Vehicle>
    fun delete(id: UUID)
    fun countByStatus(status: VehicleStatus): Long
    fun countAll(): Long
}
