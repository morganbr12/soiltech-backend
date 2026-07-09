package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.Agent
import com.soiltech.backend.domain.entity.AgentMetrics
import com.soiltech.backend.domain.enum.AgentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.util.UUID

interface AgentRepository {
    fun findById(id: UUID): Agent?
    fun findAll(status: AgentStatus?, region: String?, search: String?, pageable: Pageable): Page<Agent>
    fun countByStatus(): Map<AgentStatus, Long>
    fun findMetricsByAgentIds(ids: List<UUID>): Map<UUID, AgentMetrics>
    fun save(agent: Agent): Agent
    fun delete(id: UUID)
    fun existsByEmail(email: String): Boolean
    fun existsByPhone(phone: String): Boolean
    fun existsByEmailAndIdNot(email: String, id: UUID): Boolean
    fun existsByPhoneAndIdNot(phone: String, id: UUID): Boolean
    fun existsByAgentCode(code: String): Boolean
    fun findByAgentCode(agentCode: String): Agent?
    fun countCreatedBetween(from: LocalDateTime, to: LocalDateTime): Long
}
