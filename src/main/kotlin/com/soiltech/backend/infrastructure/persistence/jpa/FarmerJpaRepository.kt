package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.FarmerJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.UUID

@Repository
interface FarmerJpaRepository : JpaRepository<FarmerJpaEntity, UUID>, JpaSpecificationExecutor<FarmerJpaEntity> {

    fun existsByPhone(phone: String): Boolean
    fun existsByPhoneAndIdNot(phone: String, id: UUID): Boolean
    fun existsByNationalId(nationalId: String): Boolean
    fun existsByNationalIdAndIdNot(nationalId: String, id: UUID): Boolean
    fun existsByFarmerCode(code: String): Boolean

    @Query("SELECT COUNT(f) FROM FarmerJpaEntity f")
    fun countAll(): Long

    @Query("SELECT f.farmerCode FROM FarmerJpaEntity f WHERE f.farmerCode LIKE 'FMR-%' ORDER BY f.farmerCode DESC")
    fun findLastFarmerCode(pageable: Pageable): List<String>

    @Query("SELECT f.status, COUNT(f) FROM FarmerJpaEntity f GROUP BY f.status")
    fun countGroupByStatus(): List<Array<Any>>

    // Farm metrics — count + total size per farmer
    @Query("""
        SELECT fm.farmerId, COUNT(fm), COALESCE(SUM(fm.sizeHectares), 0)
        FROM FarmJpaEntity fm
        WHERE fm.farmerId IN :ids
        GROUP BY fm.farmerId
    """)
    fun findFarmStatsByFarmerIds(@Param("ids") ids: List<UUID>): List<Array<Any>>

    // Crop types per farmer (one row per farm)
    @Query("""
        SELECT fm.farmerId, fm.cropType
        FROM FarmJpaEntity fm
        WHERE fm.farmerId IN :ids AND fm.cropType IS NOT NULL
    """)
    fun findCropTypesByFarmerIds(@Param("ids") ids: List<UUID>): List<Array<Any>>

    // Total earnings from produce records
    @Query("""
        SELECT pr.farmerId, COALESCE(SUM(pr.totalAmount), 0)
        FROM ProduceRecordJpaEntity pr
        WHERE pr.farmerId IN :ids
        GROUP BY pr.farmerId
    """)
    fun sumEarningsByFarmerIds(@Param("ids") ids: List<UUID>): List<Array<Any>>

    @Query("SELECT COUNT(f) FROM FarmerJpaEntity f WHERE f.agentId = :agentId")
    fun countByAgentId(@Param("agentId") agentId: UUID): Long

    @Query("SELECT COUNT(f) FROM FarmerJpaEntity f WHERE f.agentId = :agentId AND f.status = :status")
    fun countByAgentIdAndStatus(
        @Param("agentId") agentId: UUID,
        @Param("status") status: com.soiltech.backend.domain.enum.FarmerStatus
    ): Long

    @Query("SELECT f FROM FarmerJpaEntity f WHERE f.agentId = :agentId ORDER BY f.createdAt DESC")
    fun findRecentByAgentId(@Param("agentId") agentId: UUID, pageable: Pageable): List<FarmerJpaEntity>

    // ── Admin dashboard ──────────────────────────────────────────────────────

    fun findTop5ByOrderByCreatedAtDesc(): List<FarmerJpaEntity>

    fun countByCreatedAtBetween(from: java.time.LocalDateTime, to: java.time.LocalDateTime): Long

    @Query(
        value = """
            SELECT f.region AS region,
                   COUNT(DISTINCT f.id) AS farmers,
                   COALESCE(SUM(pl.total_quantity_kg) / 1000.0, 0) AS produce,
                   COALESCE(SUM(pl.total_quantity_kg * pl.price_per_kg), 0) AS revenue
            FROM farmers f
            LEFT JOIN produce_listings pl ON f.region = pl.region
            WHERE f.region IS NOT NULL
            GROUP BY f.region
            ORDER BY revenue DESC
        """,
        nativeQuery = true
    )
    fun findRegionalOverview(): List<RegionalOverviewProjection>
}

interface RegionalOverviewProjection {
    fun getRegion(): String
    fun getFarmers(): Long
    fun getProduce(): BigDecimal
    fun getRevenue(): BigDecimal
}
