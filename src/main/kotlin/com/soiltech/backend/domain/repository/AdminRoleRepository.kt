package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.AdminRole
import com.soiltech.backend.domain.enum.AdminRoleName
import java.util.UUID

interface AdminRoleRepository {
    fun findAll(): List<AdminRole>
    fun findById(id: UUID): AdminRole?
    fun findByName(name: AdminRoleName): AdminRole?
}
