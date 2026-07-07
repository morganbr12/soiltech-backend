package com.soiltech.backend.domain.event

import java.time.LocalDateTime

abstract class DomainEvent(
    val occurredAt: LocalDateTime = LocalDateTime.now()
)
