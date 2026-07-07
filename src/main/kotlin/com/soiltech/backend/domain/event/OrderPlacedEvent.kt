package com.soiltech.backend.domain.event

import java.math.BigDecimal
import java.util.UUID

class OrderPlacedEvent(
    val orderId: UUID,
    val customerId: UUID,
    val totalAmount: BigDecimal
) : DomainEvent()
