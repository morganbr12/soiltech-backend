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

    fun findByCustomerIdOrderByCreatedAtDesc(
        customerId: UUID,
        pageable: Pageable
    ): Page<CustomerOrderJpaEntity>

    fun findByCustomerIdAndStatusInOrderByCreatedAtDesc(
        customerId: UUID,
        statuses: List<OrderStatus>,
        pageable: Pageable
    ): Page<CustomerOrderJpaEntity>

    @Query("""
        SELECT o FROM CustomerOrderJpaEntity o
        WHERE (:customerId IS NULL OR o.customerId = :customerId)
          AND (:#{#statuses == null} = true OR o.status IN :statuses)
        ORDER BY o.createdAt DESC
    """)
    fun findAllFiltered(
        @Param("customerId") customerId: UUID?,
        @Param("statuses") statuses: List<OrderStatus>?,
        pageable: Pageable
    ): Page<CustomerOrderJpaEntity>
}
