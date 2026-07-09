package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.UserRole
import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val role: UserRole,
    val isActive: Boolean,
    val lastLoginAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
