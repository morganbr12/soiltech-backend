package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.Vehicle
import com.soiltech.backend.domain.enum.VehicleStatus
import com.soiltech.backend.domain.repository.VehicleRepository
import com.soiltech.backend.infrastructure.persistence.entity.VehicleJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.VehicleJpaRepository
import com.soiltech.backend.interfaces.exception.NotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class VehicleRepositoryAdapter(
    private val jpaRepository: VehicleJpaRepository
) : VehicleRepository {

    override fun save(vehicle: Vehicle): Vehicle =
        jpaRepository.save(VehicleJpaEntity.fromDomain(vehicle)).toDomain()

    override fun findById(id: UUID): Vehicle? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByCarPlateNumber(plateNumber: String): Vehicle? =
        jpaRepository.findByCarPlateNumber(plateNumber)?.toDomain()

    override fun findAll(
        status: VehicleStatus?,
        region: String?,
        vehicleType: String?,
        search: String?,
        pageable: Pageable
    ): Page<Vehicle> = jpaRepository.findAllFiltered(status, region, vehicleType, search, pageable)
        .map { it.toDomain() }

    override fun delete(id: UUID) {
        if (!jpaRepository.existsById(id)) throw NotFoundException("Vehicle not found")
        jpaRepository.deleteById(id)
    }

    override fun countByStatus(status: VehicleStatus): Long =
        jpaRepository.countByStatus(status)

    override fun countAll(): Long =
        jpaRepository.count()
}
