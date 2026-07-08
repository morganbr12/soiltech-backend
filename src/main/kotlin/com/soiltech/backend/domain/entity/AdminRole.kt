package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.AdminRoleName
import java.time.LocalDateTime
import java.util.UUID

data class AdminRole(
    val id: UUID,
    val name: AdminRoleName,
    val label: String,
    val permissions: Set<String>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
