package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.VehicleLocationHistory
import com.soiltech.backend.domain.repository.VehicleLocationHistoryRepository
import com.soiltech.backend.infrastructure.persistence.entity.VehicleLocationHistoryJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.VehicleLocationHistoryJpaRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class VehicleLocationHistoryRepositoryAdapter(
    private val jpaRepository: VehicleLocationHistoryJpaRepository
) : VehicleLocationHistoryRepository {

    override fun save(entry: VehicleLocationHistory): VehicleLocationHistory =
        jpaRepository.save(VehicleLocationHistoryJpaEntity.fromDomain(entry)).toDomain()

    override fun findByVehicleIdBetween(
        vehicleId: UUID,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<VehicleLocationHistory> =
        jpaRepository.findByVehicleIdBetween(vehicleId, from, to).map { it.toDomain() }
}
