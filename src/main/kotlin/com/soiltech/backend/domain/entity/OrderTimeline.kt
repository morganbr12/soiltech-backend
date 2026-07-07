package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.OrderStatus
import java.time.LocalDateTime
import java.util.UUID

data class OrderTimeline(
    val id: UUID,
    val orderId: UUID,
    val status: OrderStatus,
    val note: String?,
    val createdAt: LocalDateTime,
    val createdBy: UUID?
)
