package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.AdminRoleName
import java.time.LocalDateTime
import java.util.UUID

data class AdminProfile(
    val id: UUID,
    val userId: UUID,
    val fullName: String,
    val email: String,
    val adminRoleId: UUID,
    val adminRoleName: AdminRoleName,
    val permissions: Set<String>,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
