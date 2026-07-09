package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.domain.enum.VehicleStatus
import com.soiltech.backend.infrastructure.persistence.entity.VehicleJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VehicleJpaRepository : JpaRepository<VehicleJpaEntity, UUID> {

    fun findByCarPlateNumber(carPlateNumber: String): VehicleJpaEntity?

    fun countByStatus(status: VehicleStatus): Long

    @Query("""
        SELECT v FROM VehicleJpaEntity v
        WHERE (:status IS NULL OR v.status = :status)
          AND (cast(:region as String) IS NULL OR LOWER(v.region) = LOWER(cast(:region as String)))
          AND (cast(:vehicleType as String) IS NULL OR LOWER(v.vehicleType) = LOWER(cast(:vehicleType as String)))
          AND (cast(:search as String) IS NULL
               OR LOWER(v.carPlateNumber) LIKE LOWER(CONCAT('%', cast(:search as String), '%'))
               OR LOWER(v.vehicleType) LIKE LOWER(CONCAT('%', cast(:search as String), '%'))
               OR LOWER(v.driverName) LIKE LOWER(CONCAT('%', cast(:search as String), '%'))
               OR LOWER(v.make) LIKE LOWER(CONCAT('%', cast(:search as String), '%'))
               OR LOWER(v.model) LIKE LOWER(CONCAT('%', cast(:search as String), '%')))
        ORDER BY v.createdAt DESC
    """)
    fun findAllFiltered(
        @Param("status") status: VehicleStatus?,
        @Param("region") region: String?,
        @Param("vehicleType") vehicleType: String?,
        @Param("search") search: String?,
        pageable: Pageable
    ): Page<VehicleJpaEntity>
}
