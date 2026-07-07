package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.Farmer
import com.soiltech.backend.domain.enum.FarmerStatus
import com.soiltech.backend.domain.repository.FarmerRepository
import com.soiltech.backend.infrastructure.persistence.entity.FarmerJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.FarmerJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class FarmerRepositoryAdapter(
    private val jpaRepository: FarmerJpaRepository
) : FarmerRepository {

    override fun findById(id: UUID): Farmer? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByAgentId(agentId: UUID, pageable: Pageable): Page<Farmer> =
        jpaRepository.findByAgentId(agentId, pageable).map { it.toDomain() }

    override fun findAll(agentId: UUID, status: FarmerStatus?, query: String?, pageable: Pageable): Page<Farmer> =
        jpaRepository.findAllFiltered(agentId, status, query, pageable).map { it.toDomain() }

    override fun save(farmer: Farmer): Farmer =
        jpaRepository.save(FarmerJpaEntity.fromDomain(farmer)).toDomain()

    override fun update(farmer: Farmer): Farmer {
        val entity = jpaRepository.findById(farmer.id).orElseThrow()
        entity.apply {
            fullName = farmer.fullName
            phone = farmer.phone
            nationalId = farmer.nationalId
            location = farmer.location
            status = farmer.status
            syncStatus = farmer.syncStatus
        }
        return jpaRepository.save(entity).toDomain()
    }

    override fun delete(id: UUID) = jpaRepository.deleteById(id)

    override fun existsById(id: UUID): Boolean = jpaRepository.existsById(id)
}
