package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.domain.enum.ProduceListingStatus
import com.soiltech.backend.infrastructure.persistence.entity.ProduceListingJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.UUID

@Repository
interface ProduceListingJpaRepository : JpaRepository<ProduceListingJpaEntity, UUID> {

    fun findByProduceRecordId(produceRecordId: UUID): ProduceListingJpaEntity?

    fun findByProduceRecordIdIn(produceRecordIds: Collection<UUID>): List<ProduceListingJpaEntity>

    fun findTop7ByOrderByCreatedAtDesc(): List<ProduceListingJpaEntity>

    @Query("""
        SELECT p FROM ProduceListingJpaEntity p
        WHERE (:status IS NULL OR p.status = :status)
          AND (cast(:cropType as String) IS NULL OR LOWER(p.cropType) = LOWER(cast(:cropType as String)))
          AND (cast(:region as String) IS NULL OR LOWER(p.region) = LOWER(cast(:region as String)))
          AND (cast(:search as String) IS NULL
               OR LOWER(p.cropType) LIKE LOWER(CONCAT('%', cast(:search as String), '%'))
               OR LOWER(p.agentName) LIKE LOWER(CONCAT('%', cast(:search as String), '%'))
               OR LOWER(p.farmerName) LIKE LOWER(CONCAT('%', cast(:search as String), '%')))
        ORDER BY p.createdAt DESC
    """)
    fun findAllAdmin(
        @Param("status") status: ProduceListingStatus?,
        @Param("cropType") cropType: String?,
        @Param("region") region: String?,
        @Param("search") search: String?,
        pageable: Pageable
    ): Page<ProduceListingJpaEntity>

    @Query(
        value = "SELECT COALESCE(SUM(total_quantity_kg - available_quantity_kg), 0) / NULLIF(SUM(total_quantity_kg), 0) * 100 FROM produce_listings",
        nativeQuery = true
    )
    fun computeFillRatePercent(): Double?

    @Query("""
        SELECT p FROM ProduceListingJpaEntity p
        WHERE p.status = :status
        AND (cast(:cropType as String) IS NULL OR LOWER(p.cropType) = LOWER(cast(:cropType as String)))
        AND (cast(:region as String) IS NULL OR LOWER(p.region) = LOWER(cast(:region as String)))
        AND (cast(:district as String) IS NULL OR LOWER(p.district) = LOWER(cast(:district as String)))
        AND (:lbcId IS NULL OR p.lbcId = :lbcId)
        AND (cast(:grade as String) IS NULL OR LOWER(p.grade) = LOWER(cast(:grade as String)))
        AND (:minPrice IS NULL OR p.pricePerKg >= :minPrice)
        AND (:maxPrice IS NULL OR p.pricePerKg <= :maxPrice)
        AND (:minQuantity IS NULL OR p.availableQuantityKg >= :minQuantity)
        ORDER BY p.createdAt DESC
    """)
    fun findAllFiltered(
        @Param("status") status: ProduceListingStatus,
        @Param("cropType") cropType: String?,
        @Param("region") region: String?,
        @Param("district") district: String?,
        @Param("lbcId") lbcId: UUID?,
        @Param("grade") grade: String?,
        @Param("minPrice") minPrice: BigDecimal?,
        @Param("maxPrice") maxPrice: BigDecimal?,
        @Param("minQuantity") minQuantity: BigDecimal?,
        pageable: Pageable
    ): Page<ProduceListingJpaEntity>
}
