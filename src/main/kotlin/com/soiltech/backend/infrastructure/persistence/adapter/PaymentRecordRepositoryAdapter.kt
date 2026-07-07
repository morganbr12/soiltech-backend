package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.PaymentRecord
import com.soiltech.backend.domain.enum.PaymentStatus
import com.soiltech.backend.domain.repository.PaymentRecordRepository
import com.soiltech.backend.infrastructure.persistence.entity.PaymentRecordJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.PaymentRecordJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PaymentRecordRepositoryAdapter(
    private val jpaRepository: PaymentRecordJpaRepository
) : PaymentRecordRepository {

    override fun findById(id: UUID): PaymentRecord? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findAll(agentId: UUID, farmerId: UUID?, status: PaymentStatus?, pageable: Pageable): Page<PaymentRecord> =
        jpaRepository.findAllFiltered(agentId, farmerId, status, pageable).map { it.toDomain() }

    override fun save(record: PaymentRecord): PaymentRecord =
        jpaRepository.save(PaymentRecordJpaEntity.fromDomain(record)).toDomain()
}
