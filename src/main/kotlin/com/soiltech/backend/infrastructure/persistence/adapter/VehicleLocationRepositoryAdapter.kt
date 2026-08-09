package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.VehicleLocation
import com.soiltech.backend.domain.repository.VehicleLocationRepository
import com.soiltech.backend.infrastructure.persistence.entity.VehicleLocationJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.VehicleLocationJpaRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class VehicleLocationRepositoryAdapter(
    private val jpaRepository: VehicleLocationJpaRepository
) : VehicleLocationRepository {

    override fun upsert(location: VehicleLocation): VehicleLocation {
        val existing = jpaRepository.findByVehicleId(location.vehicleId)
        return if (existing != null) {
            existing.lat = location.lat
            existing.lng = location.lng
            existing.speed = location.speed
            existing.heading = location.heading
            existing.lastUpdated = location.lastUpdated
            jpaRepository.save(existing).toDomain()
        } else {
            jpaRepository.save(VehicleLocationJpaEntity.fromDomain(location)).toDomain()
        }
    }

    override fun findByVehicleId(vehicleId: UUID): VehicleLocation? =
        jpaRepository.findByVehicleId(vehicleId)?.toDomain()

    override fun findAll(): List<VehicleLocation> =
        jpaRepository.findAll().map { it.toDomain() }
}
