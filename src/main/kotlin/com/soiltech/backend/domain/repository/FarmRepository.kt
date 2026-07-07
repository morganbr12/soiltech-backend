package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.Farm
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface FarmRepository {
    fun findById(id: UUID): Farm?
    fun findByFarmerId(farmerId: UUID, pageable: Pageable): Page<Farm>
    fun save(farm: Farm): Farm
    fun update(farm: Farm): Farm
    fun delete(id: UUID)
}
