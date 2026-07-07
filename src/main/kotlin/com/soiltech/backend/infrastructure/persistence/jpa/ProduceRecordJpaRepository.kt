package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.domain.enum.CollectionStatus
import com.soiltech.backend.infrastructure.persistence.entity.ProduceRecordJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProduceRecordJpaRepository : JpaRepository<ProduceRecordJpaEntity, UUID> {

    @Query("""
        SELECT p FROM ProduceRecordJpaEntity p
        WHERE p.agentId = :agentId
          AND (:farmerId IS NULL OR p.farmerId = :farmerId)
          AND (:status IS NULL OR p.status = :status)
        ORDER BY p.createdAt DESC
    """)
    fun findAllFiltered(
        @Param("agentId") agentId: UUID,
        @Param("farmerId") farmerId: UUID?,
        @Param("status") status: CollectionStatus?,
        pageable: Pageable
    ): Page<ProduceRecordJpaEntity>
}
