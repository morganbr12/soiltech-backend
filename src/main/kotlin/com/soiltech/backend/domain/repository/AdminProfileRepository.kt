package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.AdminProfile
import com.soiltech.backend.domain.enum.AdminRoleName
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface AdminProfileRepository {
    fun findByUserId(userId: UUID): AdminProfile?
    fun findById(id: UUID): AdminProfile?
    fun existsByUserId(userId: UUID): Boolean
    fun save(profile: AdminProfile): AdminProfile
    fun findAll(): List<AdminProfile>
    fun findAllFiltered(role: AdminRoleName?, isActive: Boolean?, search: String?, pageable: Pageable): Page<AdminProfile>
}
