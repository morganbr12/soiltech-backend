package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.domain.enum.DispatchStatus
import com.soiltech.backend.infrastructure.persistence.entity.DriverDispatchJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DriverDispatchJpaRepository : JpaRepository<DriverDispatchJpaEntity, UUID> {

    fun findByOrderId(orderId: UUID): DriverDispatchJpaEntity?

    @Query("""
        SELECT d FROM DriverDispatchJpaEntity d
        WHERE (:status IS NULL OR d.status = :status)
        ORDER BY d.createdAt DESC
    """)
    fun findAllFiltered(
        @Param("status") status: DispatchStatus?,
        pageable: Pageable
    ): Page<DriverDispatchJpaEntity>
}
