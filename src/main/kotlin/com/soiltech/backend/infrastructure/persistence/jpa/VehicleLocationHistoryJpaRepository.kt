package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.VehicleLocationHistoryJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface VehicleLocationHistoryJpaRepository : JpaRepository<VehicleLocationHistoryJpaEntity, UUID> {

    @Query("""
        SELECT h FROM VehicleLocationHistoryJpaEntity h
        WHERE h.vehicleId = :vehicleId
          AND h.recordedAt BETWEEN :from AND :to
        ORDER BY h.recordedAt ASC
    """)
    fun findByVehicleIdBetween(
        @Param("vehicleId") vehicleId: UUID,
        @Param("from") from: LocalDateTime,
        @Param("to") to: LocalDateTime
    ): List<VehicleLocationHistoryJpaEntity>
}
