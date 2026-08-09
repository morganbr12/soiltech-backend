package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.VehicleLocation
import java.util.UUID

interface VehicleLocationRepository {
    fun upsert(location: VehicleLocation): VehicleLocation
    fun findByVehicleId(vehicleId: UUID): VehicleLocation?
    fun findAll(): List<VehicleLocation>
}
