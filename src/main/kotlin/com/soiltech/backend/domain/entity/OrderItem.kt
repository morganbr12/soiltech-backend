package com.soiltech.backend.domain.entity

import java.math.BigDecimal
import java.util.UUID

data class OrderItem(
    val id: UUID,
    val orderId: UUID,
    val productId: UUID,
    val farmerId: UUID?,
    val agentId: UUID?,
    val productName: String?,
    val agentName: String?,
    val region: String?,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal
)
