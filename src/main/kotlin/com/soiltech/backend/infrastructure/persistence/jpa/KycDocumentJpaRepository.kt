package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.KycDocumentJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface KycDocumentJpaRepository : JpaRepository<KycDocumentJpaEntity, UUID> {
    fun findByCustomerId(customerId: UUID): List<KycDocumentJpaEntity>
}
