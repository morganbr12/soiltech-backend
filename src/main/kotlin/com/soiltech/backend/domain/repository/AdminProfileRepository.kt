package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.AdminProfile
import java.util.UUID

interface AdminProfileRepository {
    fun findByUserId(userId: UUID): AdminProfile?
    fun findById(id: UUID): AdminProfile?
    fun existsByUserId(userId: UUID): Boolean
    fun save(profile: AdminProfile): AdminProfile
    fun findAll(): List<AdminProfile>
}
