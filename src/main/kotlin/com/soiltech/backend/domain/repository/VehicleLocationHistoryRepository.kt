package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.VehicleLocationHistory
import java.time.LocalDateTime
import java.util.UUID

interface VehicleLocationHistoryRepository {
    fun save(entry: VehicleLocationHistory): VehicleLocationHistory
    fun findByVehicleIdBetween(vehicleId: UUID, from: LocalDateTime, to: LocalDateTime): List<VehicleLocationHistory>
}
