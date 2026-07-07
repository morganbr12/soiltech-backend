package com.soiltech.backend.domain.event

import java.math.BigDecimal
import java.util.UUID

class ProduceCollectedEvent(
    val produceRecordId: UUID,
    val farmerId: UUID,
    val agentId: UUID,
    val quantityKg: BigDecimal,
    val totalAmount: BigDecimal
) : DomainEvent()
