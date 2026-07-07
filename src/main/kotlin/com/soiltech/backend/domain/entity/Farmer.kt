package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.FarmerStatus
import com.soiltech.backend.domain.enum.SyncStatus
import java.time.LocalDateTime
import java.util.UUID

data class Farmer(
    val id: UUID,
    val agentId: UUID,
    val fullName: String,
    val phone: String,
    val nationalId: String?,
    val location: String?,
    val status: FarmerStatus,
    val syncStatus: SyncStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
