package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.domain.enum.FarmerStatus
import com.soiltech.backend.infrastructure.persistence.entity.FarmerJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FarmerJpaRepository : JpaRepository<FarmerJpaEntity, UUID> {

    fun findByAgentId(agentId: UUID, pageable: Pageable): Page<FarmerJpaEntity>

    @Query("""
        SELECT f FROM FarmerJpaEntity f
        WHERE f.agentId = :agentId
          AND (:status IS NULL OR f.status = :status)
          AND (:query IS NULL OR LOWER(f.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
               OR f.phone LIKE CONCAT('%', :query, '%'))
        ORDER BY f.createdAt DESC
    """)
    fun findAllFiltered(
        @Param("agentId") agentId: UUID,
        @Param("status") status: FarmerStatus?,
        @Param("query") query: String?,
        pageable: Pageable
    ): Page<FarmerJpaEntity>
}
