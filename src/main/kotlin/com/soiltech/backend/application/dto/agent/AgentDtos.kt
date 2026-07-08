package com.soiltech.backend.application.dto.agent

import java.time.LocalDateTime
import java.util.UUID

data class AgentProfileDto(
    val id: UUID,
    val userId: UUID,
    val fullName: String,
    val firstName: String,
    val lastName: String,
    val agentCode: String,
    val region: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
