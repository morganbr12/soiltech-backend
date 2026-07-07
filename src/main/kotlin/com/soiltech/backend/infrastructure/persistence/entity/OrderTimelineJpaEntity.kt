package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.OrderTimeline
import com.soiltech.backend.domain.enum.OrderStatus
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "order_timelines",
    indexes = [Index(name = "idx_order_timelines_order_id", columnList = "orderId")]
)
class OrderTimelineJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val orderId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: OrderStatus,

    @Column(length = 1000)
    val note: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    val createdBy: UUID? = null
) {
    fun toDomain(): OrderTimeline = OrderTimeline(
        id = id!!,
        orderId = orderId,
        status = status,
        note = note,
        createdAt = createdAt,
        createdBy = createdBy
    )

    companion object {
        fun fromDomain(timeline: OrderTimeline): OrderTimelineJpaEntity = OrderTimelineJpaEntity(
            id = timeline.id,
            orderId = timeline.orderId,
            status = timeline.status,
            note = timeline.note,
            createdAt = timeline.createdAt,
            createdBy = timeline.createdBy
        )
    }
}
