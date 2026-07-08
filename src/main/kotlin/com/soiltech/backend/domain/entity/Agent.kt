package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.AgentStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class Agent(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val agentCode: String,
    val lbcId: UUID,
    val lbcName: String,
    val region: String,
    val district: String,
    val status: AgentStatus,
    val latitude: Double?,
    val longitude: Double?,
    val lastSeen: LocalDateTime?,
    val joinedDate: LocalDateTime,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val createdBy: UUID?,
    val updatedBy: UUID?
)

data class AgentMetrics(
    val farmersCount: Long = 0,
    val farmsCount: Long = 0,
    val produceCollected: BigDecimal = BigDecimal.ZERO
)
