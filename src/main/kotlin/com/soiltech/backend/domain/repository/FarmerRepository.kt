package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.Farmer
import com.soiltech.backend.domain.enum.FarmerStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface FarmerRepository {
    fun findById(id: UUID): Farmer?
    fun findByAgentId(agentId: UUID, pageable: Pageable): Page<Farmer>
    fun findAll(agentId: UUID, status: FarmerStatus?, query: String?, pageable: Pageable): Page<Farmer>
    fun save(farmer: Farmer): Farmer
    fun update(farmer: Farmer): Farmer
    fun delete(id: UUID)
    fun existsById(id: UUID): Boolean
}
