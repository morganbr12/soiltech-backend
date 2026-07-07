package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.ProduceRecord
import com.soiltech.backend.domain.enum.CollectionStatus
import com.soiltech.backend.domain.repository.ProduceRecordRepository
import com.soiltech.backend.infrastructure.persistence.entity.ProduceRecordJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.ProduceRecordJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ProduceRecordRepositoryAdapter(
    private val jpaRepository: ProduceRecordJpaRepository
) : ProduceRecordRepository {

    override fun findById(id: UUID): ProduceRecord? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findAll(agentId: UUID, farmerId: UUID?, status: CollectionStatus?, pageable: Pageable): Page<ProduceRecord> =
        jpaRepository.findAllFiltered(agentId, farmerId, status, pageable).map { it.toDomain() }

    override fun save(record: ProduceRecord): ProduceRecord =
        jpaRepository.save(ProduceRecordJpaEntity.fromDomain(record)).toDomain()

    override fun update(record: ProduceRecord): ProduceRecord {
        val entity = jpaRepository.findById(record.id).orElseThrow()
        entity.apply {
            status = record.status
            collectedAt = record.collectedAt
            notes = record.notes
            syncStatus = record.syncStatus
            quantityKg = record.quantityKg
            pricePerKg = record.pricePerKg
            totalAmount = record.totalAmount
        }
        return jpaRepository.save(entity).toDomain()
    }
}
