package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.KycDocument
import com.soiltech.backend.domain.repository.KycDocumentRepository
import com.soiltech.backend.infrastructure.persistence.entity.KycDocumentJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.KycDocumentJpaRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class KycDocumentRepositoryAdapter(
    private val jpa: KycDocumentJpaRepository
) : KycDocumentRepository {

    override fun findByCustomerId(customerId: UUID): List<KycDocument> =
        jpa.findByCustomerId(customerId).map { it.toDomain() }

    override fun save(document: KycDocument): KycDocument =
        jpa.save(KycDocumentJpaEntity.fromDomain(document)).toDomain()
}
