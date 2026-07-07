package com.soiltech.backend.domain.entity

import java.time.LocalDateTime
import java.util.UUID

data class AgentProfile(
    val id: UUID,
    val userId: UUID,
    val fullName: String,
    val agentCode: String,
    val region: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
