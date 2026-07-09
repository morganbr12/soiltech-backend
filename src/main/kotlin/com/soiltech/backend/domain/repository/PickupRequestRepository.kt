package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.PickupRequest
import com.soiltech.backend.domain.enum.LogisticsStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.util.UUID

interface PickupRequestRepository {
    fun findById(id: UUID): PickupRequest?
    fun findAll(agentId: UUID, farmerId: UUID?, status: LogisticsStatus?, pageable: Pageable): Page<PickupRequest>
    fun save(request: PickupRequest): PickupRequest
    fun update(request: PickupRequest): PickupRequest

    // Dashboard aggregates
    fun countActiveByAgent(agentId: UUID): Long
    fun findRecentByAgent(agentId: UUID, limit: Int): List<PickupRequest>

    // Admin dashboard
    fun countByStatusAll(): Map<LogisticsStatus, Long>
    fun countByStatusSince(status: LogisticsStatus, since: LocalDateTime): Long
    fun countDistinctAgentsByStatus(status: LogisticsStatus): Long
}
