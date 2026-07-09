package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.domain.enum.PaymentStatus
import com.soiltech.backend.infrastructure.persistence.entity.PaymentRecordJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface PaymentRecordJpaRepository : JpaRepository<PaymentRecordJpaEntity, UUID> {

    @Query("""
        SELECT p FROM PaymentRecordJpaEntity p
        WHERE p.agentId = :agentId
          AND (:farmerId IS NULL OR p.farmerId = :farmerId)
          AND (:status IS NULL OR p.status = :status)
        ORDER BY p.createdAt DESC
    """)
    fun findAllFiltered(
        @Param("agentId") agentId: UUID,
        @Param("farmerId") farmerId: UUID?,
        @Param("status") status: PaymentStatus?,
        pageable: Pageable
    ): Page<PaymentRecordJpaEntity>

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM PaymentRecordJpaEntity p
        WHERE p.agentId = :agentId
          AND p.status = :status
          AND p.paidAt >= :monthStart
          AND p.paidAt < :monthEnd
    """)
    fun sumMonthlyRevenueByAgent(
        @Param("agentId") agentId: UUID,
        @Param("status") status: PaymentStatus,
        @Param("monthStart") monthStart: LocalDateTime,
        @Param("monthEnd") monthEnd: LocalDateTime
    ): BigDecimal

    @Query("SELECT p FROM PaymentRecordJpaEntity p WHERE p.agentId = :agentId ORDER BY p.createdAt DESC")
    fun findRecentByAgent(@Param("agentId") agentId: UUID, pageable: Pageable): List<PaymentRecordJpaEntity>

    @Query("SELECT COUNT(p) FROM PaymentRecordJpaEntity p WHERE p.agentId = :agentId AND p.createdAt >= :since")
    fun countRecentByAgent(
        @Param("agentId") agentId: UUID,
        @Param("since") since: LocalDateTime
    ): Long

    // ── Admin dashboard ──────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentRecordJpaEntity p WHERE p.status = :status")
    fun sumAmountByStatus(@Param("status") status: PaymentStatus): BigDecimal
}
