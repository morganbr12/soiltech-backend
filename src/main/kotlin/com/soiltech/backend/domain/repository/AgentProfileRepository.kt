package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.AgentProfile
import java.util.UUID

interface AgentProfileRepository {
    fun findById(id: UUID): AgentProfile?
    fun findByUserId(userId: UUID): AgentProfile?
    fun save(profile: AgentProfile): AgentProfile
}
