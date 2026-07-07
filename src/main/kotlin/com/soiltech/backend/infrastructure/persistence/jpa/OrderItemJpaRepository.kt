package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.OrderItemJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderItemJpaRepository : JpaRepository<OrderItemJpaEntity, UUID> {
    fun findByOrderId(orderId: UUID): List<OrderItemJpaEntity>
}
