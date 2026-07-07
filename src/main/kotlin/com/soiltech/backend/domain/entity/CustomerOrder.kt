package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.OrderStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class CustomerOrder(
    val id: UUID,
    val customerId: UUID,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val deliveryAddress: String,
    val notes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
