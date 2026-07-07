package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.OrderTimelineJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderTimelineJpaRepository : JpaRepository<OrderTimelineJpaEntity, UUID> {
    fun findByOrderIdOrderByCreatedAtAsc(orderId: UUID): List<OrderTimelineJpaEntity>
}
