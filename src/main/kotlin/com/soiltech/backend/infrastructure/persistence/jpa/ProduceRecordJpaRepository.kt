package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.domain.enum.CollectionStatus
import com.soiltech.backend.domain.enum.SyncStatus
import com.soiltech.backend.infrastructure.persistence.entity.ProduceRecordJpaEntity
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

    // ── Dashboard aggregates ──────────────────────────────────────────────────

    @Query("SELECT COUNT(p) FROM ProduceRecordJpaEntity p WHERE p.agentId = :agentId AND p.createdAt >= :dayStart AND p.createdAt < :dayEnd")
    fun countTodayByAgent(
        @Param("agentId") agentId: UUID,
        @Param("dayStart") dayStart: LocalDateTime,
        @Param("dayEnd") dayEnd: LocalDateTime
    ): Long

    @Query("SELECT COUNT(DISTINCT p.farmerId) FROM ProduceRecordJpaEntity p WHERE p.agentId = :agentId AND p.createdAt >= :dayStart AND p.createdAt < :dayEnd")
    fun countDistinctFarmersTodayByAgent(
        @Param("agentId") agentId: UUID,
        @Param("dayStart") dayStart: LocalDateTime,
        @Param("dayEnd") dayEnd: LocalDateTime
    ): Long

    @Query("SELECT COUNT(p) FROM ProduceRecordJpaEntity p WHERE p.agentId = :agentId AND p.syncStatus = :status")
    fun countBySyncStatus(
        @Param("agentId") agentId: UUID,
        @Param("status") status: SyncStatus
    ): Long

    @Query("SELECT COALESCE(SUM(p.quantityKg), 0) FROM ProduceRecordJpaEntity p WHERE p.agentId = :agentId AND p.createdAt >= :dayStart AND p.createdAt < :dayEnd")
    fun sumWeightTodayByAgent(
        @Param("agentId") agentId: UUID,
        @Param("dayStart") dayStart: LocalDateTime,
        @Param("dayEnd") dayEnd: LocalDateTime
    ): BigDecimal

    @Query("SELECT COALESCE(SUM(p.quantityKg), 0) FROM ProduceRecordJpaEntity p WHERE p.agentId = :agentId AND p.createdAt >= :weekStart AND p.createdAt < :weekEnd")
    fun sumWeightWeekByAgent(
        @Param("agentId") agentId: UUID,
        @Param("weekStart") weekStart: LocalDateTime,
        @Param("weekEnd") weekEnd: LocalDateTime
    ): BigDecimal

    @Query("""
        SELECT EXTRACT(DAY_OF_WEEK FROM p.createdAt), COALESCE(SUM(p.quantityKg), 0)
        FROM ProduceRecordJpaEntity p
        WHERE p.agentId = :agentId AND p.createdAt >= :weekStart AND p.createdAt < :weekEnd
        GROUP BY EXTRACT(DAY_OF_WEEK FROM p.createdAt)
    """)
    fun sumWeightByDayOfWeekForWeek(
        @Param("agentId") agentId: UUID,
        @Param("weekStart") weekStart: LocalDateTime,
        @Param("weekEnd") weekEnd: LocalDateTime
    ): List<Array<Any>>

    @Query("SELECT COUNT(p) FROM ProduceRecordJpaEntity p WHERE p.agentId = :agentId")
    fun countAllByAgent(@Param("agentId") agentId: UUID): Long

    @Query("SELECT COUNT(p) FROM ProduceRecordJpaEntity p WHERE p.agentId = :agentId AND p.status = :status")
    fun countByAgentAndStatus(
        @Param("agentId") agentId: UUID,
        @Param("status") status: CollectionStatus
    ): Long

    @Query("SELECT COALESCE(SUM(p.quantityKg), 0) FROM ProduceRecordJpaEntity p WHERE p.agentId = :agentId")
    fun sumTotalWeightByAgent(@Param("agentId") agentId: UUID): BigDecimal

    @Query("SELECT p FROM ProduceRecordJpaEntity p WHERE p.agentId = :agentId ORDER BY p.createdAt DESC")
    fun findRecentByAgent(@Param("agentId") agentId: UUID, pageable: Pageable): List<ProduceRecordJpaEntity>
}
