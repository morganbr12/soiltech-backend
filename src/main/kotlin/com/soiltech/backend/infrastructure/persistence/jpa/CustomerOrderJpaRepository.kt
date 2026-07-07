package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.domain.enum.OrderStatus
import com.soiltech.backend.infrastructure.persistence.entity.CustomerOrderJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CustomerOrderJpaRepository : JpaRepository<CustomerOrderJpaEntity, UUID> {

    @Query("""
        SELECT o FROM CustomerOrderJpaEntity o
        WHERE o.customerId = :customerId
          AND (:status IS NULL OR o.status = :status)
        ORDER BY o.createdAt DESC
    """)
    fun findAllFiltered(
        @Param("customerId") customerId: UUID,
        @Param("status") status: OrderStatus?,
        pageable: Pageable
    ): Page<CustomerOrderJpaEntity>
}
