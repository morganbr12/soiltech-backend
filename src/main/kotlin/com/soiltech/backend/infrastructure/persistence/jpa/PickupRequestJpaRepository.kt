package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.domain.enum.LogisticsStatus
import com.soiltech.backend.infrastructure.persistence.entity.PickupRequestJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PickupRequestJpaRepository : JpaRepository<PickupRequestJpaEntity, UUID> {

    @Query("""
        SELECT p FROM PickupRequestJpaEntity p
        WHERE p.agentId = :agentId
          AND (:farmerId IS NULL OR p.farmerId = :farmerId)
          AND (:status IS NULL OR p.status = :status)
        ORDER BY p.scheduledDate DESC
    """)
    fun findAllFiltered(
        @Param("agentId") agentId: UUID,
        @Param("farmerId") farmerId: UUID?,
        @Param("status") status: LogisticsStatus?,
        pageable: Pageable
    ): Page<PickupRequestJpaEntity>

    @Query("SELECT COUNT(p) FROM PickupRequestJpaEntity p WHERE p.agentId = :agentId AND p.status IN :statuses")
    fun countByAgentAndStatusIn(
        @Param("agentId") agentId: UUID,
        @Param("statuses") statuses: List<LogisticsStatus>
    ): Long

    @Query("SELECT p FROM PickupRequestJpaEntity p WHERE p.agentId = :agentId ORDER BY p.createdAt DESC")
    fun findRecentByAgent(@Param("agentId") agentId: UUID, pageable: Pageable): List<PickupRequestJpaEntity>
}
