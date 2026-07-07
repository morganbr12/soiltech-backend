package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.AgentProfileJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AgentProfileJpaRepository : JpaRepository<AgentProfileJpaEntity, UUID> {
    fun findByUserId(userId: UUID): AgentProfileJpaEntity?
    fun existsByAgentCode(agentCode: String): Boolean
}
