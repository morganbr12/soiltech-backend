package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.Farm
import com.soiltech.backend.domain.repository.FarmRepository
import com.soiltech.backend.infrastructure.persistence.entity.FarmJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.FarmJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class FarmRepositoryAdapter(
    private val jpaRepository: FarmJpaRepository
) : FarmRepository {

    override fun findById(id: UUID): Farm? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByFarmerId(farmerId: UUID, pageable: Pageable): Page<Farm> =
        jpaRepository.findByFarmerId(farmerId, pageable).map { it.toDomain() }

    override fun save(farm: Farm): Farm =
        jpaRepository.save(FarmJpaEntity.fromDomain(farm)).toDomain()

    override fun update(farm: Farm): Farm {
        val entity = jpaRepository.findById(farm.id).orElseThrow()
        entity.apply {
            name = farm.name
            sizeHectares = farm.sizeHectares
            cropType = farm.cropType
            location = farm.location
            latitude = farm.latitude
            longitude = farm.longitude
            estimatedYieldKg = farm.estimatedYieldKg
            lastHarvestDate = farm.lastHarvestDate
            photosRaw = farm.photos.joinToString(",").takeIf { it.isNotEmpty() }
        }
        return jpaRepository.save(entity).toDomain()
    }

    override fun delete(id: UUID) = jpaRepository.deleteById(id)

    override fun countAll(): Long = jpaRepository.count()

    override fun countCreatedBetween(from: LocalDateTime, to: LocalDateTime): Long =
        jpaRepository.countByCreatedAtBetween(from, to)

    override fun findAllAdmin(region: String?, cropType: String?, search: String?, pageable: Pageable): Page<Farm> =
        jpaRepository.findAllAdmin(region, cropType, search, pageable).map { it.toDomain() }
}
