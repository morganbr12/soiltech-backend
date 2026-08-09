package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.VehicleLocationJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VehicleLocationJpaRepository : JpaRepository<VehicleLocationJpaEntity, UUID> {
    fun findByVehicleId(vehicleId: UUID): VehicleLocationJpaEntity?
}
