package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.PickupRequest
import com.soiltech.backend.domain.enum.LogisticsStatus
import com.soiltech.backend.domain.repository.PickupRequestRepository
import com.soiltech.backend.infrastructure.persistence.entity.PickupRequestJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.PickupRequestJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PickupRequestRepositoryAdapter(
    private val jpaRepository: PickupRequestJpaRepository
) : PickupRequestRepository {

    override fun findById(id: UUID): PickupRequest? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findAll(agentId: UUID, farmerId: UUID?, status: LogisticsStatus?, pageable: Pageable): Page<PickupRequest> =
        jpaRepository.findAllFiltered(agentId, farmerId, status, pageable).map { it.toDomain() }

    override fun save(request: PickupRequest): PickupRequest =
        jpaRepository.save(PickupRequestJpaEntity.fromDomain(request)).toDomain()

    override fun update(request: PickupRequest): PickupRequest {
        val entity = jpaRepository.findById(request.id).orElseThrow()
        entity.apply {
            scheduledDate = request.scheduledDate
            status = request.status
            notes = request.notes
        }
        return jpaRepository.save(entity).toDomain()
    }
}
